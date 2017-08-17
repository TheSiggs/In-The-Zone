package inthezone.game.battle;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import static isogame.GlobalConstants.TILEH;
import static isogame.GlobalConstants.TILEW;

public class HealthBar extends Group {
	private int hp = 0;
	private int maxHP = 0;
	private boolean cover = false;

	private int hpChange = 0;

	private final static double nanos = 1000000000d;
	private final static double hidingSpeed = 2d / nanos;
	private final static double showingSpeed = 8d / nanos;
	private final static double animatingSpeed = 1d / nanos;

	// The delay before fading after changes to a character's health
	private final static double fadeDelay = 2d * nanos;

	private final static Paint backgroundPaint = Color.color(1.0f, 0.0f, 0.0f);
	private final static Paint healthPaint = Color.color(0.0f, 1.0f, 0.0f);
	private final static Paint coverBackgroundPaint = Color.color(1.0f, 0.65f, 0.0f);
	private final static Paint coverHealthPaint = Color.color(1.0f, 1.00f, 0.0f);

	private static final Font hpChangeFont = new Font(32d);

	private final static double X = TILEW * 0.35d;
	private final static double Y = 0d;
	private final static double W = TILEW * 0.3d;
	private final static double H = TILEH * 1d/16d;

	private final Rectangle health = new Rectangle(X, Y, W, H);
	private final Rectangle background = new Rectangle(X + W, Y, 0d, H);
	private final Text hpChangeLabel = new Text();

	public HealthBar() {
		hpChangeLabel.setVisible(false);
		hpChangeLabel.setFont(hpChangeFont);
		hpChangeLabel.setFill(Color.RED);
		hpChangeLabel.setY(Y - H - 4d);
		this.getChildren().addAll(background, health, hpChangeLabel);
	}

	public void updateHP(
		final int hp, final int maxHP, final boolean cover
	) {
		this.cover = cover;
		final int hpChange1 = hp - this.hp;
		this.hp = hp;
		this.maxHP = maxHP;

		final double d = (W * (double) hp) / (double) maxHP;

		health.setWidth(d);
		background.setX(X + d);
		background.setWidth(W - d);

		if (cover) {
			health.setFill(coverHealthPaint);
			background.setFill(coverBackgroundPaint);
		} else {
			health.setFill(healthPaint);
			background.setFill(backgroundPaint);
		}

		if (hpChange1 != hpChange && hpChange1 != 0) {
			hpChangeLabel.setVisible(true);
			hpChangeLabel.setText("" + hpChange1);
			hpChangeLabel.setX((TILEW / 2d) -
				(hpChangeLabel.getBoundsInLocal().getWidth() / 2d));
		} else {
			hpChangeLabel.setVisible(false);
		}
		this.hpChange = hpChange1;
	}
}

