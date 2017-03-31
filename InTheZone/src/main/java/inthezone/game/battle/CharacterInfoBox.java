package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.StatusEffectType;
import inthezone.battle.status.StatusEffect;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.util.Optional;

/**
 * Displays information about a character during battle.
 * */
public class CharacterInfoBox extends AnchorPane {
	public final int id;

	private final ImageView portrait;
	private final GridPane grid = new GridPane();
	private final ProgressBar ap = new ProgressBar(1);
	private final ProgressBar mp = new ProgressBar(1);
	private final ProgressBar hp = new ProgressBar(1);

	private final Label nap = new Label("/");
	private final Label nmp = new Label("/");
	private final Label nhp = new Label("/");

	private final HBox statusLine = new HBox();

	private boolean isSelected = false;

	private final StandardSprites sprites;

	public CharacterInfoBox(Character character, StandardSprites sprites) {
		super();

		this.sprites = sprites;

		this.id = character.id;

		portrait = new ImageView(character.portrait);

		this.getStyleClass().add("character-info-box");
		this.getChildren().addAll(portrait, grid);
		this.setPrefWidth(162);
		this.setPrefHeight(274);
		this.setMaxWidth(this.getPrefWidth());
		this.setMaxHeight(this.getPrefHeight());

		AnchorPane.setTopAnchor(portrait, 0d);
		AnchorPane.setLeftAnchor(portrait, 0d);
		AnchorPane.setRightAnchor(portrait, 0d);

		AnchorPane.setBottomAnchor(grid, 0d);
		AnchorPane.setLeftAnchor(grid, 0d);
		AnchorPane.setRightAnchor(grid, 0d);

		grid.addRow(0, new Label("ap"), new StackPane(ap, nap));
		grid.addRow(1, new Label("mp"), new StackPane(mp, nmp));
		grid.addRow(2, new Label("hp"), new StackPane(hp, nhp));
		grid.add(statusLine, 3, 0, 2, 1);

		ap.setMouseTransparent(true);
		mp.setMouseTransparent(true);
		hp.setMouseTransparent(true);

		updateAP(character.getAP(), character.getStats().ap);
		updateMP(character.getMP(), character.getStats().mp);
		updateHP(character.getHP(), character.getMaxHP());
	}

	private void updateAP(int ap, int max) {
		this.ap.setProgress((double) ap / (double) max);
		this.nap.setText(ap + " / " + max);
	}

	private void updateMP(int mp, int max) {
		this.mp.setProgress((double) mp / (double) max);
		this.nmp.setText(mp + " / " + max);
	}

	private void updateHP(int hp, int max) {
		this.hp.setProgress((double) hp / (double) max);
		this.nhp.setText(hp + " / " + max);
	}

	private void updateStatus(
		Optional<StatusEffect> buff, Optional<StatusEffect> debuff, boolean cover
	) {
		statusLine.getChildren().clear();
		buff.ifPresent(s -> statusLine.getChildren().add(
			new ImageView(sprites.statusEffects.get(s.getInfo().type))));
		debuff.ifPresent(s -> statusLine.getChildren().add(
			new ImageView(sprites.statusEffects.get(s.getInfo().type))));
		if (cover) statusLine.getChildren().add(
			new ImageView(sprites.statusEffects.get(StatusEffectType.COVER)));
	}

	public void updateCharacter(Character c) {
		updateAP(c.getAP(), c.getStats().ap);
		updateMP(c.getMP(), c.getStats().mp);
		updateHP(c.getHP(), c.getMaxHP());
		updateStatus(c.getStatusBuff(), c.getStatusDebuff(), c.hasCover());
	}

	public void setSelected(boolean isSelected) {
		if (this.isSelected != isSelected) {
			this.isSelected = isSelected;
			if (isSelected) {
				this.getStyleClass().add("character-info-box-selected");
			} else {
				this.getStyleClass().remove("character-info-box-selected");
			}
		}
	}
}

