package inthezone.game.lobby;

import java.util.Optional;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Panel which shows the current lobby state of the player (e.g. waiting on
 * queue, waiting for challenged player to respond).
 * */
public class StatusPanel extends StackPane {
	private final VBox inner = new VBox();
	private final Label title = new Label("Status");
	private final StackPane cancelWrapper = new StackPane();
	private final Button cancel = new Button("Cancel");
	private final Label message = new Label();

	public enum WaitingStatus {
		NONE, CHALLENGE, QUEUE, QUEUED_SETUP
	}

	private Optional<String> waitingForPlayer = Optional.empty();
	public Optional<String> getWaitingForPlayer() { return waitingForPlayer; }

	private WaitingStatus waitingStatus = WaitingStatus.NONE;

	public StatusPanel(final Runnable onCancel) {
		this.setMaxWidth(380d);
		this.setMinWidth(380d);
		this.setPrefWidth(380d);

		title.getStyleClass().add("title");
		this.getStyleClass().add("left-panel");
		inner.getStyleClass().add("left-panel-inner");
		message.setId("status-message");
		cancel.getStyleClass().addAll("gui-button");
		cancelWrapper.getStyleClass().add("control-wrapper");

		title.setMaxWidth(Double.MAX_VALUE);
		VBox.setVgrow(message, Priority.ALWAYS);
		message.setMaxHeight(Double.MAX_VALUE);
		message.setMaxWidth(Double.MAX_VALUE);
		message.setAlignment(Pos.CENTER);

		cancelWrapper.getChildren().add(cancel);

		cancel.setOnAction(event -> onCancel.run());
		inner.getChildren().addAll(title, message, cancelWrapper);
		this.getChildren().add(inner);
	}

	public WaitingStatus getWaitingStatus() { return waitingStatus; }

	public String getWaitingMessage() { return message.getText(); }

	public void waitingDone() {
		waitingStatus = WaitingStatus.NONE;
		waitingForPlayer = Optional.empty();
	}

	public void waitInQueue() {
		waitingStatus = WaitingStatus.QUEUE;
		waitingForPlayer = Optional.empty();
		message.setText("Waiting in queue");
	}

	public void waitForChallenge(final String toPlayer) {
		waitingStatus = WaitingStatus.CHALLENGE;
		waitingForPlayer = Optional.of(toPlayer);
		message.setText("Waiting for " + toPlayer + " to respond to challenge");
	}

	public void waitForOtherPlayer(final String otherPlayer) {
		waitingStatus = WaitingStatus.QUEUED_SETUP;
		waitingForPlayer = Optional.of(otherPlayer);
		message.setText("Waiting for " +
			otherPlayer + " to choose starting positions");
	}
}

