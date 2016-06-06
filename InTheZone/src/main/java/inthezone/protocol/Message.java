package inthezone.protocol;

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
	public static Message NAME(String name) {
		JSONObject o = new JSONObject();
		o.put("name" , name);
		return new Message(MessageKind.REQUEST_NAME, o);
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
			kind != MessageKind.REJECT_CHALLENGE)
				throw new ProtocolException("Expected name");
		try {
			Object v = payload.get("name");
			if (v == null) throw new ProtocolException("Malformed message");
			return (String) v;
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
		if (kind != MessageKind.C_VERSION && kind != MessageKind.RECONNECT)
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

	public String toString() {
		return kind.toString() + payload.toString();
	}
}

