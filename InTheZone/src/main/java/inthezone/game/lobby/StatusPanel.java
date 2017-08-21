package inthezone.game.lobby;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StatusPanel extends VBox {
	private final Button cancel = new Button("Cancel");
	private final Label message = new Label();

	public StatusPanel(final Runnable onCancel) {
		cancel.setOnAction(event -> onCancel.run());
		this.getChildren().addAll(message, cancel);
	}

	public void setStatus(final String status) {
		message.setText(status);
	}
}

