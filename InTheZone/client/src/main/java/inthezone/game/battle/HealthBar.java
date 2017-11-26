package inthezone.game.battle;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import static isogame.GlobalConstants.TILEW;
import static isogame.GlobalConstants.TILEH;

/**
 * A healthbar that appears on top of a player.
 * */
public class HealthBar extends Group {
	private int hp = 0;
	private int maxHP = 0;

	private final static Duration animatingSpeed = new Duration(250d);
	private final static Paint backgroundPaint = Color.color(1.0f, 0.0f, 0.0f);
	private final static Paint healthPaint = Color.color(0.0f, 1.0f, 0.0f);
	private final static Paint coverBackgroundPaint = Color.color(1.0f, 0.65f, 0.0f);
	private final static Paint coverHealthPaint = Color.color(1.0f, 1.00f, 0.0f);

	private static final Paint hpChangeColor = Color.RED;
	private static final Font hpChangeFont = new Font(32d);
	private final static double hpChangeTranslation = -30d;

	private final static double X = TILEW * 0.35d;
	private final static double Y = 0d;
	private final static double W = TILEW * 0.3d;
	private final static double H = TILEH * 1d/16d;

	private final Rectangle health = new Rectangle(X, Y, W, H);
	private final Rectangle background = new Rectangle(X + W, Y, 0d, H);

	private boolean inited = false;
	private double d = W;
	private Animation hpAnimation = null;

	public HealthBar() {
		this.getChildren().addAll(background, health);
		health.setWidth(d);
		background.setX(X + d);
		background.setWidth(W - d);
	}

	/**
	 * Update the health bar after damage.
	 * @param damage the amount of damage (perhaps with a + or - sign)
	 * */
	private void doDamage(final String damage) {
		final Text hpChangeLabel = new Text();
		this.getChildren().add(hpChangeLabel);
		hpChangeLabel.setFont(hpChangeFont);
		hpChangeLabel.setFill(hpChangeColor);
		hpChangeLabel.setY(Y - H - 4d);
		hpChangeLabel.setText(damage);
		hpChangeLabel.setX((TILEW / 2d) -
			(hpChangeLabel.getBoundsInLocal().getWidth() / 2d));

		final TranslateTransition animation =
			new TranslateTransition(new Duration(1000d), hpChangeLabel);

		animation.setByY(hpChangeTranslation);
		animation.setOnFinished(event -> this.getChildren().remove(hpChangeLabel));
		animation.play();
	}

	/**
	 * Update HP.
	 * @param hp the new hp value
	 * @param maxHP the new maximum hp
	 * @param cover true if the character has cover, otherwise false
	 * */
	public void updateHP(
		final int hp, final int maxHP, final boolean cover
	) {
		final int hp0 = this.hp;
		final int hpChange = hp - hp0;
		this.hp = hp;
		this.maxHP = maxHP;

		if (cover) {
			health.setFill(coverHealthPaint);
			background.setFill(coverBackgroundPaint);
		} else {
			health.setFill(healthPaint);
			background.setFill(backgroundPaint);
		}

		if (hpChange != 0 && inited) {
			doDamage("" + hpChange);

			final double d0 = d;
			final double d1 = (W * (double) hp) / (double) maxHP;

			if (hpAnimation != null) hpAnimation.stop();

			hpAnimation = new Transition() {
				{
					setCycleDuration(animatingSpeed);
				}

				@Override protected void interpolate(final double f) {
					d = d0 + ((d1 - d0) * f);
					health.setWidth(d);
					background.setX(X + d);
					background.setWidth(W - d);
				}
			};

			hpAnimation.play();
		}
		this.inited = true;
	}
}

