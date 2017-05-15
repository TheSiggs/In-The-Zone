package inthezone.comptroller;

import inthezone.battle.commands.Command;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.protocol.Message;
import inthezone.protocol.MessageKind;
import inthezone.protocol.Protocol;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.Optional;
import java.util.UUID;

public class Network implements Runnable {
	private final static long timeout = 20*1000;
	private final TimeUnit timeoutUnit = TimeUnit.SECONDS;

	private final GameDataFactory gameData;
	private final LobbyListener lobbyListener;

	private final AtomicBoolean connect = new AtomicBoolean(false);
	private String host;
	private int port;
	private Socket socket = null;
	private BufferedReader fromServer = null;
	private Writer toServer = null; 
	private String playerName;
	private UUID session = null;

	private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
	public final BlockingQueue<Command> readCommandQueue = new LinkedBlockingQueue<>();

	public Network(
		GameDataFactory gameData,
		LobbyListener lobbyListener
	) {
		this.gameData = gameData;
		this.lobbyListener = lobbyListener;
	}

	private volatile boolean disconnectNow = false;

	public synchronized void shutdown() {
		disconnectNow = true;
		logout();
	}

	@Override
	public void run() {
		while (!disconnectNow) {

			synchronized (connect) {
				while (!connect.get()) {
					try {
						connect.wait();
					} catch (InterruptedException e) {
						/* ignore */
					}
				}

				try {
					doConnect();
					(new Thread(new NetworkReader(
						fromServer, lobbyListener, readCommandQueue, gameData,
							Thread.currentThread()))).start();
				} catch (IOException e) {
					cleanUpConnection();
					lobbyListener.errorConnectingToServer(e);
					continue;
				} catch (ProtocolException e) {
					cleanUpConnection();
					lobbyListener.serverError(e);
					continue;
				}
			}

			while (true) {
				try {
					Message msg = sendQueue.poll(timeout, timeoutUnit);
					if (msg == null) throw new InterruptedException();

					toServer.write(msg.toString());
					toServer.flush();

					if (msg.kind == MessageKind.LOGOFF) {
						cleanUpConnection();
						lobbyListener.loggedOff();
						break;
					}
				} catch (InterruptedException e) {
					if (socket.isClosed()) {
						cleanUpConnection();
						lobbyListener.connectionDropped();
						break;
					}
				} catch (IOException e) {
					cleanUpConnection();
					lobbyListener.connectionDropped();
					break;
				}
			}
		}
	}

	private void cleanUpConnection() {
		sendQueue.clear();
		if (socket != null) try {
			socket.close();
		} catch (IOException e) {
			/* Doesn't matter */
		}
		connect.set(false);
	}

	private void doConnect() throws IOException, ProtocolException {
		// connect to the server.  If this fails, terminate the thread.
		this.socket = new Socket(host, port);
		this.fromServer = new BufferedReader(
			new InputStreamReader(socket.getInputStream(), "UTF-8"));
		this.toServer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");

		toServer.write(Message.CV(
			Protocol.PROTOCOL_VERSION,
			gameData.getVersion()).toString());
		toServer.flush();

		String raw = fromServer.readLine();
		if (raw == null) throw new IOException("Unexpected disconnection");
		Message r = Message.fromString(raw);
		if (r.kind != MessageKind.S_VERSION) {
			throw new ProtocolException("Server error: protocol violation #1");
		} else {
			this.session = r.parseSessionKey();
		}

		raw = fromServer.readLine();
		if (raw == null) throw new IOException("Unexpected disconnection");
		r = Message.fromString(raw);
		if (r.kind == MessageKind.GAME_DATA) {
			try {
				gameData.update(r.payload);
			} catch (CorruptDataException e) {
				throw new ProtocolException("Server error: bad game data", e);
			}
		} else if (r.kind != MessageKind.OK) {
			throw new ProtocolException("Server error: protocol violation #2");
		}

		// get a name on the server
		boolean named = false;
		while (!named) {
			toServer.write(Message.NAME(playerName).toString());
			toServer.flush();
			raw = fromServer.readLine();
			if (raw == null) throw new IOException("Unexpected disconnection");
			r = Message.fromString(raw);
			if (r.kind == MessageKind.OK) {
				named = true;
			} else {
				Optional<String> nextPlayerName =
					lobbyListener.tryDifferentPlayerName(playerName);
				if (!nextPlayerName.isPresent()) {
					throw new ProtocolException("Cannot get name on server");
				} else {
					playerName = nextPlayerName.get();
				}
			}
		}

		// server sends a list of players in the lobby
		raw = fromServer.readLine();
		if (raw == null) throw new IOException("Unexpected disconnection");
		r = Message.fromString(raw);
		if (r.kind != MessageKind.PLAYERS_JOIN) {
			throw new ProtocolException("Server error: protocol violation #4");
		} else {
			lobbyListener.connectedToServer(playerName, r.parseJoinedLobby());
		}
	}

	public synchronized void connectToServer(String host, int port, String playerName) {
		synchronized (connect) {
			if (connect.get()) return;
			connect.set(true);
			this.host = host;
			this.port = port;
			this.playerName = playerName;
			connect.notify();
		}
	}

	public void challengePlayer(
		StartBattleCommandRequest cmd, String player
	) {
		try {
			sendQueue.put(Message.CHALLENGE_PLAYER(player, cmd.getJSON()));
		} catch (InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	/**
	 * @param player The player accepting the challenge
	 * @param otherPlayer The player who's challenge we're accepting
	 * */
	public void acceptChallenge(
		StartBattleCommand cmd, Player player, String otherPlayer
	) {
		try {
			sendQueue.put(Message.ACCEPT_CHALLENGE(otherPlayer, player, cmd.getJSON()));
		} catch (InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	public void refuseChallenge(String player) {
		try {
			sendQueue.put(Message.REJECT_CHALLENGE(player));
		} catch (InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	public void sendCommand(Command cmd) {
		try {
			sendQueue.put(Message.COMMAND(cmd.getJSON()));
		} catch (InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	public void gameOver() {
		try {
			sendQueue.put(Message.GAME_OVER());
		} catch (InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	public void logout() {
		try {
			sendQueue.put(Message.LOGOFF());
		} catch (InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}
}

