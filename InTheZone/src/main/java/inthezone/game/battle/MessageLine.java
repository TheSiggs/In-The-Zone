package inthezone.game.battle;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class MessageLine extends Label{
	private final Timeline animation = new Timeline();

	private final static double VISIBLE_DURATION = 4 * 1000;
	private final static double HIDE_DURATION = 0.8 * 1000;

	public MessageLine() {
		super("");
		this.getStyleClass().add("message-line");
	}

	public void writeMessage(String message) {
		this.setText(message);
		animation.stop();

		animation.getKeyFrames().addAll(
			new KeyFrame(Duration.ZERO,
				new KeyValue(this.opacityProperty(), 1.0)),
			new KeyFrame(Duration.millis(VISIBLE_DURATION),
				new KeyValue(this.opacityProperty(), 1.0)),
			new KeyFrame(Duration.millis(VISIBLE_DURATION + HIDE_DURATION),
				new KeyValue(this.opacityProperty(), 0.0, Interpolator.EASE_IN)));

		animation.play();
	}
}

