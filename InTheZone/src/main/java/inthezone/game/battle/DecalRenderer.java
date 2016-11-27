package inthezone.game.battle;

import inthezone.battle.Character;
import isogame.engine.CameraAngle;
import isogame.engine.MapPoint;
import isogame.engine.Sprite;
import isogame.engine.SpriteDecalRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;
import static isogame.GlobalConstants.TILEH;
import static isogame.GlobalConstants.TILEW;

/**
 * Render extra informational elements associated with characters.
 * */
public class DecalRenderer implements SpriteDecalRenderer {
	private final BattleView view;
	private Map<Integer, HealthBar> healthBars = new HashMap<>();

	public DecalRenderer(BattleView view) {
		this.view = view;
	}

	public void registerCharacter(Character c) {
		healthBars.put(c.id, new HealthBar(c.getHP(), c.getMaxHP()));
	}

	public void updateCharacter(Character c) {
		HealthBar h = healthBars.get(c.id);
		if (h != null) h.updateHP(c.getHP(), c.getMaxHP());
	}

	// The selection arrow
	private final Color sarrowColor = Color.rgb(0x00, 0xFF, 0x00, 0.9);
	private final double[] sarrowx = new double[] {
		TILEW * 3.0/8.0, TILEW * 5.0/8.0, TILEW / 2.0};
	private final double[] sarrowy = new double[] {
		TILEH * -0.2, TILEH * -0.2, TILEH * -0.06};
	
	@Override public void render(
		GraphicsContext cx, Sprite s, long t, CameraAngle angle
	) {
		view.getSelectedCharacter().ifPresent(c -> {
			if (s.userData.equals(c.id)) {
				cx.setFill(sarrowColor);
				cx.fillPolygon(sarrowx, sarrowy, 3);
			}
		});

		HealthBar h = healthBars.get(s.userData);
		if (h != null) h.render(cx, s, t);
	}

	public void handleMouseOver(MapPoint p) {
		view.sprites.getCharacterAt(p).ifPresent(c -> {
			HealthBar h = healthBars.get(c.id);
			if (h != null) h.show();
		});
	}

	public void handleMouseOut() {
		for (HealthBar h : healthBars.values()) h.hide();
	}
}

