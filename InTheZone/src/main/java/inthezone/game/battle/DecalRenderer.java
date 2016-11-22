package inthezone.game.battle;

import isogame.engine.CameraAngle;
import isogame.engine.Sprite;
import isogame.engine.SpriteDecalRenderer;
import isogame.GlobalConstants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Render extra informational elements associated with characters.
 * */
public class DecalRenderer implements SpriteDecalRenderer {
	private final BattleView view;
	public DecalRenderer(BattleView view) {
		this.view = view;
	}

	// The selection arrow
	private final Color sarrowColor = Color.rgb(0x00, 0xFF, 0x00, 0.9);
	private final double[] sarrowx = new double[] {
		GlobalConstants.TILEW * 3.0/8.0,
		GlobalConstants.TILEW * 5.0/8.0,
		GlobalConstants.TILEW / 2.0};
	private final double[] sarrowy = new double[] {
		GlobalConstants.TILEW * (-1.0)/8.0,
		GlobalConstants.TILEW * (-1.0)/8.0,
		0};
	
	@Override public void render(
		GraphicsContext cx, Sprite s, long t, CameraAngle angle
	) {
		view.getSelectedCharacter().ifPresent(c -> {
			if (s.userData.equals(c.id)) {
				cx.setFill(sarrowColor);
				cx.fillPolygon(sarrowx, sarrowy, 3);
			}
		});
	}
}

