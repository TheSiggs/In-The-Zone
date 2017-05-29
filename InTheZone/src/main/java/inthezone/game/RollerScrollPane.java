package inthezone.game;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.Node;
import javafx.util.Duration;

public class RollerScrollPane extends AnchorPane {
	private final ScrollPane pane = new ScrollPane();
	private final boolean horizontal;
	private final Button back;
	private final Button forward;

	private final static double SCROLL_BUTTON_SIZE = 10;

	private Region content;

	public RollerScrollPane(Region content, boolean horizontal) {
		super();
		this.horizontal = horizontal;
		this.content = content;

		pane.setContent(content);
		pane.setPannable(true);
		pane.setFitToHeight(true);
		pane.setFitToWidth(true);
		pane.setVbarPolicy(ScrollBarPolicy.NEVER);
		pane.setHbarPolicy(ScrollBarPolicy.NEVER);

		if (horizontal) {
			back = new Button(null, new ImageView(
				new Image("/gui_assets/arrow_left.png")));
			forward = new Button(null, new ImageView(
				new Image("/gui_assets/arrow_right.png")));
			back.setMaxWidth(SCROLL_BUTTON_SIZE);
			back.setMaxHeight(Double.MAX_VALUE);
			forward.setMaxWidth(SCROLL_BUTTON_SIZE);
			forward.setMaxHeight(Double.MAX_VALUE);
		} else {
			back = new Button(null, new ImageView(
				new Image("/gui_assets/arrow_up.png")));
			forward = new Button(null, new ImageView(
				new Image("/gui_assets/arrow_down.png")));
			back.setMaxWidth(Double.MAX_VALUE);
			back.setMaxHeight(SCROLL_BUTTON_SIZE);
			forward.setMaxHeight(SCROLL_BUTTON_SIZE);
			forward.setMaxWidth(Double.MAX_VALUE);
		}

		pane.getStyleClass().add("clear-panel");
		pane.setStyle("-fx-padding:0px;");
		back.getStyleClass().add("gui-img-button");
		forward.getStyleClass().add("gui-img-button");

		if (horizontal) {
			AnchorPane.setTopAnchor(back, 0d);
			AnchorPane.setBottomAnchor(back, 0d);
			AnchorPane.setLeftAnchor(back, 0d);

			AnchorPane.setTopAnchor(forward, 0d);
			AnchorPane.setBottomAnchor(forward, 0d);
			AnchorPane.setRightAnchor(forward, 0d);
		} else {
			AnchorPane.setTopAnchor(back, 0d);
			AnchorPane.setLeftAnchor(back, 0d);
			AnchorPane.setRightAnchor(back, 0d);

			AnchorPane.setBottomAnchor(forward, 0d);
			AnchorPane.setLeftAnchor(forward, 0d);
			AnchorPane.setRightAnchor(forward, 0d);
		}

		AnchorPane.setTopAnchor(pane, 0d);
		AnchorPane.setBottomAnchor(pane, 0d);
		AnchorPane.setLeftAnchor(pane, 0d);
		AnchorPane.setRightAnchor(pane, 0d);

		content.boundsInLocalProperty().addListener(o -> showHideButtons());
		pane.viewportBoundsProperty().addListener(o -> showHideButtons());

		back.setOnMousePressed(event -> mouseDown(true));
		back.setOnMouseReleased(event -> mouseUp());
		back.setOnMouseExited(event -> mouseUp());
		forward.setOnMousePressed(event -> mouseDown(false));
		forward.setOnMouseReleased(event -> mouseUp());
		forward.setOnMouseExited(event -> mouseUp());

		this.getChildren().addAll(pane, back, forward);
	}

	private boolean isVisible = false;

	public void showHideButtons() {
		final Bounds vp = pane.getViewportBounds();
		final Bounds cn = pane.getContent().getBoundsInLocal();

		final boolean v;
		if (horizontal) v = vp.getWidth() < cn.getWidth();
		else v = vp.getHeight() < cn.getHeight();

		if (v == isVisible) return;
		isVisible = v;

		back.setVisible(v);
		forward.setVisible(v);

		if (v) {
			if (horizontal) content.setPadding(new Insets(0, 18, 0, 18));
			else content.setPadding(new Insets(18, 0, 18, 0));
		} else {
			content.setPadding(new Insets(0));
		}
	}

	public void setContent(Region content) {
		this.content = content;
		pane.setContent(content);
		content.boundsInLocalProperty().addListener(o -> showHideButtons());
		showHideButtons();
	}

	private ScrollAnimation animation = null;

	private void mouseDown(boolean back) {
		if (animation == null) {
			// this is a hack that forces the ScrollPane to correctly compute the
			// scroll bounds.
			pane.requestFocus();

			// start the scrolling animation
			animation = new ScrollAnimation(back);
			animation.play();
		}
	}

	private void mouseUp() {
		if (animation != null) {
			animation.pause();
			animation = null;
		}
	}

	private class ScrollAnimation extends Transition {
		private final static double SCROLL_SPEED = 4;

		final double max;
		final double start;
		final boolean back;

		public ScrollAnimation(boolean back) {
			this.back = back;
			this.setInterpolator(Interpolator.LINEAR);
			if (back) {
				max = horizontal? pane.getHmin() : pane.getVmin();
			} else {
				max = horizontal? pane.getHmax() : pane.getVmax();
			}
			start = horizontal? pane.getHvalue() : pane.getVvalue();
			setCycleDuration(new Duration((Math.abs(max - start) / SCROLL_SPEED) * 1000));
		}

		public void interpolate(double frac) {
			final double pos;
			if (back) pos = ((start - max) * (1 - frac)) + max;
			else pos = ((max - start) * frac) + start;

			if (horizontal) pane.setHvalue(pos); else pane.setVvalue(pos);
		}
	}

}

