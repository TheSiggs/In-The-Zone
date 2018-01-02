package inthezone.battle.data;

/**
 * Game players.
 * */
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
}

