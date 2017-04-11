package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.StatusEffectType;
import inthezone.battle.status.StatusEffect;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.Optional;

/**
 * Displays information about a character during battle.
 * */
public class CharacterInfoBox extends AnchorPane {
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

	public CharacterInfoBox(Character character, StandardSprites sprites) {
		super();

		this.sprites = sprites;

		this.id = character.id;

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

		grid.setAlignment(Pos.CENTER);
		grid.getChildren().addAll(ap, mp, hp, statusLine);

		ap.setMouseTransparent(true);
		mp.setMouseTransparent(true);
		hp.setMouseTransparent(true);

		ap.update(character.hasMana()? "ap_mana" : "ap", character.getAP(), character.getStats().ap, false);
		mp.update("mp", character.getMP(), character.getStats().mp, false);
		hp.update(character.hasCover()? "hp_cover" : "hp", character.getHP(), character.getMaxHP(), character.hasCover());
	}

	private void updateStatus(
		Optional<StatusEffect> buff, Optional<StatusEffect> debuff
	) {
		statusLine.getChildren().clear();
		buff.ifPresent(s -> statusLine.getChildren().add(
			new ImageView(sprites.statusEffects.get(s.getInfo().type))));
		debuff.ifPresent(s -> statusLine.getChildren().add(
			new ImageView(sprites.statusEffects.get(s.getInfo().type))));
	}

	public void updateCharacter(Character c) {
		ap.update(c.hasMana()? "ap_mana" : "ap", c.getAP(), c.getStats().ap, false);
		mp.update("mp", c.getMP(), c.getStats().mp, false);
		hp.update(c.hasCover()? "hp_cover" : "hp", c.getHP(), c.getMaxHP(), c.hasCover());
		updateStatus(c.getStatusBuff(), c.getStatusDebuff());
	}

	public void setSelected(boolean isSelected) {
		if (this.isSelected != isSelected) {
			this.isSelected = isSelected;
			selectedImage.setVisible(isSelected);
			if (isSelected) {
				this.getStyleClass().add("character-info-box-selected");

				RotateTransition rt = new RotateTransition(Duration.millis(1000), selectedImage);
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

