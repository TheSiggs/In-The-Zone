package inthezone.game.lobby;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class StatusPanel extends StackPane {
	private final VBox inner = new VBox();
	private final Button cancel = new Button("Cancel");
	private final Label message = new Label();

	public enum WaitingStatus {
		NONE, CHALLENGE, QUEUE
	}

	private WaitingStatus waitingStatus = WaitingStatus.NONE;

	public StatusPanel(final Runnable onCancel) {
		cancel.setOnAction(event -> onCancel.run());
		inner.getChildren().addAll(message, cancel);
		this.getChildren().add(inner);
	}

	public WaitingStatus getWaitingStatus() { return waitingStatus; }

	public String getWaitingMessage() { return message.getText(); }

	public void waitingDone() {
		waitingStatus = WaitingStatus.NONE;
	}

	public void waitInQueue() {
		waitingStatus = WaitingStatus.QUEUE;
		message.setText("Waiting in queue");
	}

	public void waitForChallenge(final String toPlayer) {
		waitingStatus = WaitingStatus.CHALLENGE;
		message.setText("Waiting for " + toPlayer + " to respond to challenge");
	}
}

