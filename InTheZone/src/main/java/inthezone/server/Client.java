package inthezone.server;

import isogame.engine.CorruptDataException;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;

import inthezone.Log;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.protocol.ClientState;
import inthezone.protocol.Message;
import inthezone.protocol.MessageChannel;
import inthezone.protocol.MessageKind;
import inthezone.protocol.Protocol;
import inthezone.protocol.ProtocolException;

public class Client {
	public static final int MAX_CHALLENGES = 5;
	public static final long DISCONNECTION_TIMEOUT_MILLIS = 30 * 1000;
	public static final int MAX_PLAYERNAME_LENGTH = 32;

	private ClientState state = ClientState.HANDSHAKE;
	private SocketChannel connection;
	private MessageChannel channel;
	private final GameDataFactory dataFactory;

	private final Map<String, Client> namedClients;
	private final Map<UUID, Client> sessions;
	private final Collection<Client> pendingClients;

	public Optional<String> name = Optional.empty();

	private final Set<Client> challenges = new HashSet<>();
	private final Set<Client> challenged = new HashSet<>();
	private Optional<Game> currentGame = Optional.empty();

	private final List<Message> messages = new ArrayList<>();

	// the global game queue
	private final List<GameQueueElement> gameQueue;

	private final Collection<String> allMaps = new ArrayList<>();

	// If we are in the disconnected state, then this tells us when the
	// disconnection happened
	private long disconnectedAt = 0;

	private final UUID sessionKey = UUID.randomUUID();

	private final static String validNameMessage =
		"You cannot use slashes, angle brackets, quotation marks, " +
		"or control codes in your player name";
	
	private static boolean isValidPlayerName(final String name) {
		return (!name.matches(".*(/|\\\\|>|<|\"|\\p{IsControl}).*"));
	}

	public Client(
		final String serverName,
		final SocketChannel connection,
		final Selector sel,
		final Map<String, Client> namedClients,
		final Collection<Client> pendingClients,
		final Map<UUID, Client> sessions,
		final List<GameQueueElement> gameQueue,
		final GameDataFactory dataFactory
	) throws IOException {
		this.gameQueue = gameQueue;
		this.namedClients = namedClients;
		this.pendingClients = pendingClients;
		this.sessions = sessions;
		this.dataFactory = dataFactory;
		this.connection = connection;
		this.channel = new MessageChannel(connection, sel, this);

		sessions.put(sessionKey, this);

		allMaps.addAll(dataFactory.getStages().stream()
			.map(s -> s.name).collect(Collectors.toList()));

		// Send the server version to get the ball rolling
		channel.requestSend(Message.SV(
			Protocol.PROTOCOL_VERSION, dataFactory.getVersion(), sessionKey, serverName));
	}

	public void resetSelector(final Selector sel) throws IOException {
		channel.resetSelector(sel, this);
	}

	public String getClientName() { return name.orElse("<UNNAMED CLIENT>"); }

	/**
	 * Data from the client ready to read
	 * */
	public void receive() {
		if (!connection.isOpen()) return;
		try {
			final List<Message> msgs = channel.doRead();
			for (Message msg : msgs) doNextMessage(msg);
		} catch (IOException e) {
			Log.error("IO error reading from " + getClientName(), e);
			closeConnection(false);
		} catch (ProtocolException e) {
			Log.error("Protocol error reading from " + getClientName(), e);
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
			Log.error("IO error writing to " + getClientName(), e);
			closeConnection(false);
		}
	}

	boolean recursiveCloseConnection = false;

