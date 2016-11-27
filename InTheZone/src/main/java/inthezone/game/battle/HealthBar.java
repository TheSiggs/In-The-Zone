package inthezone.game.battle;

import isogame.engine.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import static isogame.GlobalConstants.TILEH;
import static isogame.GlobalConstants.TILEW;

public class HealthBar {
	private int hp;
	private int maxHP;

	private double targetPosition;
	private double position = 1.0;
	private double untilFade = 0.0;
	private double alpha = 0;
	private boolean hiding = true;

	private long t0 = 0;

	private final static double nanos = 1000000000.0;
	private final static double hidingSpeed = 2.0 / nanos;
	private final static double showingSpeed = 8.0 / nanos;
	private final static double animatingSpeed = 1.0 / nanos;

	// The delay before fading after changes to a character's health
	private final static double fadeDelay = 2.0 * nanos;

	private final static Paint background = Color.color(1.0f, 0.0f, 0.0f);
	private final static Paint health = Color.color(0.0f, 1.0f, 0.0f);

	private final static double X = TILEW * 0.35;
	private final static double Y = 0;
	private final static double W = TILEW * 0.3;
	private final static double H = TILEH * 1.0/16.0;

	public HealthBar(int hp, int maxHP) {
		this.hp = hp;
		this.maxHP = maxHP;
		targetPosition = (double) hp / (double) maxHP;
	}

	public void updateHP(int hp, int maxHP) {
		this.hp = hp;
		this.maxHP = maxHP;
		targetPosition = (double) hp / (double) maxHP;
	}

	public void show() {
		hiding = false;
	}

	public void hide() {
		hiding = true;
	}

	/**
	 * Render a health bar
	 * @param t The timestamp of the current frame in nanoseconds
	 * */
	public void render(GraphicsContext cx, Sprite s, long t) {
		updateAnimations(t);
		if (alpha == 0) return;

		cx.save();
		cx.setGlobalAlpha(alpha);
		cx.setFill(background);
		cx.fillRect(X, Y, W, H);
		cx.setFill(health);
		cx.fillRect(X, Y, W * position, H);
		cx.restore();
	}

	private void updateAnimations(long t) {
		if (t0 == 0) t0 = t;
		double delta = (double) (t - t0);
		t0 = t;

		if (untilFade > 0) untilFade -= delta;

		if ((!hiding || position != targetPosition) && alpha != 1.0) {
			alpha += delta * showingSpeed;
			if (alpha > 1.0) alpha = 1.0;
		}

		if (position < targetPosition) {
			position += delta * animatingSpeed;
			if (position >= targetPosition) {
				position = targetPosition;
				untilFade = fadeDelay;
			}

		} else if (position > targetPosition) {
			position -= delta * animatingSpeed;
			if (position <= targetPosition) {
				position = targetPosition;
				untilFade = fadeDelay;
			}

		} else {
			if (hiding && alpha != 0 && untilFade <= 0) alpha -= delta * hidingSpeed;
			if (alpha < 0) alpha = 0;
		}
	}
}

