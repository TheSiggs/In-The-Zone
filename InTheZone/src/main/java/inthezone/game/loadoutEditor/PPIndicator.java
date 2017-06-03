package inthezone.game.loadoutEditor;

import inthezone.battle.data.Loadout;
import javafx.beans.property.IntegerProperty;
import javafx.scene.control.Label;

public class PPIndicator extends Label {
	public PPIndicator(IntegerProperty cost) {
		this.setText("Power points: 0 / " + Loadout.maxPP);
		this.getStyleClass().add("gui-textfield");

		cost.addListener((v, o, n) -> {
			this.setText("Power points: " + n + " / " + Loadout.maxPP);
		});
	}
}

