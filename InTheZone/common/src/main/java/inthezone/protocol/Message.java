package inthezone.protocol;

import inthezone.battle.data.Player;
import isogame.engine.CorruptDataException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A message to be transmitted across the network.
 * */
public class Message {
	public final MessageKind kind;
	public final JSONObject payload;

	private int sequenceNumber = 0;

	/**
	 * @param kind The kind of message
	 * @param payload The JSON payload of the message
	 * */
	private Message(final MessageKind kind, final JSONObject payload) {
		this.kind = kind;
		this.payload = payload;
	}

	/**
	 * Add a sequence number to this message.
	 * */
	public void setSequenceNumber(final int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
		this.payload.put("sequenceNumber", sequenceNumber);
	}

	/**
	 * Get the message sequence number.
	 * */
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * A hack to edit messages after they've been constructed.
	 * */
	public void substitute(final String key, final Object value) {
		payload.put(key, value);
	}

	/**
	 * Server version message.
	 * @param v protocol version
	 * @param gameDataVersion game data version
	 * @param sessionKey session key
	 * @param serverName name of this server
	 * */
	public static Message SV(
		final int v,
		final UUID gameDataVersion,
		final UUID sessionKey,
		final String serverName
	) {
		final JSONObject o = new JSONObject();
		o.put("version", v);
		o.put("gdversion", gameDataVersion.toString());
		o.put("session", sessionKey.toString());
		o.put("serverName", serverName);
		return new Message(MessageKind.S_VERSION, o);
	}

	/**
	 * Client version message.
	 * @param v protocol version
	 * @param gameDataVersion game data version
	 * */
	public static Message CV(final int v, final UUID gameDataVersion) {
		final JSONObject o = new JSONObject();
		o.put("version", v);
		o.put("gdversion", gameDataVersion.toString());
		return new Message(MessageKind.C_VERSION, o);
	}

	/**
	 * OK message.
	 * */
	public static Message OK() {
		return new Message(MessageKind.OK, new JSONObject());
	}

	/**
	 * Not OK message.  Indicates an error.
	 * @param msg an error message.
	 * */
	public static Message NOK(final String msg) {
		final JSONObject o = new JSONObject();
		o.put("m", msg);
		return new Message(MessageKind.NOK, o);
	}

	/**
	 * Game data message.
	 * @param data the complete game data.
	 * */
	public static Message DATA(final JSONObject data) {
		return new Message(MessageKind.GAME_DATA, data);
	}

	/**
	 * Game over message.
	 * */
	public static Message GAME_OVER() {
		return new Message(MessageKind.GAME_OVER, new JSONObject());
	}

	/**
	 * Issue challenge message.
	 * @param name the name of the player to challenge.
	 * */
	public static Message ISSUE_CHALLENGE(final String name) {
		final JSONObject o = new JSONObject();
		o.put("name", name);
		return new Message(MessageKind.ISSUE_CHALLENGE, o);
	}

	/**
	 * Command message.
	 * @param cmd the game command to transmit.
	 * */
	public static Message COMMAND(final JSONObject cmd) {
		final JSONObject o = new JSONObject();
		o.put("cmd", cmd);
		return new Message(MessageKind.COMMAND, o);
	}

	/**
	 * Request name message.
	 * @param name the player name to request
	 * */
	public static Message NAME(final String name) {
		final JSONObject o = new JSONObject();
		o.put("name" , name);
		return new Message(MessageKind.REQUEST_NAME, o);
	}

	/**
	 * Notification that players joined the lobby.
	 * @param players the names of the players that joined the lobby.
	 * */
	public static Message PLAYERS_JOIN(final Collection<String> players) {
		final JSONObject o = new JSONObject();
		o.put("add", players);
		o.put("leave", new JSONArray());
		o.put("game", new JSONArray());
		return new Message(MessageKind.PLAYERS_JOIN, o);
	}

	/**
	 * Notification that a player joined the lobby.
	 * @param name the name of the player that joined the lobby.
	 * */
	public static Message PLAYER_JOINS(final String name) {
		final JSONObject o = new JSONObject();
		final JSONArray a = new JSONArray();
		a.put(name);
		o.put("add", a);
		o.put("leave", new JSONArray());
		o.put("game", new JSONArray());
		return new Message(MessageKind.PLAYERS_JOIN, o);
	}

	/**
	 * Notification that a player left the lobby.
	 * @param name the name of the player that left the lobby.
	 * */
	public static Message PLAYER_LEAVES(final String name) {
		final JSONObject o = new JSONObject();
		final JSONArray a = new JSONArray();
		a.put(name);
		o.put("add", new JSONArray());
		o.put("leave", a);
		o.put("game", new JSONArray());
		return new Message(MessageKind.PLAYERS_JOIN, o);
	}

	/**
	 * Notification that a player entered a game.
	 * @param name the name of the player that entered a game.
	 * */
	public static Message PLAYER_STARTS_GAME(final String name) {
		final JSONObject o = new JSONObject();
		final JSONArray a = new JSONArray();
		a.put(name);
		o.put("add", new JSONArray());
		o.put("leave", new JSONArray());
		o.put("game", a);
		return new Message(MessageKind.PLAYERS_JOIN, o);
	}

