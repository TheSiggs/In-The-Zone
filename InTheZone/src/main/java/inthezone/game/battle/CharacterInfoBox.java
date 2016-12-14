package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.StatusEffectType;
import inthezone.battle.status.StatusEffect;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import java.util.Optional;

/**
 * Displays information about a character during battle.
 * */
public class CharacterInfoBox extends GridPane {
	public final int id;

	private final Label name = new Label("");
	private final HBox nameLine = new HBox();
	private final ProgressBar ap = new ProgressBar(1);
	private final ProgressBar mp = new ProgressBar(1);
	private final ProgressBar hp = new ProgressBar(1);

	private boolean isSelected = false;

	private final StandardSprites sprites;

	public CharacterInfoBox(Character character, StandardSprites sprites) {
		super();

		this.sprites = sprites;

		this.id = character.id;

		this.getStyleClass().add("character-info-box");

		name.setText(character.name);
		nameLine.getChildren().add(name);
		this.add(nameLine, 0, 0, 2, 1);
		this.addRow(1, new Label("ap"), ap);
		this.addRow(2, new Label("mp"), mp);
		this.addRow(3, new Label("hp"), hp);

		ap.setMouseTransparent(true);
		mp.setMouseTransparent(true);
		hp.setMouseTransparent(true);

		updateAP(character.getAP(), character.getStats().ap);
		updateMP(character.getMP(), character.getStats().mp);
		updateHP(character.getHP(), character.getMaxHP());
	}

	private void updateAP(int ap, int max) {
		this.ap.setProgress((double) ap / (double) max);
	}

	private void updateMP(int mp, int max) {
		this.mp.setProgress((double) mp / (double) max);
	}

	private void updateHP(int hp, int max) {
		this.hp.setProgress((double) hp / (double) max);
	}

	private void updateStatus(
		Optional<StatusEffect> buff, Optional<StatusEffect> debuff, boolean cover
	) {
		nameLine.getChildren().clear();
		nameLine.getChildren().add(name);
		buff.ifPresent(s -> nameLine.getChildren().add(
			new ImageView(sprites.statusEffects.get(s.getInfo().type))));
		debuff.ifPresent(s -> nameLine.getChildren().add(
			new ImageView(sprites.statusEffects.get(s.getInfo().type))));
		if (cover) nameLine.getChildren().add(
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

