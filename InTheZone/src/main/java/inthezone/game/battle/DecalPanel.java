package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.data.StandardSprites;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import static isogame.GlobalConstants.TILEH;
import static isogame.GlobalConstants.TILEW;

/**
 * A panel of information about character
 * */
public class DecalPanel extends Group {
	final private StandardSprites sprites;

	final private ImageView buff = new ImageView();
	final private ImageView debuff = new ImageView();
	final private HealthBar healthBar = new HealthBar();
	final private Shape selectionIndicator;

	private final Color sarrowColor = Color.rgb(0x00, 0xFF, 0x00, 0.9);

	private static final double BUFF_SCALE = 2.0;
	private static final double BUFF_X_OFFSET = -40.0;
	private static final double DEBUFF_X_OFFSET = 25.0;

	private final static double BUFF_Y = -20;
	private final static double BUFF_X = BUFF_X_OFFSET;
	private final static double DEBUFF_X = DEBUFF_X_OFFSET;

	public DecalPanel(final StandardSprites sprites) {
		this.sprites = sprites;

		final Polygon si = new Polygon();
		si.getPoints().addAll(new Double[]{
			TILEW * 3.0/8.0, TILEH * -0.2,
			TILEW * 5.0/8.0, TILEH * -0.2,
			TILEW / 2.0,     TILEH * -0.06
		});
		selectionIndicator = si;
		selectionIndicator.setFill(sarrowColor);
	}

	public void updateSelectionStatus(final boolean isSelected) {
		final ObservableList<Node> graph = this.getChildren();
		graph.remove(selectionIndicator);
		if (isSelected) graph.add(selectionIndicator);
	}

	/**
	 * Update this decal panel
	 * */
	public void updateCharacter(final Character c, final boolean isSelected) {
		final ObservableList<Node> graph = this.getChildren();
		graph.clear();

		if (isSelected) graph.add(selectionIndicator);

		healthBar.updateHP(c.getHP(), c.getMaxHP(), c.hasCover());
		graph.add(healthBar);

		c.getStatusBuff().ifPresent(effect -> {
			buff.setImage(sprites.statusEffects.get(effect.info.type));
			graph.add(buff);
		});

		c.getStatusDebuff().ifPresent(effect -> {
			debuff.setImage(sprites.statusEffects.get(effect.info.type));
			graph.add(debuff);
		});
	}
}