	/**
	 * Challenge player message.
	 * @param name the name of the player to challenge
	 * @param cmd the StartBattleCommandRequest
	 * @param inQueue true if this challenge came from the game queue.  false if
	 * it is a direct challenge (i.e. the player click the challenge button)
	 * */
	public static Message CHALLENGE_PLAYER(
		final String name,
		final JSONObject cmd,
		final boolean inQueue
	) {
		final JSONObject o = new JSONObject();
		o.put("name", name);
		o.put("cmd", cmd);
		o.put("isQueue", inQueue);
		return new Message(MessageKind.CHALLENGE_PLAYER, o);
	}

	/**
	 * Accept challenge message.
	 * @param name the name of the player who's challenge we're accepting
	 * @param player the player accepting the challenge
	 * @param cmd the StartBattleCommandRequest
	 * @param fromQueue true if we are accepting a game from the game queue,
	 * otherwise false.
	 * */
	public static Message ACCEPT_CHALLENGE(
		final String name,
		final Player player,
		final JSONObject cmd,
		final boolean fromQueue
	) {
		final JSONObject o = new JSONObject();
		o.put("name", name);
		o.put("player", player.toString());
		o.put("cmd", cmd);
		o.put("isQueue", fromQueue);
		return new Message(MessageKind.ACCEPT_CHALLENGE, o);
	}

	/**
	 * Reject challenge message.
	 * @param name the name of the player we are rejecting
	 * @param myName the name of the player doing the rejecting
	 * @param notReady true if this is an automatic rejection due to the other
	 * client not being ready to accept challenges
	 * */
	public static Message REJECT_CHALLENGE(
		final String name,
		final String myName,
		final boolean notReady
	) {
		final JSONObject o = new JSONObject();
		o.put("name", name);
		o.put("otherPlayer", myName);
		o.put("notReady", notReady);
		return new Message(MessageKind.REJECT_CHALLENGE, o);
	}

	/**
	 * Notification that a battle is starting
	 * @param cmd the StartBattleCommand
	 * @param player the player that the recipient of this message should play.
	 * @param otherPlayer the name of the other player
	 * @param fromQueue true if this game is starting from the game queue, false
	 * if it is starting from a direct challenge.
	 * */
	public static Message START_BATTLE(
		final JSONObject cmd,
		final Player player,
		final String otherPlayer,
		final boolean fromQueue
	) {
		final JSONObject o = new JSONObject();
		o.put("player", player.toString());
		o.put("otherPlayer", otherPlayer);
		o.put("cmd", cmd);
		o.put("isQueue", fromQueue);
		return new Message(MessageKind.START_BATTLE, o);
	}

	/**
	 * Logoff message.
	 * */
	public static Message LOGOFF() {
		return new Message(MessageKind.LOGOFF, new JSONObject());
	}

	/**
	 * Notification to wait for the other player to reconnect.
	 * */
	public static Message WAIT_FOR_RECONNECT() {
		return new Message(MessageKind.WAIT_FOR_RECONNECT, new JSONObject());
	}

	/**
	 * Reconnect message.
	 * @param sessionKey the session to reconnect to
	 * @param sequenceNumber the sequence number of the last message received
	 * */
	public static Message RECONNECT(
		final UUID sessionKey, final int sequenceNumber
	) {
		final JSONObject o = new JSONObject();
		o.put("session", sessionKey);
		o.put("lastSequenceNumber", sequenceNumber);
		return new Message(MessageKind.RECONNECT, o);
	}

	/**
	 * Cancel a challenge.
	 * @param fromPlayer the player that is cancelling the challenge that they
	 * issued
	 * */
	public static Message CANCEL_CHALLENGE(
		final String fromPlayer
	) {
		final JSONObject o = new JSONObject();
		o.put("name", fromPlayer);
		return new Message(MessageKind.CANCEL_CHALLENGE, o);
	}

	/**
	 * Enter the game queue.
	 * @param name the name of the player entering the queue
	 * @param vetoMaps the maps to veto
	 * */
	public static Message ENTER_QUEUE(
		final String name,
		final List<String> vetoMaps
	) {
		final JSONObject o = new JSONObject();
		final JSONArray a = new JSONArray();
		for (final String m : vetoMaps) a.put(m);
		o.put("veto", a);
		o.put("name", name);
		return new Message(MessageKind.ENTER_QUEUE, o);
	}

	/**
	 * Exit the game queue.
	 * */
	public static Message CANCEL_QUEUE() {
		return new Message(MessageKind.CANCEL_QUEUE, new JSONObject());
	}

