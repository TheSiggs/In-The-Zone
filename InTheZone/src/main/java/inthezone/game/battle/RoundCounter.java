package inthezone.game.battle;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class RoundCounter extends StackPane {
	private int round = 0;
	private boolean fatigue = false;

	private final Label label = new Label("Round: 0");

	public RoundCounter() {
		this.setId("rounds");
		this.getChildren().add(label);
		this.setPrefWidth(114);
		this.setPrefHeight(40);
	}

	public void increment() {
		round += 1;
		label.setText("Round: " + round);
	}

	public void setFatigue() {
		fatigue = true;
	}
}

