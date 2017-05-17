package inthezone.game.battle;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ReconnectManager extends HBox {
	private final Label message = new Label(" ");

	public ReconnectManager(boolean thisClientReconnecting) {
		super();

		this.setAlignment(Pos.CENTER);
		this.getStyleClass().add("reconnect-manager");

		if (thisClientReconnecting) {
			message.setText("Attempting to reconnect to server");
		} else {
			message.setText("Waiting for other client to reconnect to server...");
		}

		this.getChildren().addAll(message);
	}
}


