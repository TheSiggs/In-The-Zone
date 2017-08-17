package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.StatusEffectDescription;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
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

	final private Tooltip buffTooltip = new Tooltip();
	final private Tooltip debuffTooltip = new Tooltip();

	private final Color sarrowColor = Color.rgb(0x00, 0xFF, 0x00, 0.9f);

	private static final double BUFF_SCALE = 1.5d;

	private static final double BUFF_X = 34d;
	private static final double DEBUFF_X = 34d;

	private final static double BUFF_Y = -20d;
	private final static double DEBUFF_Y = 18d;

	private final static double DECAL_Y = -88d;

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

		buff.setScaleX(BUFF_SCALE);
		buff.setScaleY(BUFF_SCALE);
		buff.setTranslateX(BUFF_X);
		buff.setTranslateY(BUFF_Y);

		debuff.setScaleX(BUFF_SCALE);
		debuff.setScaleY(BUFF_SCALE);
		debuff.setTranslateX(DEBUFF_X);
		debuff.setTranslateY(DEBUFF_Y);

		Tooltip.install(buff, buffTooltip);
		Tooltip.install(debuff, debuffTooltip);

		buff.setMouseTransparent(false);
		buffTooltip.setWrapText(true);
		buffTooltip.setPrefWidth(300);

		debuff.setMouseTransparent(false);
		debuffTooltip.setWrapText(true);
		debuffTooltip.setPrefWidth(300);

		this.setTranslateY(DECAL_Y);
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
			buffTooltip.setText(new StatusEffectDescription(
				effect.getInfo()).toString());
			graph.add(buff);
		});

		c.getStatusDebuff().ifPresent(effect -> {
			debuff.setImage(sprites.statusEffects.get(effect.info.type));
			debuffTooltip.setText(new StatusEffectDescription(
				effect.getInfo()).toString());
			graph.add(debuff);
		});
	}
}

