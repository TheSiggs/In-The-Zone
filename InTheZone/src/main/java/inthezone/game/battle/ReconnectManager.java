package inthezone.game.battle;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * A HUD element to manage situations where a connection to the server has been
 * lost.
 * */
public class ReconnectManager extends HBox {
	private final Label message = new Label(" ");

	/**
	 * @param thisClientReconnecting true if this is the client that lost the
	 * connection, otherwise it is the other client that lost the connection
	 * */
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