	/**
	 * Close the connection used by this client.  All other clients are notified
	 * that this client has left the lobby.
	 * @param intentional true if this is an intentional logoff, false if it is
	 * caused by an IO error.
	 * */
	public void closeConnection(final boolean intentional) {
		Log.info("Closing connection to " + getClientName() +
			(intentional ? "" : " (due to an error)"), null);

		currentGame.ifPresent(x -> x.close());

		if (recursiveCloseConnection) return;

		pendingClients.remove(this);
		if (intentional || state != ClientState.GAME) {
			// otherGuyLoggedOff may call closeConnection, which could lead to an
			// infinite loop.  So we need to be careful here.  Doing it this way is
			// ugly, but it guarantees that both sides get closed, no matter which
			// side gets closed first.
			recursiveCloseConnection = true;
			currentGame.ifPresent(x -> x.getOther(this).otherGuyLoggedOff());
			recursiveCloseConnection = false;
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
			currentGame.ifPresent(x -> x.getOther(this).waitForReconnect());
			state = ClientState.DISCONNECTED;
			disconnectedAt = System.currentTimeMillis();
		}
		try {
			connection.close();
		} catch (IOException e) {
			/* We tried, it failed, too bad. */
		}
	}

	/**
	 * Is the connection to this client open
	 * */
	public boolean isConnected() {
		return connection.isOpen();
	}

	/**
	 * Is the client in the disconnected state
	 * */
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
	public void leftLobby(final Client client) throws ProtocolException {
		if (!isConnected()) return;
		channel.requestSend(Message.PLAYER_LEAVES(client.name.orElseThrow(() ->
			new ProtocolException("Unnamed client attempted to leave lobby"))));
		challenges.remove(client);
	}

	/**
	 * Some other player enters the lobby.
	 * */
	public void enteredLobby(final Client client) throws ProtocolException {
		if (!isConnected()) return;
		channel.requestSend(Message.PLAYER_JOINS(client.name.orElseThrow(() ->
			new ProtocolException("Unnamed client attempted to enter lobby"))));
	}

	/**
	 * Some other client has entered a game (and so isn't available for
	 * challenges).
	 * */
	public void enteredGame(final Client client) throws ProtocolException {
		if (!isConnected()) return;
		channel.requestSend(Message.PLAYER_STARTS_GAME(client.name.orElseThrow(() ->
			new ProtocolException("Unnamed client attempted to start game"))));
	}

	/**
	 * Begin a game with another client.
	 * */
	public void startGameWith(final Client client, final Game game) {
		Log.info(getClientName() +
			" entered a game with " + client.getClientName(), null);
		messages.clear();
		state = ClientState.GAME;
		challenges.remove(client);

		queuedGame = Optional.empty();
		toPlayAs = Optional.empty();
		makingMatchWith = Optional.empty();

		currentGame.ifPresent(g -> g.close());
		currentGame = Optional.of(game);
	}

	/**
	 * A challenge that this client made is rejected.
	 * @param client The client that rejected the challenge
	 * */
	public void challengeRejected(final Client client) {
		Log.info("A challenge made by " + getClientName() +
			" has been rejected by " + client.getClientName(), null);
		challenged.remove(client);
	}

	/**
	 * A challenge made to this client is cancelled.
	 * */
	public void challengeCancelled(final Client client) {
		Log.info("A challenge made by " +
			client.getClientName() + " has been cancelled", null);
		challenges.remove(client);
	}

	/**
	 * This client is challenged to a battle.
	 * @param client The client issuing the challenge
	 * */
	public void challenge(final Client client) {
		Log.info(getClientName() +
			" has been challenged by " + client.getClientName(), null);
		challenges.add(client);
	}

	// The client we're currently matching a queued game with
	Optional<Client> makingMatchWith = Optional.empty();
	// the player to play as in the queued game that is currently under
	// construction
	Optional<Player> toPlayAs = Optional.empty(); 
	// The queued game while we wait for the other player
	Optional<StartBattleCommandRequest> queuedGame = Optional.empty();

	/**
	 * Return true if matchmaking is allowed, false otherwise
	 * @param rq the start battle request from the other player
	 * */
	public boolean makeMatch(
		final Client other, final StartBattleCommandRequest rq
	) {
		if (makingMatchWith.isPresent()) {
			Log.warn(other.getClientName() + " attempted to make match with " +
				getClientName() + ", but " + getClientName() +
				" was already matching with " +
				makingMatchWith.get().getClientName(), null);
			return false;

		} else if (state != ClientState.QUEUE) {
			Log.warn(other.getClientName() + " attempted to make match with " +
				getClientName() + ", but " + getClientName() +
				" was not in the QUEUE state", null);
			return false;

		} else {
			Log.info(getClientName() +
				" is matching with " + other.getClientName() +
				" and playing as " + rq.player.otherPlayer(), null);
			makingMatchWith = Optional.of(other);
			toPlayAs = Optional.of(rq.player.otherPlayer());
			channel.requestSend(Message.CHALLENGE_PLAYER(
				other.getClientName(), rq.getJSON(), true));

			return true;
		}
	}

