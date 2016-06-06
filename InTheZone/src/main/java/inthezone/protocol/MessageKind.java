package inthezone.protocol;

public enum MessageKind {
	S_VERSION, C_VERSION, OK, NOK, GAME_DATA, REQUEST_NAME, PLAYERS_JOIN,
	CHALLENGE_PLAYER, ACCEPT_CHALLENGE, REJECT_CHALLENGE, COMMAND,
	GAME_OVER, LOGOFF, RECONNECT, WAIT_FOR_RECONNECT;

	// NOTE: REJECT_CHALLENGE can happen during a game, in which case it probably
	// should not be reported.  Or perhaps the reporting could be delayed until
	// the player returns to the lobby.

	public static MessageKind fromString(String in) throws ProtocolException {
		switch (in) {
			case "SV": return S_VERSION;
			case "CV": return C_VERSION;
			case "OK": return OK;
			case "NK": return NOK;
			case "GD": return GAME_DATA;
			case "RN": return REQUEST_NAME;
			case "PJ": return PLAYERS_JOIN;
			case "CP": return CHALLENGE_PLAYER;
			case "AC": return ACCEPT_CHALLENGE;
			case "RC": return REJECT_CHALLENGE;
			case "CM": return COMMAND;
			case "GO": return GAME_OVER;
			case "LO": return LOGOFF;
			case "RT": return RECONNECT;
			case "WR": return WAIT_FOR_RECONNECT;
			default:
				throw new ProtocolException("Bad message kind " + in);
		}
	}

	public String toString() {
		switch (this) {
			case S_VERSION: return "SV";
			case C_VERSION: return "CV";
			case OK: return "OK";
			case NOK: return "NK";
			case GAME_DATA: return "GD";
			case REQUEST_NAME: return "RN";
			case PLAYERS_JOIN: return "PJ";
			case CHALLENGE_PLAYER: return "CP";
			case ACCEPT_CHALLENGE: return "AC";
			case REJECT_CHALLENGE: return "RC";
			case COMMAND: return "CM";
			case GAME_OVER: return "GO";
			case LOGOFF: return "LO";
			case RECONNECT: return "RT";
			case WAIT_FOR_RECONNECT: return "WR";
			default:
				throw new RuntimeException("This cannot happen");
		}
	}
}

