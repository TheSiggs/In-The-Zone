package inthezone.game.battle;

import inthezone.battle.Character;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;

/**
 * Displays information about a character during battle.
 * */
public class CharacterInfoBox extends GridPane {
	public final int id;

	private final ProgressBar ap = new ProgressBar(1);
	private final ProgressBar mp = new ProgressBar(1);
	private final ProgressBar hp = new ProgressBar(1);

	private boolean isSelected = false;

	public CharacterInfoBox(Character character) {
		super();

		this.id = character.id;

		this.getStyleClass().add("character-info-box");

		this.add(new Label(character.name), 0, 0, 2, 1);
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

	public void updateAP(int ap, int max) {
		this.ap.setProgress((double) ap / (double) max);
	}

	public void updateMP(int mp, int max) {
		this.mp.setProgress((double) mp / (double) max);
	}

	public void updateHP(int hp, int max) {
		this.hp.setProgress((double) hp / (double) max);
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
