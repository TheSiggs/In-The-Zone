package inthezone.game.battle;

import java.util.Optional;

import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import inthezone.battle.Character;
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

	private final StandardSprites sprites;

	public CharacterInfoBox(
		final Character character,
		final StandardSprites sprites
	) {
		this.sprites = sprites;
		this.id = character.id;

		Tooltip.install(ap, new Tooltip("Action points: you spend action points when you attack, push, use a potion, or do an ability"));
		Tooltip.install(mp, new Tooltip("Movement points: every time you move one square, it costs you one movement point"));
		Tooltip.install(hp, new Tooltip("Health points: when they get to zero your character faints"));

		portrait = new ImageView(character.portrait);

		selectedImage.setId("selectedCharacterImage");
		selectedImage.setVisible(false);

		this.getStyleClass().add("character-info-box");
		this.getChildren().addAll(portrait, grid, selectedImage);
		this.setPrefWidth(162);
		this.setPrefHeight(274);
		this.setMaxWidth(this.getPrefWidth());
		this.setMaxHeight(this.getPrefHeight());

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

	private void updateStatus(
		final Character c,
		final Optional<StatusEffect> buff,
		final Optional<StatusEffect> debuff
	) {
		statusLine.getChildren().clear();
		buff.ifPresent(s -> statusLine.getChildren().add(statusEffectImage(s)));
		debuff.ifPresent(s -> statusLine.getChildren().add(statusEffectImage(s)));
		if (c.getRevengeBonus() != 0) {
			statusLine.getChildren().add(revengeImage(c));
		}
	}

	private ImageView statusEffectImage(final StatusEffect s) {
		final ImageView r = new ImageView(
			sprites.statusEffects.get(s.getInfo().type));
		final Tooltip t = new Tooltip((new StatusEffectDescription(
			s.getInfo())).toString());
		t.setWrapText(true);
		t.setPrefWidth(300);
		Tooltip.install(r, t);
		return r;
	}

	private ImageView revengeImage(final Character c) {
		final ImageView r = new ImageView(sprites.revengeIcon);
		final Tooltip t = new Tooltip(c.name + " is angry!\n" +
			c.name + " gets a revenge bonus of " +
			Math.round(c.getRevengeBonus() * 100) + "%");
		t.setWrapText(true);
		t.setPrefWidth(300);
		Tooltip.install(r, t);
		return r;
	}

	public void updateCharacter(final Character c) {
		ap.update(c.hasMana()? "ap_mana" : "ap", c.getAP(), c.getStats().ap, false);
		mp.update("mp", c.getMP(), c.getStats().mp, false);
		hp.update(c.hasCover()? "hp_cover" : "hp",
			c.getHP(), c.getMaxHP(), c.hasCover());
		updateStatus(c, c.getStatusBuff(), c.getStatusDebuff());
	}

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

