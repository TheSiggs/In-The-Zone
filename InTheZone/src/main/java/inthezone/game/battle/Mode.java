package inthezone.game.battle;

/**
 * A GUI mode.
 * */
public abstract class Mode {
	private abstract void handleSelection(MapPoint p);
	private abstract void handleMouseOver(MapPoint p);
	private abstract void handleMouseOut();
}

