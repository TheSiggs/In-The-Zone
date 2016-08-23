package inthezone.protocol;

import inthezone.battle.data.Player;
import isogame.engine.CorruptDataException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Message {
	public final MessageKind kind;
	public final JSONObject payload;

	public Message(MessageKind kind, JSONObject payload) {
		this.kind = kind;
		this.payload = payload;
	}

	@SuppressWarnings("unchecked")
	public static Message SV(int v, UUID gameDataVersion, UUID sessionKey) {
		JSONObject o = new JSONObject();
		o.put("version", v);
		o.put("gdversion", gameDataVersion.toString());
		o.put("session", sessionKey.toString());
		return new Message(MessageKind.S_VERSION, o);
	}

	@SuppressWarnings("unchecked")
	public static Message CV(int v, UUID gameDataVersion) {
		JSONObject o = new JSONObject();
		o.put("version", v);
		o.put("gdversion", gameDataVersion.toString());
		return new Message(MessageKind.C_VERSION, o);
	}

	public static Message OK() {
		return new Message(MessageKind.OK, new JSONObject());
	}

	public static Message NOK() {
		return new Message(MessageKind.NOK, new JSONObject());
	}

	public static Message DATA(JSONObject data) {
		return new Message(MessageKind.GAME_DATA, data);
	}

	@SuppressWarnings("unchecked")
	public static Message COMMAND(JSONObject cmd) {
		JSONObject o = new JSONObject();
		o.put("cmd", cmd);
		return new Message(MessageKind.COMMAND, o);
	}

	@SuppressWarnings("unchecked")
	public static Message NAME(String name) {
		JSONObject o = new JSONObject();
		o.put("name" , name);
		return new Message(MessageKind.REQUEST_NAME, o);
	}

	@SuppressWarnings("unchecked")
	public static Message PLAYERS_JOIN(Collection<String> players) {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		a.addAll(players);
		o.put("add", a);
		o.put("leave", new JSONArray());
		o.put("game", new JSONArray());
		return new Message(MessageKind.PLAYERS_JOIN, o);
	}

	@SuppressWarnings("unchecked")
	public static Message PLAYER_JOINS(String name) {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		a.add(name);
		o.put("add", a);
		o.put("leave", new JSONArray());
		o.put("game", new JSONArray());
		return new Message(MessageKind.PLAYERS_JOIN, o);
	}

	@SuppressWarnings("unchecked")
	public static Message PLAYER_LEAVES(String name) {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		a.add(name);
		o.put("add", new JSONArray());
		o.put("leave", a);
		o.put("game", new JSONArray());
		return new Message(MessageKind.PLAYERS_JOIN, o);
	}

	@SuppressWarnings("unchecked")
	public static Message PLAYER_STARTS_GAME(String name) {
		JSONObject o = new JSONObject();
		JSONArray a = new JSONArray();
		a.add(name);
		o.put("add", new JSONArray());
		o.put("leave", new JSONArray());
		o.put("game", a);
		return new Message(MessageKind.PLAYERS_JOIN, o);
	}

	@SuppressWarnings("unchecked")
	public static Message CHALLENGE_PLAYER(String name, JSONObject cmd) {
		JSONObject o = new JSONObject();
		o.put("name", name);
		o.put("cmd", cmd);
		return new Message(MessageKind.CHALLENGE_PLAYER, o);
	}

	/**
	 * @param name The name of the player who's challenge we're accepting
	 * @param player The player accepting the challenge
	 * */
	@SuppressWarnings("unchecked")
	public static Message ACCEPT_CHALLENGE(
		String name, Player player, JSONObject cmd
	) {
		JSONObject o = new JSONObject();
		o.put("name", name);
		o.put("player", player.toString());
		o.put("cmd", cmd);
		return new Message(MessageKind.ACCEPT_CHALLENGE, o);
	}

	@SuppressWarnings("unchecked")
	public static Message REJECT_CHALLENGE(String name) {
		JSONObject o = new JSONObject();
		o.put("name", name);
		return new Message(MessageKind.REJECT_CHALLENGE, o);
	}

	/**
	 * @param player The player that the recipient of this message should play.
	 * */
	@SuppressWarnings("unchecked")
	public static Message START_BATTLE(JSONObject cmd, Player player, String otherPlayer) {
		JSONObject o = new JSONObject();
		o.put("player", player.toString());
		o.put("otherPlayer", otherPlayer);
		o.put("cmd", cmd);
		return new Message(MessageKind.START_BATTLE, o);
	}

	/**
	 * @param name The name of the player who challenged us.
	 * */
	@SuppressWarnings("unchecked")
	public static Message CANCEL_BATTLE(String name) {
		JSONObject o = new JSONObject();
		o.put("name", name);
		return new Message(MessageKind.CANCEL_BATTLE, o);
	}

	public static Message LOGOFF() {
		return new Message(MessageKind.LOGOFF, new JSONObject());
	}

	public static Message WAIT_FOR_RECONNECT() {
		return new Message(MessageKind.WAIT_FOR_RECONNECT, new JSONObject());
	}

	public static Message RECONNECT() {
		return new Message(MessageKind.RECONNECT, new JSONObject());
	}

	public String parseName() throws ProtocolException {
		if (kind != MessageKind.REQUEST_NAME &&
			kind != MessageKind.CHALLENGE_PLAYER &&
			kind != MessageKind.ACCEPT_CHALLENGE &&
			kind != MessageKind.REJECT_CHALLENGE &&
			kind != MessageKind.CANCEL_BATTLE)
				throw new ProtocolException("Expected name");
		try {
			Object v = payload.get("name");
			if (v == null) throw new ProtocolException("Malformed message");
			return (String) v;
		} catch (ClassCastException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	public Player parsePlayer() throws ProtocolException {
		if (kind != MessageKind.ACCEPT_CHALLENGE &&
			kind != MessageKind.START_BATTLE)
				throw new ProtocolException("Expected player");

		try {
			Object player = payload.get("player");
			if (player == null) throw new ProtocolException("Malformed message");
			return Player.fromString((String) player);
		} catch (ClassCastException e) {
			throw new ProtocolException("Malformed message");
		} catch (CorruptDataException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	public JSONObject parseCommand() throws ProtocolException {
		if (kind != MessageKind.ACCEPT_CHALLENGE &&
			kind != MessageKind.COMMAND &&
			kind != MessageKind.START_BATTLE &&
			kind != MessageKind.CHALLENGE_PLAYER)
				throw new ProtocolException("Expected command");

		try {
			Object cmd = payload.get("cmd");
			if (cmd == null) throw new ProtocolException("Malformed message");
			return (JSONObject) cmd;
		} catch (ClassCastException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	public int parseVersion() throws ProtocolException {
		if (kind != MessageKind.C_VERSION && kind != MessageKind.S_VERSION)
			throw new ProtocolException("Expected version");
		try {
			Object v = payload.get("version");
			if (v == null) throw new ProtocolException("Malformed message");
			return ((Number) v).intValue();
		} catch (ClassCastException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	public UUID parseGameDataVersion() throws ProtocolException {
		if (kind != MessageKind.C_VERSION && kind != MessageKind.S_VERSION)
			throw new ProtocolException("Expected version");
		try {
			Object v = payload.get("gdversion");
			if (v == null) throw new ProtocolException("Malformed message");
			return UUID.fromString((String) v);
		} catch (ClassCastException e) {
			throw new ProtocolException("Malformed message");
		} catch (IllegalArgumentException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	public UUID parseSessionKey() throws ProtocolException {
		if (kind != MessageKind.S_VERSION && kind != MessageKind.RECONNECT)
			throw new ProtocolException("Expected session key");
		try {
			Object v = payload.get("session");
			if (v == null) throw new ProtocolException("Malformed message");
			return UUID.fromString((String) v);
		} catch (ClassCastException e) {
			throw new ProtocolException("Malformed message");
		} catch (IllegalArgumentException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	public Collection<String> parseJoinedLobby() throws ProtocolException {
		if (kind != MessageKind.PLAYERS_JOIN)
			throw new ProtocolException("Expected lobby players message");
		try {
			Object a = payload.get("add");
			if (a == null) throw new ProtocolException("Malformed message");
			return jsonArrayToList((JSONArray) a, String.class);
		} catch (ClassCastException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	public Collection<String> parseLeftLobby() throws ProtocolException {
		if (kind != MessageKind.PLAYERS_JOIN)
			throw new ProtocolException("Expected lobby players message");
		try {
			Object a = payload.get("leave");
			if (a == null) throw new ProtocolException("Malformed message");
			return jsonArrayToList((JSONArray) a, String.class);
		} catch (ClassCastException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	public Collection<String> parseJoinedGame() throws ProtocolException {
		if (kind != MessageKind.PLAYERS_JOIN)
			throw new ProtocolException("Expected lobby players message");
		try {
			Object a = payload.get("game");
			if (a == null) throw new ProtocolException("Malformed message");
			return jsonArrayToList((JSONArray) a, String.class);
		} catch (ClassCastException e) {
			throw new ProtocolException("Malformed message");
		}
	}

	public static Message fromString(String message) throws ProtocolException {
		String id = message.substring(0, 2);
		String data = message.substring(2);

		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(data);
			return new Message(MessageKind.fromString(id), json);
		} catch (ParseException e) {
			throw new ProtocolException("Malformed JSON");
		}
	}

	@Override
	public String toString() {
		return kind.toString() + payload.toString() + "\n";
	}

	private static <T> List<T> jsonArrayToList(JSONArray a, Class<T> clazz)
		throws ClassCastException
	{
		List<T> r = new ArrayList<>();
		int limit = a.size();
		for (int i = 0; i < limit; i++) {
			r.add(clazz.cast(a.get(i)));
		}
		return r;
	}
}

