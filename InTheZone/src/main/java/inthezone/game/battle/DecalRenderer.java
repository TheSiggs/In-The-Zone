package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.data.StandardSprites;
import isogame.engine.CameraAngle;
import isogame.engine.MapPoint;
import isogame.engine.Sprite;
import isogame.engine.SpriteDecalRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static isogame.GlobalConstants.TILEH;
import static isogame.GlobalConstants.TILEW;
import javafx.scene.image.Image;

/**
 * Render extra informational elements associated with characters.
 * */
public class DecalRenderer implements SpriteDecalRenderer {
	private final BattleView view;
	private final Map<Integer, HealthBar> healthBars = new HashMap<>();
	private final Set<Integer> covered = new HashSet<>();
	private final Set<Integer> enemies = new HashSet<>();
	private final StandardSprites sprites;
	private static final double BUFF_FACTOR = 4.0;
	private static final double BUFF_X_OFFSET = -80.0;
	private static final double DEBUFF_X_OFFSET = 50.0;

	public DecalRenderer(BattleView view, StandardSprites sprites) {
		this.view = view;
		this.sprites = sprites;
	}

	public void registerCharacter(Character c) {
		final HealthBar healthBar = new HealthBar(c.getHP(), c.getMaxHP());
		healthBar.show();
		healthBars.put(c.id, healthBar);
		if (c.player != view.player) enemies.add(c.id);
	}

	public void updateCharacter(Character c) {
		final HealthBar h = healthBars.get(c.id);
		if (h != null) h.updateHP(c.getHP(), c.getMaxHP());
		if (c.hasCover()) covered.add(c.id); else covered.remove(c.id);
	}

	// The selection arrow
	private final Color sarrowColor = Color.rgb(0x00, 0xFF, 0x00, 0.9);
	private final double[] sarrowx = new double[] {
		TILEW * 3.0/8.0, TILEW * 5.0/8.0, TILEW / 2.0};
	private final double[] sarrowy = new double[] {
		TILEH * -0.2, TILEH * -0.2, TILEH * -0.06};

	private final static double X = TILEW * 0.45;
	private final static double Y = TILEH * 0.25;

	private static final Font markFont = new Font(48d);
	
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

		final Character character = view.sprites.getCharacterById((int) s.userData);
		if (!character.hasCover()) { // @callum1: See comment @ https://gitlab.com/inthezone/in-the-zone/issues/102
			character.getStatusBuff().ifPresent((buff) -> {
				final Image effect = sprites.statusEffects.get(buff.info.type);
				cx.drawImage(effect, X + BUFF_X_OFFSET, Y, effect.getWidth() * BUFF_FACTOR, effect.getHeight() * BUFF_FACTOR);
			});
			character.getStatusDebuff().ifPresent((buff) -> {
				final Image effect = sprites.statusEffects.get(buff.info.type);
				cx.drawImage(effect, X + DEBUFF_X_OFFSET, Y, effect.getWidth() * BUFF_FACTOR, effect.getHeight() * BUFF_FACTOR);
			});
		}
		if (enemies.contains(s.userData)) {
			String m = covered.contains(s.userData)? "E(C)" : "E";
			cx.setFill(Color.RED);
			cx.setFont(markFont);
			cx.fillText(m, X, Y);
		}
	}

	public void handleMouseOver(MapPoint p) {
		view.sprites.getCharacterAt(p).ifPresent(c -> {
			HealthBar h = healthBars.get(c.id);
			if (h != null) h.show();
		});
	}

	public void handleMouseOut() {
		//for (HealthBar h : healthBars.values()) h.hide();
	}
}

