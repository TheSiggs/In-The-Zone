package inthezone.comptroller;

import isogame.engine.CorruptDataException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import inthezone.battle.commands.Command;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.protocol.Message;
import inthezone.protocol.MessageKind;
import inthezone.protocol.Protocol;
import inthezone.protocol.ProtocolException;

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
		final GameDataFactory gameData,
		final LobbyListener lobbyListener
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
					} catch (final InterruptedException e) {
						/* ignore */
					}
				}
				connect.set(false);

				try {
					doConnect();
					(new Thread(new NetworkReader(
						fromServer, lobbyListener, readCommandQueue, gameData,
							Thread.currentThread()))).start();
				} catch (final IOException e) {
					cleanUpConnection();
					lobbyListener.errorConnectingToServer(e);
					continue;
				} catch (final ProtocolException e) {
					cleanUpConnection();
					lobbyListener.serverError(e);
					continue;
				}
			}

			while (true) {
				try {
					final Message msg = sendQueue.poll(timeout, timeoutUnit);
					if (msg == null) throw new InterruptedException();

					toServer.write(msg.toString());
					toServer.flush();

					if (msg.kind == MessageKind.LOGOFF) {
						cleanUpConnection();
						lobbyListener.loggedOff();
						break;
					}
				} catch (final InterruptedException e) {
					if (socket.isClosed()) {
						cleanUpConnection();
						lobbyListener.connectionDropped();
						break;
					}
				} catch (final IOException e) {
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
		} catch (final IOException e) {
			/* Doesn't matter */
		}
		connect.set(false);
	}

	private void doConnect() throws IOException, ProtocolException {
		// connect to the server.  If this fails, terminate the thread.
		this.socket = new Socket(host, port);
		this.socket.setKeepAlive(true);
		this.socket.setTcpNoDelay(true);
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
			} catch (final CorruptDataException e) {
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
				final String reason = r.parseMessage();
				throw new IOException(reason);
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

	public synchronized void connectToServer(
		final String host, final int port, final String playerName
	) {
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
		final StartBattleCommandRequest cmd, final String player
	) {
		try {
			sendQueue.put(Message.CHALLENGE_PLAYER(player, cmd.getJSON(), false));
		} catch (final InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	public void enterQueue(final List<String> vetoMaps) {
		try {
			sendQueue.put(Message.ENTER_QUEUE(playerName, vetoMaps));
		} catch (final InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	/**
	 * @param player The player accepting the challenge
	 * @param otherPlayer The player who's challenge we're accepting
	 * */
	public void acceptChallenge(
		final StartBattleCommand cmd,
		final Player player,
		final String otherPlayer
	) {
		try {
			sendQueue.put(Message.ACCEPT_CHALLENGE(
				otherPlayer, player, cmd.getJSON(), false));
		} catch (final InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	/**
	 * @param player the player to reject
	 * @param thisPlayer this player
	 * */
	public void refuseChallenge(final String player, final String thisPlayer) {
		try {
			sendQueue.put(Message.REJECT_CHALLENGE(player, thisPlayer));
		} catch (final InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	public void cancelChallenge() {
		try {
			sendQueue.put(Message.CANCEL_CHALLENGE(playerName));
		} catch (final InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	public void sendCommand(final Command cmd) {
		try {
			sendQueue.put(Message.COMMAND(cmd.getJSON()));
		} catch (final InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	public void gameOver() {
		try {
			sendQueue.put(Message.GAME_OVER());
		} catch (final InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}

	public void logout() {
		try {
			sendQueue.put(Message.LOGOFF());
		} catch (final InterruptedException e) {
			throw new RuntimeException("This cannot happen");
		}
	}
}

