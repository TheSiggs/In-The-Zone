package inthezone.game.lobby;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

/**
 * Content to show when the server is disconnected.
 * */
public class DisconnectedPanel extends Label {
	public DisconnectedPanel() {
		this.setMaxWidth(Double.MAX_VALUE);
		this.setMaxHeight(Double.MAX_VALUE);
		this.setAlignment(Pos.CENTER);
		this.setText("Disconnected from server");
	}
}

