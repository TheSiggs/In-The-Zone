package inthezone.game.loadoutEditor;

import inthezone.battle.data.Loadout;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

public class PPIndicator extends StackPane {
	private final Label label = new Label();

	final String tooltipMessage =
		"You spend power points when you equip abilities,\n" +
		"or when you add extra health, attack, or defence points";

	public PPIndicator(IntegerProperty cost) {
		label.setText("Power points: 0 / " + Loadout.maxPP);
		label.getStyleClass().add("gui-textfield");

		Tooltip.install(this, new Tooltip(tooltipMessage));

		this.getChildren().add(label);
		setCostProperty(cost);
	}

	private ChangeListener<Number> updatePP = new ChangeListener<Number>() {
		@Override public void changed(
			ObservableValue<? extends Number> v, Number o, Number n
		) {
			updatePP(n.intValue());
		}
	};

	private void updatePP(int n) {
		label.setText("Power points: " + n + " / " + Loadout.maxPP);
		if (n > Loadout.maxPP) {
			Tooltip.install(this, new Tooltip(tooltipMessage + "\n\n" +
				"You have spent too many power points.  You must\n" +
				"remove some abilities or reduce your stats."));
		}
	}

	public void setCostProperty(IntegerProperty cost) {
		cost.removeListener(updatePP);
		cost.addListener(updatePP);
		updatePP(cost.get());
	}
}

