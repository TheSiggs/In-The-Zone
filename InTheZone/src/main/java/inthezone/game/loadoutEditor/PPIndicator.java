package inthezone.game.loadoutEditor;

import inthezone.battle.data.Loadout;
import javafx.beans.property.IntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Tooltip;

public class PPIndicator extends StackPane {
	private final Label label = new Label();

	public PPIndicator(IntegerProperty cost) {
		label.setText("Power points: 0 / " + Loadout.maxPP);
		label.getStyleClass().add("gui-textfield");

		String tooltipMessage = 
			"You spend power points when you equip abilities,\n" +
			"or when you add extra health, attack, or defence points";
		Tooltip.install(this, new Tooltip(tooltipMessage));

		this.getChildren().add(label);

		cost.addListener((v, o, n) -> {
			label.setText("Power points: " + n + " / " + Loadout.maxPP);
			if (n.intValue() > Loadout.maxPP) {
				Tooltip.install(this, new Tooltip(tooltipMessage + "\n\n" +
					"You have spent too many power points.  You must\n" +
					"remove some abilities or reduce your stats."));
			}
		});
	}
}