	public Optional<StartBattleCommand> completeMatch(
		final StartBattleCommandRequest rq
	) throws CorruptDataException {
		if (!makingMatchWith.isPresent()) {
			Log.warn(getClientName() + " attempted to complete a match" +
				", but there was no match to complete", null);
			return Optional.empty();
		} else if (queuedGame.isPresent()) {
			Log.warn(getClientName() +
				" attempted to complete the same match twice", null);
			return Optional.empty();
		} else if (toPlayAs.map(p -> rq.player != p).orElse(true)) {
			Log.warn(getClientName() +
				" attempted to cheat by playing as the wrong character", null);
			return Optional.empty();
		} else {
			if (makingMatchWith.get().queuedGame.isPresent()) {
				Log.info(getClientName() + " completing match with " +
					makingMatchWith.get().getClientName(), null);
				return Optional.of(rq.makeCommand(
					makingMatchWith.get().queuedGame.get(), dataFactory));
			} else {
				Log.info(getClientName() +
					" finished setting up game, now waiting for" +
					makingMatchWith.get().getClientName(), null);
				queuedGame = Optional.of(rq);
				return Optional.empty();
			}
		}
	}

	private int seq = 0;

	/**
	 * Forward a message on to this client.
	 * */
	public void forwardMessage(final Message msg) throws ProtocolException {
		msg.setSequenceNumber(seq++);
		messages.add(msg);
		if (messages.size() > Protocol.MAX_GAME_MESSAGES)
			throw new ProtocolException("Too many game messages");

		if (isConnected()) channel.requestSend(msg);
	}

	/**
	 * Replay all messages that came after the provided sequence number.
	 * */
	public void replayMessagesFrom(final int lastSequenceNumber) {
		for (Message m : messages) {
			if (m.getSequenceNumber() > lastSequenceNumber) {
				channel.requestSend(m);
			}
		}
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
		Log.info(getClientName() + " has finished game with " +
			currentGame.map(c ->
				c.getOther(this).getClientName()).orElse("<NO GAME>"), null);
		state = ClientState.LOBBY;

		currentGame.ifPresent(x -> x.close());
		currentGame = Optional.empty();

		messages.clear();
	}

	/**
	 * The other player logged off
	 * */
	public void otherGuyLoggedOff() {
		if (state == ClientState.DISCONNECTED) {
			closeConnection(true);
		} else {
			Log.warn(currentGame.map(c ->
				c.getOther(this).getClientName()).orElse("<NO GAME>") +
				"has logged off while in a game with " + getClientName(), null);
			
			currentGame.ifPresent(x -> x.close());
			currentGame = Optional.empty();

			messages.clear();
			channel.requestSend(Message.LOGOFF());
			if (state == ClientState.GAME) {
				state = ClientState.LOBBY;
			}
		}

	}

	/**
	 * Wait for the other player to reconnect
	 * */
	public void waitForReconnect() {
		if (isConnected()) channel.requestSend(Message.WAIT_FOR_RECONNECT());
	}

	/**
	 * The other player has reconnected
	 * */
	public void otherGuyReconnected() {
		if (isConnected()) channel.requestSend(Message.RECONNECT(sessionKey, 0));
	}

	/**
	 * Attempt to reconnect this client
	 * */
	public void reconnect(
		final SocketChannel connection,
		final MessageChannel channel, final int lastSequenceNumber
	)
		throws ProtocolException
	{
		if (connection.isOpen()) throw new ProtocolException(
			"Attempted to reconnect client, but the client wasn't disconnected"); 
		this.channel = channel;
		channel.affiliate(this);
		this.connection = connection;

		if (currentGame.isPresent()) {
			this.state = ClientState.GAME;
			currentGame.get().getOther(this).otherGuyReconnected();
		} else {
			this.state = ClientState.LOBBY;
		}
	}

