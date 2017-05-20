package inthezone.game.battle;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

public class RoundCounter extends StackPane {
	private int round = 0;
	private boolean fatigue = false;

	private final Label label = new Label("Round: 0");
	private final Tooltip tooltip = new Tooltip();

	public RoundCounter() {
		this.setId("rounds");
		this.getStyleClass().add("rounds_indicator");
		this.getChildren().add(label);
		this.setPrefWidth(114);
		this.setPrefHeight(40);
		Tooltip.install(this, tooltip);
	}

	public void increment() {
		round += 1;
		label.setText("Round: " + ((round + 1) / 2));
		updateTooltip();
	}

	public void setFatigue() {
		fatigue = true;
		this.setId("rounds_fatigue");
		updateTooltip();
	}

	private void updateTooltip() {
		String t = "You are now playing round " + ((round + 1) / 2);
		if (fatigue) t += ".\nFatigue has now set in.";
		tooltip.setText(t);
	}
}

