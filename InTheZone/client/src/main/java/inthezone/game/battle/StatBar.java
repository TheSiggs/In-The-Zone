package inthezone.game.battle;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;

/**
 * A status bar for the CharacterInfoBoxes.
 * */
public class StatBar extends StackPane {
	private final static double WIDTH = 147;
	private final static double HEIGHT = 20;

	private final ImageView bar = new ImageView();
	private final Pane linesPane = new Pane();
	private final Label label;

	private int val = 0;
	private int max = 0;

	/**
	 * @param id the JavaFX id for this control
	 * @param hasLabel true if this StatBar is labelled with it's actual / max
	 * values
	 * */
	public StatBar(final String id, final boolean hasLabel) {
		this.setId(id);
		this.getStyleClass().add("statBar");
		this.setMaxWidth(WIDTH+4);
		this.setMaxHeight(HEIGHT+4);
		this.setMinWidth(WIDTH+4);
		this.setMinHeight(HEIGHT+4);
		this.setPrefWidth(WIDTH+4);
		this.setPrefHeight(HEIGHT+4);
		if (hasLabel) {
			label = new Label("0 / 0");
		} else {
			label = null;
		}

		StackPane.setAlignment(bar, Pos.TOP_LEFT);
		this.getChildren().addAll(bar, linesPane);

		if (label != null) {
			this.getChildren().add(label);
		}
	}

	/**
	 * Update this StatBar
	 * @param id the new JavaFX id for this control
	 * @param val the actual value
	 * @param max the maximum value
	 * @param infinite true if this bar is infinite (in which case the max value
	 * is ignored)
	 * */
	public void update(
		final String id, final int val, final int max, final boolean infinite
	) {
		this.setId(id);

		if (val == 0) {
			bar.setVisible(false);
		} else {
			bar.setVisible(true);
			bar.setViewport(new Rectangle2D(0, 0,
				WIDTH * ((double) val / (double) max), HEIGHT));
			bar.setX(0);
			bar.setY(0);
		}

		if (label != null) {
			label.setText(val + " / " + (infinite? "∞" : ("" + max)));
		} else {
			if (linesPane.getChildren().size() + 1 != max) {
				linesPane.getChildren().clear();
				double stride = WIDTH / (double) max;
				for (int i = 1; i < max; i++) {
					double x = ((double) i) * stride;
					Line l = new Line(x, 0, x, HEIGHT);
					linesPane.getChildren().add(l);
				}
			}
		}
	}
}

