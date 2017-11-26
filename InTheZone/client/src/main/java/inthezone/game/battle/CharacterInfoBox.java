package inthezone.game.battle;

import java.util.Optional;

import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import inthezone.battle.CharacterFrozen;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.StatusEffectDescription;
import inthezone.battle.status.StatusEffect;

/**
 * Displays information about a character during battle.
 * */
public class CharacterInfoBox extends AnchorPane {
	private static final double STATUS_LINE_HEIGHT = 32;
	public final int id;

	private final ImageView portrait;
	private final ImageView selectedImage = new ImageView();
	private final VBox grid = new VBox(4);
	private final StatBar ap = new StatBar("ap", false);
	private final StatBar mp = new StatBar("mp", false);
	private final StatBar hp = new StatBar("hp", true);

	private final HBox statusLine = new HBox();

	private boolean isSelected = false;

	public boolean isSelected() { return isSelected; }

	private final StandardSprites sprites;

	private final ColorAdjust deadEffect = new ColorAdjust();

	/**
	 * @param character the character to display
	 * @param sprites the standard sprites set (for icons and the like)
	 * */
	public CharacterInfoBox(
		final CharacterFrozen character,
		final StandardSprites sprites
	) {
		this.sprites = sprites;
		this.id = character.getId();

		final Tooltip ttap = new Tooltip("Action points: you spend action points when you attack, push, use a potion, or do an ability");
		final Tooltip ttmp = new Tooltip("Movement points: every time you move one square, it costs you one movement point");
		final Tooltip tthp = new Tooltip("Health points: when they get to zero your character faints");

		ttap.setWrapText(true); ttap.setMaxWidth(300);
		ttmp.setWrapText(true); ttmp.setMaxWidth(300);
		tthp.setWrapText(true); tthp.setMaxWidth(300);

		Tooltip.install(ap, ttap);
		Tooltip.install(mp, ttmp);
		Tooltip.install(hp, tthp);

		portrait = new ImageView(character.getPortrait());

		selectedImage.setId("selectedCharacterImage");
		selectedImage.setVisible(false);

		this.getStyleClass().add("character-info-box");
		this.getStyleClass().add("character-info-box" +
			(character.getPlayer() == Player.PLAYER_A? "A" : "B"));
		this.getChildren().addAll(portrait, grid, selectedImage);
		this.setPrefWidth(162);
		this.setPrefHeight(274);
		this.setMaxWidth(this.getPrefWidth());
		this.setMaxHeight(this.getPrefHeight());

		this.portrait.setEffect(deadEffect);
		this.ap.setEffect(deadEffect);
		this.mp.setEffect(deadEffect);
		this.hp.setEffect(deadEffect);

		AnchorPane.setTopAnchor(selectedImage, 4d);
		AnchorPane.setRightAnchor(selectedImage, 4d);

		AnchorPane.setTopAnchor(portrait, 0d);
		AnchorPane.setLeftAnchor(portrait, 0d);
		AnchorPane.setRightAnchor(portrait, 0d);

		AnchorPane.setBottomAnchor(grid, 0d);
		AnchorPane.setLeftAnchor(grid, 0d);
		AnchorPane.setRightAnchor(grid, 0d);

		statusLine.setId("statusLine");
		statusLine.setMinHeight(STATUS_LINE_HEIGHT);
		statusLine.setAlignment(Pos.CENTER);
		statusLine.setFillHeight(false);

		grid.setAlignment(Pos.CENTER);
		grid.getChildren().addAll(hp, ap, mp, statusLine);

		updateCharacter(character);
	}

	/**
	 * Update the character status effects info.
	 * @param c a new version of the character
	 * @param buff the character's buff
	 * @param debuff the character's debuff
	 * */
	private void updateStatus(
		final CharacterFrozen c,
		final Optional<StatusEffect> buff,
		final Optional<StatusEffect> debuff
	) {
		statusLine.getChildren().clear();
		buff.ifPresent(s -> statusLine.getChildren().add(statusEffectImage(s)));
		debuff.ifPresent(s -> statusLine.getChildren().add(statusEffectImage(s)));
		if (c.getRevengeBonus() != 0) {
			statusLine.getChildren().add(revengeImage(c));
		}

		deadEffect.setSaturation(c.isDead()? -1.0d : 0d);
		if (c.isDead()) {
			this.getStyleClass().remove("character-info-box-dead");
			this.getStyleClass().add("character-info-box-dead");
		} else {
			this.getStyleClass().remove("character-info-box-dead");
		}
	}

	/**
	 * Get the icon to use for a status effect.
	 * @param s the status effect
	 * */
	private ImageView statusEffectImage(final StatusEffect s) {
		final ImageView r = new ImageView(
			sprites.statusEffects.get(s.getInfo().type));
		final Tooltip t = new Tooltip((new StatusEffectDescription(
			s.getInfo())).toString());
		t.setWrapText(true);
		t.setMaxWidth(300);
		Tooltip.install(r, t);
		return r;
	}

	/**
	 * Get the revenge image.
	 * @param c the character that has revenge
	 * */
	private ImageView revengeImage(final CharacterFrozen c) {
		final ImageView r = new ImageView(sprites.revengeIcon);
		final Tooltip t = new Tooltip(c.getName() + " is angry!\n" +
			c.getName() + " gets a revenge bonus of " +
			Math.round(c.getRevengeBonus() * 100) + "%");
		t.setWrapText(true);
		t.setMaxWidth(300);
		Tooltip.install(r, t);
		return r;
	}

	/**
	 * Update the character info.
	 * @param c a new version of the character.
	 * */
	public void updateCharacter(final CharacterFrozen c) {
		ap.update(c.hasMana()? "ap_mana" : "ap", c.getAP(), c.getStats().ap, false);
		mp.update("mp", c.getMP(), c.getStats().mp, false);
		hp.update(c.hasCover()? "hp_cover" : "hp",
			c.getHP(), c.getMaxHP(), c.hasCover());
		updateStatus(c, c.getStatusBuff(), c.getStatusDebuff());
	}

	/**
	 * Set whether or not this character is selected.
	 * @param isSelected true if selected, otherwise false
	 * */
	public void setSelected(final boolean isSelected) {
		if (this.isSelected != isSelected) {
			this.isSelected = isSelected;
			selectedImage.setVisible(isSelected);
			if (isSelected) {
				this.getStyleClass().add("character-info-box-selected");

				final RotateTransition rt =
					new RotateTransition(Duration.millis(1000), selectedImage);
				rt.setFromAngle(0);
				rt.setByAngle(-360);
				rt.setCycleCount(1);
				rt.setAutoReverse(false);
				rt.play();
			} else {
				this.getStyleClass().remove("character-info-box-selected");
			}
		}
	}
}

