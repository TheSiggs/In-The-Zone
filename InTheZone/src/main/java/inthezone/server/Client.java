package inthezone.server;

import inthezone.battle.data.GameDataFactory;
import inthezone.protocol.ClientState;
import inthezone.protocol.Message;
import inthezone.protocol.MessageChannel;
import inthezone.protocol.MessageKind;
import inthezone.protocol.Protocol;
import inthezone.protocol.ProtocolException;
import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Client {
	public static final int MAX_CHALLENGES = 5;
	public static final long DISCONNECTION_TIMEOUT_MILLIS = 5 * 60 * 1000;

	private ClientState state = ClientState.HANDSHAKE;
	private SocketChannel connection;
	private MessageChannel channel;
	private final GameDataFactory dataFactory;

	private final Map<String, Client> namedClients;
	private final Map<UUID, Client> sessions;
	private final Collection<Client> pendingClients;

	public Optional<String> name = Optional.empty();

	private Set<Client> challenges = new HashSet<>();
	private Set<Client> challenged = new HashSet<>();
	private Optional<Client> inGameWith = Optional.empty();

	// If we are in the disconnected state, then this tells us when the
	// disconnection happened
	private long disconnectedAt = 0;

	private final UUID sessionKey = UUID.randomUUID();
	
	public Client(
		SocketChannel connection,
		Selector sel,
		Map<String, Client> namedClients,
		Collection<Client> pendingClients,
		Map<UUID, Client> sessions,
		GameDataFactory dataFactory
	) throws IOException {
		this.namedClients = namedClients;
		this.pendingClients = pendingClients;
		this.sessions = sessions;
		this.dataFactory = dataFactory;
		this.connection = connection;
		this.channel = new MessageChannel(connection, sel, this);

		sessions.put(sessionKey, this);

		// Send the server version to get the ball rolling
		channel.requestSend(Message.SV(
			Protocol.PROTOCOL_VERSION, dataFactory.getVersion(), sessionKey));
	}

	public void resetSelector(Selector sel) throws IOException {
		channel.resetSelector(sel, this);
	}

	/**
	 * Data from the client ready to read
	 * */
	public void receive() {
		if (!connection.isOpen()) return;
		try {
			List<Message> msgs = channel.doRead();
			for (Message msg : msgs) doNextMessage(msg);
		} catch (IOException e) {
			e.printStackTrace(System.err);
			closeConnection(false);
		} catch (ProtocolException e) {
			e.printStackTrace(System.err);
			closeConnection(false);
		}
	}

	/**
	 * Client ready to be sent data.
	 * */
	public void send() {
		if (!connection.isOpen()) return;
		try {
			channel.doWrite();
		} catch (IOException e) {
			e.printStackTrace(System.err);
			closeConnection(false);
		}
	}

	/**
	 * Close the connection used by this client.  All other clients are notified
	 * that this client has left the lobby.
	 * @param intentional true if this is an intentional logoff, false if it is
	 * caused by an IO error.
	 * */
	public void closeConnection(boolean intentional) {
		pendingClients.remove(this);
		if (intentional) {
			inGameWith.ifPresent(x -> x.otherGuyLoggedOff());
			sessions.remove(sessionKey);
			if (name.isPresent()) {
				namedClients.remove(name.get());
				for (Client c : namedClients.values()) {
					try {
						c.leftLobby(this);
					} catch (ProtocolException e) {
						/* If there was an error, then that client can't have had a
						 * reference to us anyway, so we can ignore the exception */
					}
				}
			}
		} else {
			inGameWith.ifPresent(x -> x.waitForReconnect());
			state = ClientState.DISCONNECTED;
			disconnectedAt = System.currentTimeMillis();
		}
		try {
			connection.close();
		} catch (IOException e) {
			/* We tried, it failed, too bad. */
		}
	}

	public boolean isConnected() {
		return connection.isOpen();
	}

	public boolean isDisconnected() {
		return state == ClientState.DISCONNECTED;
	}

	public boolean isDisconnectedTimeout() {
		return state == ClientState.DISCONNECTED &&
			(System.currentTimeMillis() - disconnectedAt) > DISCONNECTION_TIMEOUT_MILLIS;
	}

	/**
	 * Some other player leaves the lobby, i.e. disconnects from the server.
	 * */
	public void leftLobby(Client client) throws ProtocolException {
		channel.requestSend(Message.PLAYER_LEAVES(client.name.orElseThrow(() ->
			new ProtocolException("Unnamed client attempted to leave lobby"))));
		challenges.remove(client);
		challenged.remove(client);
	}

	/**
	 * Some other player enters the lobby.
	 * */
	public void enteredLobby(Client client) throws ProtocolException {
		channel.requestSend(Message.PLAYER_JOINS(client.name.orElseThrow(() ->
			new ProtocolException("Unnamed client attempted to enter lobby"))));
	}

	/**
	 * Some other client has entered a game (and so isn't available for
	 * challenges).
	 * */
	public void enteredGame(Client client) throws ProtocolException {
		channel.requestSend(Message.PLAYER_STARTS_GAME(client.name.orElseThrow(() ->
			new ProtocolException("Unnamed client attempted to start game"))));
	}

	/**
	 * Begin a game with another client.
	 * */
	public void startGameWith(Client client) {
		state = ClientState.GAME;
		challenges.remove(client);
		inGameWith = Optional.of(client);
	}

	/**
	 * A challenge that this client made is rejected.
	 * */
	public void challengeRejected(Client client) {
		challenged.remove(client);
	}

	/**
	 * @param client The client issuing the challenge
	 * */
	public void challenge(Client client) {
		challenges.add(client);
	}

	/**
	 * Forward a message on to this client.
	 * */
	public void forwardMessage(Message msg) {
		channel.requestSend(msg);
	}

	/**
	 * Determine if this player is available to enter a game.
	 * */
	public boolean isReadyToPlay() {
		return state == ClientState.LOBBY;
	}

	/**
	 * The game that this client was playing is now over.
	 * */
	public void gameOver() {
		state = ClientState.LOBBY;
		inGameWith = Optional.empty();
	}

	/**
	 * The other player logged off
	 * */
	public void otherGuyLoggedOff() {
		inGameWith = Optional.empty();
		channel.requestSend(Message.LOGOFF());
		if (state == ClientState.GAME) {
			state = ClientState.LOBBY;
		}
	}

	/**
	 * Wait for the other player to reconnect
	 * */
	public void waitForReconnect() {
		channel.requestSend(Message.WAIT_FOR_RECONNECT());
	}

	/**
	 * The other player has reconnected
	 * */
	public void otherGuyReconnected() {
		channel.requestSend(Message.RECONNECT());
	}

	/**
	 * */
	public void reconnect(SocketChannel connection, MessageChannel channel)
		throws ProtocolException
	{
		if (connection.isOpen()) throw new ProtocolException(
			"Attempted to reconnect client, but the client wasn't disconnected"); 
		this.channel = channel;
		channel.affiliate(this);
		this.connection = connection;

		if (inGameWith.isPresent()) {
			this.state = ClientState.GAME;
			inGameWith.get().otherGuyReconnected();
		} else {
			this.state = ClientState.LOBBY;
		}
	}

	/**
	 * Process the next message, which may result in a state change.
	 * */
	private void doNextMessage(Message msg) throws ProtocolException {
		if (msg.kind == MessageKind.LOGOFF) {
			closeConnection(true);
			return;
		}

		switch (state) {
			case HANDSHAKE:
				if (msg.kind != MessageKind.C_VERSION)
					throw new ProtocolException("Expected client version");
				int v = msg.parseVersion();
				UUID gv = msg.parseGameDataVersion();
				if (v != Protocol.PROTOCOL_VERSION)
					throw new ProtocolException("Wrong protocol version");
				if (gv.equals(dataFactory.getVersion())) {
					channel.requestSend(Message.OK());
				} else {
					channel.requestSend(Message.DATA(dataFactory.getJSON()));
				}
				state = ClientState.NAMING;
				break;

			case NAMING:
				String name = msg.parseName();
				if (msg.kind == MessageKind.RECONNECT) {
					UUID connectTo = msg.parseSessionKey();
					Client old = sessions.get(connectTo);
					if (old == null) {
						channel.requestSend(Message.NOK("Cannot reconnect"));
					} else {
						old.reconnect(connection, channel);
						channel.requestSend(Message.OK());
						channel.requestSend(Message.PLAYERS_JOIN(namedClients.keySet()));

						// remove this client
						pendingClients.remove(this);
						sessions.remove(sessionKey);
					}
				} else if (msg.kind == MessageKind.REQUEST_NAME) {
					if (namedClients.containsKey(name)) {
						channel.requestSend(Message.NOK("That name is already in use"));
					} else {
						this.name = Optional.of(name);
						for (Client c : namedClients.values()) {
							if (c != this) c.enteredLobby(this);
						}
						namedClients.put(name, this);
						pendingClients.remove(this);
						channel.requestSend(Message.OK());
						channel.requestSend(Message.PLAYERS_JOIN(namedClients.keySet()));
						state = ClientState.LOBBY;
					}
				} else {
					throw new ProtocolException("Expected name request");
				}
				break;

			case LOBBY:
				Client client = namedClients.get(msg.parseName());
				if (client == null) {
					channel.requestSend(Message.NOK("No such player"));
				} else {
					if (msg.kind == MessageKind.CHALLENGE_PLAYER) {
						doChallenge(msg, client);
					} else if (msg.kind == MessageKind.REJECT_CHALLENGE) {
						doRejectChallenge(msg, client);
					} else if (msg.kind == MessageKind.ACCEPT_CHALLENGE) {
						doAcceptChallenge(msg, client);
					} else {
						throw new ProtocolException("Wrong message for lobby mode");
					}
				}
				break;

			case GAME:
				Client otherPlayer = inGameWith.orElseThrow(() ->
					new ProtocolException("In game state without a partner"));
				if (msg.kind == MessageKind.COMMAND) {
					otherPlayer.forwardMessage(msg);
				} else if (msg.kind == MessageKind.GAME_OVER) {
					otherPlayer.gameOver();
					this.gameOver();
					for (Client c : namedClients.values()) {
						if (c != otherPlayer) c.enteredLobby(otherPlayer);
						if (c != this) c.enteredLobby(this);
					}
				} else if (msg.kind == MessageKind.REJECT_CHALLENGE) {
					doRejectChallenge(msg, namedClients.get(msg.parseName()));
				} else {
					throw new ProtocolException("Wrong message for game mode");
				}

				break;

			case DISCONNECTED:
				throw new ProtocolException(
					"Cannot receive messages while disconnected, unless it's ghosts.  Must be ghosts.");

			default:
				throw new RuntimeException("This cannot happen");
		}
	}

	private void doChallenge(Message msg, Client client) {
		if (challenged.size() < MAX_CHALLENGES && client.name.isPresent()) {
			challenged.add(client);
			client.challenge(this);
			channel.requestSend(Message.ISSUE_CHALLENGE(client.name.get()));
			msg.substitute("name", this.name.orElse(""));
			client.forwardMessage(msg);
		} else {
			channel.requestSend(
				Message.NOK("Cannot challenge " + client.name.orElse("")));
		}
	}

	private void doRejectChallenge(Message msg, Client client) {
		if (challenges.contains(client)) {
			challenges.remove(client);
			client.challengeRejected(this);
			client.forwardMessage(msg);
		} else {
			channel.requestSend(Message.NOK("No such challenge to reject"));
		}
	}

	/**
	 * @param client The client to start the battle with
	 * */
	private void doAcceptChallenge(Message msg, Client client)
		throws ProtocolException
	{
		System.err.println("challenges: " + challenges.toString() + "? " + client.toString());
		if (challenges.contains(client) && client.isReadyToPlay()) {
			challenges.remove(client);
			startGameWith(client);
			client.startGameWith(this);

			for (Client c : namedClients.values()) {
				if (c != this && c != client) {
					c.enteredGame(this);
					c.enteredGame(client);
				}
			}

			client.forwardMessage(Message.START_BATTLE(
				msg.parseCommand(),
				msg.parsePlayer().otherPlayer(),
				this.name.orElse("")));

			channel.requestSend(Message.START_BATTLE(
				msg.parseCommand(),
				msg.parsePlayer(),
				msg.parseName()));
		} else {
			channel.requestSend(Message.NOK(client.name.orElse("") +
				" is already in battle with someone else"));
		}
	}
}

