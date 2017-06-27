package inthezone.battle.data;

import isogame.engine.CorruptDataException;

public enum Player {
	PLAYER_A, PLAYER_B, PLAYER_OBSERVER;

	public Player otherPlayer() {
		switch(this) {
			case PLAYER_A: return PLAYER_B;
			case PLAYER_B: return PLAYER_A;
			case PLAYER_OBSERVER: return PLAYER_OBSERVER;
			default:
				throw new RuntimeException("Invalid player, this cannot happen");
		}
	}

	public static Player randomPlayer() {
		final int pn = (int) Math.floor(Math.random() * 2.0d);
		return pn == 0 ? PLAYER_A : PLAYER_B;
	}

	public static Player fromString(final String s) throws CorruptDataException {
		if (s.equals("Player1")) {
			return PLAYER_A;
		} else if(s.equals("Player2")) {
			return PLAYER_B;
		} else if (s.equals("observer")) {
			return PLAYER_OBSERVER;
		} else {
			throw new CorruptDataException("Invalid player " + s);
		}
	}

	@Override
	public String toString() {
		switch(this) {
			case PLAYER_A: return "Player1";
			case PLAYER_B: return "Player2";
			case PLAYER_OBSERVER: return "observer";
			default:
				throw new RuntimeException("Invalid player, this cannot happen");
		}
	}
}

