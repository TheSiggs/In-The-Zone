package inthezone.game.battle;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.CacheHint;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * A HUD component to show the player how much time they have remaining for
 * their turn.
 * */
public class TurnClock extends Pane {
	private final Circle face = new Circle();
	private final Line hand = new Line();
	private final Circle handBase = new Circle();
	private final Arc arc = new Arc();

	private final double HAND_LENGTH = 0.8;
	private final double ARC_LENGTH = 0.6;
	private final double HAND_BASE_R = 4;

	public double angle = 0;

	public final Animation clockAnimator;

	public StringProperty remainingTime = new SimpleStringProperty();

	private final Duration duration;

	private final Rotate rotation = new Rotate();

	/**
	 * @param duration the duration of each cycle of the clock
	 * */
	public TurnClock(final Duration duration) {
		this.duration = duration;

		this.getStyleClass().add("turn-clock");
		face.getStyleClass().add("turn-clock-face");
		hand.getStyleClass().add("turn-clock-hand");
		handBase.getStyleClass().add("turn-clock-hand");
		arc.getStyleClass().add("turn-clock-arc");

		this.widthProperty().addListener(o -> resizeGraphics());
		this.heightProperty().addListener(o -> resizeGraphics());

		face.setCache(true);
		hand.setCache(false);
		handBase.setCache(true);
		arc.setCache(false);

		hand.getTransforms().add(rotation);

		arc.setStartAngle(90d);
		resizeGraphics();

		clockAnimator = new Transition() {
			{
				setCycleDuration(duration);
				setInterpolator(Interpolator.LINEAR);
			}

			@Override protected void interpolate(final double f) {
				setAngle(360d * f);
			}
		};


		this.getChildren().addAll(face, hand, handBase, arc);
	}

	/**
	 * Reset the clock to 0
	 * */
	public void reset() {
		clockAnimator.stop();
		setAngle(0);
	}

	/**
	 * Format a count of seconds in a human readable format.
	 * */
	private String formatTime(final long s) {
		String us = "" + s % 60;
		while (us.length() < 2) us = "0" + us;
		return "" + (s / 60) + ":" + us;
	}

	private long lastS = -1;

	/**
	 * Set the clock angle.
	 * @param angle the angle to set
	 * */
	private void setAngle(final double angle) {
		final long s = (long) Math.floor(
			(duration.toMillis() / 250) * ((360d - angle) / 360d));

		if (s != lastS) {
			final double w = this.getWidth();
			final double h = this.getHeight();
			final double r = Math.min(w, h) / 2;
			this.angle = angle;
			final double handAngle = ((360d + 90d - angle) % 360d);
			final double handAngleR = handAngle * (Math.PI / 180d);

			arc.setStartAngle(handAngle);
			arc.setLength(angle);
			rotation.setAngle(angle);

			lastS = s;
			if (s % 4 == 0) remainingTime.set(formatTime(s / 4));
		}
	}

	/**
	 * Resize this control to fit its borders.
	 * */
	private void resizeGraphics() {
		final double w = this.getWidth();
		final double h = this.getHeight();
		final double r = Math.min(w, h) / 2;

		face.setCenterX(w / 2);
		face.setCenterY(h / 2);
		face.setRadius(r);

		handBase.setCenterX(w / 2);
		handBase.setCenterY(h / 2);
		handBase.setRadius(HAND_BASE_R);

		hand.setStartX(w / 2);
		hand.setStartY(h / 2);

		rotation.setPivotX(hand.getStartX());
		rotation.setPivotY(hand.getStartY());

		hand.setEndX(hand.getStartX());
		hand.setEndY(hand.getStartY() - (r * HAND_LENGTH));

		arc.setCenterX(w / 2);
		arc.setCenterY(h / 2);
		arc.setRadiusX(r * ARC_LENGTH);
		arc.setRadiusY(r * ARC_LENGTH);

		setAngle(angle);
	}
}

