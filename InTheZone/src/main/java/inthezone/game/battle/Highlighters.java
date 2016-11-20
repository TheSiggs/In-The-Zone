package inthezone.game.battle;

import isogame.engine.Highlighter;
import javafx.scene.paint.Color;

class Highlighters {
	public final static int HIGHLIGHT_ZONE       = 0;
	public final static int HIGHLIGHT_TARGET     = 1;
	public final static int HIGHLIGHT_MOVE       = 2;
	public final static int HIGHLIGHT_PATH       = 3;
	public final static int HIGHLIGHT_ATTACKAREA = 4;

	public final static Highlighter[] highlights = new Highlighter[] {
		new Highlighter(Color.rgb(0xFF, 0x88, 0x00, 0.2)),
		new Highlighter(Color.rgb(0xFF, 0xFF, 0x00, 0.2)),
		new Highlighter(Color.rgb(0x00, 0xFF, 0x00, 0.2)),
		new Highlighter(Color.rgb(0x00, 0x00, 0xFF, 0.2)),
		new Highlighter(Color.rgb(0xFF, 0x00, 0x00, 0.2))};
}