	public String getRemoteAddress() {
		try {
			return connection.getRemoteAddress().toString();
		} catch (IOException e) {
			return "Unknown address (" + e.getMessage() + ")";
		}
	}

	/**
	 * Process the next message, which may result in a state change.
	 * */
	private void doNextMessage(final Message msg) throws ProtocolException {
		if (msg.kind == MessageKind.LOGOFF) {
			Log.info(getClientName() + " logged off", null);
			closeConnection(true);
			return;
		}

		switch (state) {
			case HANDSHAKE:
				if (msg.kind != MessageKind.C_VERSION)
					throw new ProtocolException("Expected client version");
				int v = msg.parseVersion();
				final UUID gv = msg.parseGameDataVersion();
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
				final String name = msg.parseName();
				if (msg.kind == MessageKind.RECONNECT) {
					final UUID connectTo = msg.parseSessionKey();
					final Client old = sessions.get(connectTo);
					if (old == null) {
						Log.info("Someone attempted to connect to session" +
							connectTo + ", but the session has expired", null);
						channel.requestSend(Message.NOK("Cannot reconnect"));
					} else {
						final	int lastSequenceNumber = msg.parseLastSequenceNumber();
						old.reconnect(connection, channel, lastSequenceNumber);

						Log.info(old.getClientName() + " reconnected to the server", null);

						channel.requestSend(Message.OK());
						channel.requestSend(Message.PLAYERS_JOIN(namedClients.keySet()));
						old.replayMessagesFrom(lastSequenceNumber);
						old.currentGame.ifPresent(other -> {
							if (!other.getOther(this).isConnected()) old.waitForReconnect();
						});

						// remove this client
						pendingClients.remove(this);
						sessions.remove(sessionKey);
					}

				} else if (msg.kind == MessageKind.REQUEST_NAME) {
					if (namedClients.containsKey(name)) {
						Log.info(getRemoteAddress() +
							" asked for name " + name +
							", but someone else is already logged in with that name", null);

						channel.requestSend(Message.NOK("That name is already in use"));

					} else if (name.length() > MAX_PLAYERNAME_LENGTH) {
						Log.info(getRemoteAddress() +
							" asked for an overly long name", null);
						channel.requestSend(Message.NOK("That name is too long"));

					} else if (!isValidPlayerName(name)) {
						Log.info(getRemoteAddress() +
							" asked for name " + name +
							", which failed the validity check", null);

						channel.requestSend(Message.NOK(validNameMessage));

					} else {
						this.name = Optional.of(name);
						for (Client c : namedClients.values()) {
							if (c != this) c.enteredLobby(this);
						}
						namedClients.put(name, this);
						pendingClients.remove(this);
						channel.requestSend(Message.OK());
						channel.requestSend(Message.PLAYERS_JOIN(namedClients.keySet()));

						Log.info(getClientName() + " logged in from " +
							getRemoteAddress(), null);
						state = ClientState.LOBBY;
					}
				} else {
					throw new ProtocolException("Expected name request");
				}
				break;

			case LOBBY:
				if (msg.kind == MessageKind.GAME_OVER) break;

				final Client client = namedClients.get(msg.parseName());
				if (client == null) {
					Log.warn(getClientName() + " attempted to interact with " +
						msg.parseName() + ", but there is no such client on the server", null);
					channel.requestSend(Message.NOK("No such player"));

				} else {
					if (msg.kind == MessageKind.CHALLENGE_PLAYER) {
						doChallenge(msg, client);
					} else if (msg.kind == MessageKind.REJECT_CHALLENGE) {
						doRejectChallenge(msg, client);
					} else if (msg.kind == MessageKind.ACCEPT_CHALLENGE) {
						doAcceptChallenge(msg, client);
					} else if (msg.kind == MessageKind.CANCEL_CHALLENGE) {
						doCancelChallenges();
					} else if (msg.kind == MessageKind.ENTER_QUEUE) {
						doCancelChallenges();
						doEnterQueue(msg.parseVetos());
					} else {
						throw new ProtocolException("Wrong message for lobby mode");
					}
				}
				break;
			
			case QUEUE:
				if (msg.kind == MessageKind.GAME_OVER) break;

				if (msg.kind == MessageKind.CANCEL_CHALLENGE) {
					cancelQueue();
				} else if (msg.kind == MessageKind.REJECT_CHALLENGE) {
					cancelQueue();
				} else if (msg.kind == MessageKind.ACCEPT_CHALLENGE) {
					doAcceptQueueGame(msg);
				} else {
					throw new ProtocolException("Wrong message for lobby mode");
				}
				break;

			case GAME:
				final Client otherPlayer = currentGame.map(x -> x.getOther(this))
					.orElseThrow(() -> new ProtocolException(
						"In game state without a partner"));
				if (msg.kind == MessageKind.COMMAND) {
					final JSONObject cmd = msg.parseCommand();
					currentGame.ifPresent(x -> x.nextCommand(cmd, this));
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
				Log.fatal("Unknown client state, this cannot happen", null);
				throw new RuntimeException("Unknown client state, this cannot happen");
		}
	}

	/**
	 * This client challenges another client
	 * @param client The client to challenge
	 * */
	private void doChallenge(final Message msg, final Client client)
		throws ProtocolException
	{
		if (challenged.size() < MAX_CHALLENGES && client.name.isPresent()) {
			Log.info(getClientName() +
				" challenged " + client.getClientName() + " to battle!", null);
			challenged.add(client);
			client.challenge(this);
			channel.requestSend(Message.ISSUE_CHALLENGE(client.name.get()));
			msg.substitute("name", this.name.orElse(""));
			client.forwardMessage(msg);

		} else {
			Log.info(getClientName() +
				" challenged " + client.getClientName() + " to battle, but " +
				 (!client.name.isPresent() ?
					client.getClientName() + " is not ready to accept challenges" :
					getClientName() + " has too many unanswered challenges"), null);

			channel.requestSend(
				Message.NOK("Cannot challenge " + client.name.orElse("")));
		}
	}

	/**
	 * Cancel all current challenges
	 * */
	private void doCancelChallenges() throws ProtocolException {
		Log.info(getClientName() + " cancels all challenges", null);
		for (final Client c : challenged) {
			c.challengeCancelled(this);
			c.forwardMessage(Message.CANCEL_CHALLENGE(getClientName()));
		}
		challenged.clear();
	}

	/**
	 * Reject a challenge from another client
	 * @param client The client who's challenge we are rejecting
	 * */
	private void doRejectChallenge(final Message msg, final	Client client)
		throws ProtocolException
	{
		if (challenges.contains(client)) {
			Log.info(getClientName() +
				" rejects a challenge from " + client.getClientName(), null);
			challenges.remove(client);
			client.challengeRejected(this);
			client.forwardMessage(msg);
		} else {
			Log.warn(getClientName() +
				" attempted to reject a challenge from" + client.getClientName() +
				", but " + client.getClientName() +
				" never challenged " + getClientName(), null);
			channel.requestSend(Message.NOK("No such challenge to reject"));
		}
	}

	/**
	 * @param client The client to start the battle with
	 * */
	private void doAcceptChallenge(final Message msg, final Client client)
		throws ProtocolException
	{
		Log.info(getClientName() +
			" with these active challenges (" + challenges.toString() + ")" +
			" starts a battle with " + client.getClientName(), null);
		if (challenges.contains(client) && client.isReadyToPlay()) {
			challenges.remove(client);

			// reject any other challenges
			while (!challenges.isEmpty()) {
				final Client c = challenges.iterator().next();
				doRejectChallenge(
					Message.REJECT_CHALLENGE(c.getClientName(), getClientName()), c);
			}

			doStartBattle(
				client, msg.parseCommand(), msg.parsePlayer(),
				msg.parsePlayer().otherPlayer(), false);

		} else {
			Log.warn(getClientName() +
				" attempted to accept a challenge from " + client.getClientName() +
				", but " + client.getClientName() + " was already in a battle with " +
				client.currentGame.map (c ->
					c.getOther(this).getClientName()).orElse("<NO GAME>"), null);
			channel.requestSend(Message.NOK(client.name.orElse("") +
				" is already in battle with someone else"));
		}
	}

	/**
	 * Start a battle with another client.
	 * */
	private void doStartBattle(
		final Client other,
		final JSONObject cmd,
		final Player thisPlayer,
		final Player thatPlayer,
		final boolean fromQueue
	) throws ProtocolException {
		final Game game = new Game(this, other);
		game.nextCommand(cmd, this);

		startGameWith(other, game);
		other.startGameWith(this, game);

		for (final Client c : namedClients.values()) {
			if (c != this && c != other) {
				c.enteredGame(this);
				c.enteredGame(other);
			}
		}

		other.forwardMessage(Message.START_BATTLE(
			cmd, thatPlayer, getClientName(), fromQueue));

		channel.requestSend(Message.START_BATTLE(
			cmd, thisPlayer, other.getClientName(), fromQueue));
	}

	/**
	 * Enter the game queue.
	 * */
	private void doEnterQueue(final List<String> vetoMaps) {
		state = ClientState.QUEUE;

		final GameQueueElement q =
			new GameQueueElement(this, vetoMaps, allMaps);

		// attempt to start a game now
		for (int i = 0; i < gameQueue.size(); i++) {
			final Optional<GameQueueElement.StartBattlePair> sb =
				gameQueue.get(i).match(q);

			if (sb.isPresent()) {
				// we have to cross the StartBattleCommandRequests since usually player
				// A sends his SBCRQ to player B and vice versa.
				final boolean thisok = makeMatch(
					gameQueue.get(i).client, sb.get().thatone);
				final boolean thatok = gameQueue.get(i).client.makeMatch(
					this, sb.get().thisone);

				if (thisok && thatok) {
					Log.info("Matched " + getClientName() +
						" with " + gameQueue.get(i).client.getClientName(), null);
					Log.info(
						gameQueue.get(i).client.getClientName() +
						" leaves the queue", null);
					gameQueue.remove(i);
					return;
				} else {
					// we're in an invalid state, so cancel everything and return to the
					// lobby
					Log.warn(getClientName() + " and " +
						gameQueue.get(i).client.getClientName() +
						" are potentially in an invalid state" +
						", throw them both off the queue", null);
					cancelQueue();
					gameQueue.get(i).client.cancelQueue();
				}
			}
		}

		// failing that, wait in the queue
		Log.info(getClientName() + " enters queue", null);
		gameQueue.add(q);
	}

	/**
	 * Remove this client from the game queue.
	 * */
	public void cancelQueue() {
		Log.info(getClientName() + " is leaving the queue", null);
		if (state != ClientState.QUEUE) return;

		state = ClientState.LOBBY;
		for (int i = 0; i < gameQueue.size(); i++) {
			if (gameQueue.get(i).client == this) {
				Log.info("Removing " + getClientName() + " from the queue", null);
				gameQueue.remove(i); i -= 1;
			}
		}

		channel.requestSend(Message.CANCEL_QUEUE());

		queuedGame = Optional.empty();
		toPlayAs = Optional.empty();
		makingMatchWith.ifPresent(other -> {
			other.channel.requestSend(Message.CANCEL_CHALLENGE(getClientName()));
			other.cancelQueue();
			makingMatchWith = Optional.empty();
		});
	}

	/**
	 * Accept a game from the queue.
	 * */
	public void doAcceptQueueGame(final Message msg) throws ProtocolException {
		try {
			final Optional<StartBattleCommand> result =
				completeMatch(StartBattleCommandRequest.fromJSON(
					msg.parseCommand(), dataFactory));

			if (result.isPresent()) {
				doStartBattle(makingMatchWith.get(), result.get().getJSON(),
					toPlayAs.get(), toPlayAs.get().otherPlayer(), true);
			}
		} catch (final CorruptDataException e) {
			throw new ProtocolException("Error accepting queue game", e);
		}
	}
}

