package inthezone.game.battle;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

class Highlighters {
	public final static int HIGHLIGHT_ZONE       = 0;
	public final static int HIGHLIGHT_TARGET     = 1;
	public final static int HIGHLIGHT_MOVE       = 2;
	public final static int HIGHLIGHT_PATH       = 3;
	public final static int HIGHLIGHT_ATTACKAREA = 4;

	public final static Paint[] highlights = new Paint[] {
		Color.rgb(0xFF, 0x00, 0x00, 0.2),
		Color.rgb(0x00, 0x00, 0xFF, 0.2),
		Color.rgb(0x00, 0xFF, 0x00, 0.2),
		Color.rgb(0xFF, 0xFF, 0x00, 0.2),
		Color.rgb(0xFF, 0x88, 0x00, 0.2)
	};
}