	/**
	 * Extract the name field.
	 * */
	public String parseName() throws ProtocolException {
		if (kind != MessageKind.REQUEST_NAME &&
			kind != MessageKind.CHALLENGE_PLAYER &&
			kind != MessageKind.CANCEL_CHALLENGE &&
			kind != MessageKind.ACCEPT_CHALLENGE &&
			kind != MessageKind.REJECT_CHALLENGE &&
			kind != MessageKind.ENTER_QUEUE &&
			kind != MessageKind.ISSUE_CHALLENGE)
				throw new ProtocolException("Expected name");
		try {
			return payload.getString("name");
		} catch (final JSONException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the player field.
	 * */
	public Player parsePlayer() throws ProtocolException {
		if (kind != MessageKind.ACCEPT_CHALLENGE &&
			kind != MessageKind.START_BATTLE)
				throw new ProtocolException("Expected player");

		try {
			return Player.fromString(payload.getString("player"));
		} catch (final JSONException|CorruptDataException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the otherPlayer field.
	 * */
	public String parseOtherPlayer() throws ProtocolException {
		if (kind != MessageKind.REJECT_CHALLENGE)
			throw new ProtocolException("Expected name");
		try {
			return payload.getString("otherPlayer");
		} catch (final JSONException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the command field.
	 * */
	public JSONObject parseCommand() throws ProtocolException {
		if (kind != MessageKind.ACCEPT_CHALLENGE &&
			kind != MessageKind.COMMAND &&
			kind != MessageKind.START_BATTLE &&
			kind != MessageKind.CHALLENGE_PLAYER)
				throw new ProtocolException("Expected command");

		try {
			return payload.getJSONObject("cmd");
		} catch (final JSONException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the protocol version field.
	 * */
	public int parseVersion() throws ProtocolException {
		if (kind != MessageKind.C_VERSION && kind != MessageKind.S_VERSION)
			throw new ProtocolException("Expected version");
		try {
			return payload.getInt("version");
		} catch (final JSONException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the game data version field.
	 * */
	public UUID parseGameDataVersion() throws ProtocolException {
		if (kind != MessageKind.C_VERSION && kind != MessageKind.S_VERSION)
			throw new ProtocolException("Expected version");
		try {
			return UUID.fromString(payload.getString("gdversion"));
		} catch (final JSONException|IllegalArgumentException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the session key field.
	 * */
	public UUID parseSessionKey() throws ProtocolException {
		if (kind != MessageKind.S_VERSION && kind != MessageKind.RECONNECT)
			throw new ProtocolException("Expected session key");
		try {
			return UUID.fromString(payload.getString("session"));
		} catch (final JSONException|IllegalArgumentException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the players that joined the lobby.
	 * */
	public Collection<String> parseJoinedLobby() throws ProtocolException {
		if (kind != MessageKind.PLAYERS_JOIN)
			throw new ProtocolException("Expected lobby players message");
		try {
			return jsonArrayToList(payload.getJSONArray("add"), String.class);
		} catch (final JSONException|ClassCastException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the players that left the lobby.
	 * */
	public Collection<String> parseLeftLobby() throws ProtocolException {
		if (kind != MessageKind.PLAYERS_JOIN)
			throw new ProtocolException("Expected lobby players message");
		try {
			return jsonArrayToList(payload.getJSONArray("leave"), String.class);
		} catch (final JSONException|ClassCastException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the players that joined games.
	 * */
	public Collection<String> parseJoinedGame() throws ProtocolException {
		if (kind != MessageKind.PLAYERS_JOIN)
			throw new ProtocolException("Expected lobby players message");
		try {
			return jsonArrayToList(payload.getJSONArray("game"), String.class);
		} catch (final JSONException|ClassCastException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the last sequence number field.
	 * */
	public int parseLastSequenceNumber() throws ProtocolException {
		if (kind != MessageKind.RECONNECT)
			throw new ProtocolException("Expected reconnect");

		try {
			return payload.getInt("lastSequenceNumber");
		} catch (final JSONException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the message field
	 * */
	public String parseMessage() throws ProtocolException {
		if (kind != MessageKind.NOK)
			throw new ProtocolException("Expected message");

		try {
			return payload.getString("m");
		} catch (final JSONException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Extract the veto list field.
	 * */
	public List<String> parseVetos() throws ProtocolException {
		if (kind != MessageKind.ENTER_QUEUE)
			throw new ProtocolException("Expected enter queue message");

		try {
			final List<String> r = new ArrayList<>();
			final JSONArray a = payload.getJSONArray("veto");
			for (int i = 0; i < a.length(); i++) {
				r.add(a.getString(i));
			}
			return r;
		} catch (final JSONException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	/**
	 * Parse a message from a String.
	 * @param message the String to parse.
	 * */
	public static Message fromString(final String message)
		throws ProtocolException
	{
		final String id = message.substring(0, 2);
		final String data = message.substring(2);

		try {
			final JSONObject json = new JSONObject(data);
			final Message r = new Message(MessageKind.fromString(id), json);
			r.sequenceNumber = json.optInt("sequenceNumber", 0);
			return r;

		} catch (final JSONException e) {
			throw new ProtocolException("Malformed JSON " + data, e);
		}
	}

	@Override
	public String toString() {
		return kind.toString() + payload.toString() + "\n";
	}

	private static <T> List<T> jsonArrayToList(
		final JSONArray a, final Class<T> clazz
	)
		throws ClassCastException
	{
		final List<T> r = new ArrayList<>();
		final int limit = a.length();
		for (int i = 0; i < limit; i++) {
			r.add(clazz.cast(a.get(i)));
		}
		return r;
	}
}

