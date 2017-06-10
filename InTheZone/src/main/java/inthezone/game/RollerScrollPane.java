package inthezone.game;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class RollerScrollPane extends AnchorPane {
	private final StackPane innerPane = new StackPane();
	private final StackPane viewPane = new StackPane();
	private final Rectangle clipRect = new Rectangle();
	private final boolean horizontal;
	private final Button back;
	private final Button forward;

	private final static double SCROLL_BUTTON_SIZE = 10;

	private Region content;

	private double scrollMin = 0;
	private double scrollMax = 0;
	private double scroll = 0;

	private boolean enableScrollWheel = true;

	public RollerScrollPane(Region content, boolean horizontal) {
		super();
		this.horizontal = horizontal;
		this.content = content;

		if (horizontal) this.setMinWidth(64);
		else this.setMinHeight(64);

		if (horizontal) innerPane.maxWidthProperty().bind(this.widthProperty());
		else innerPane.maxHeightProperty().bind(this.heightProperty());

		innerPane.getChildren().add(content);
		viewPane.setAlignment(Pos.TOP_LEFT);
		viewPane.getChildren().add(innerPane);
		viewPane.maxWidthProperty().bind(this.widthProperty());
		viewPane.maxHeightProperty().bind(this.heightProperty());
		viewPane.setClip(clipRect);

		this.widthProperty().addListener((v, o, n) -> {
			final Insets i = this.getInsets();
			clipRect.setWidth(this.getWidth() - i.getLeft() - i.getRight());
		});

		this.heightProperty().addListener((v, o, n) -> {
			final Insets i = this.getInsets();
			clipRect.setHeight(this.getHeight() - i.getTop() - i.getBottom());
		});

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
			forward.setMaxWidth(Double.MAX_VALUE);
			forward.setMaxHeight(SCROLL_BUTTON_SIZE);
		}

		this.getStyleClass().add("roller-scroller");
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

		AnchorPane.setTopAnchor(viewPane, 0d);
		AnchorPane.setBottomAnchor(viewPane, 0d);
		AnchorPane.setLeftAnchor(viewPane, 0d);
		AnchorPane.setRightAnchor(viewPane, 0d);

		back.setOnMousePressed(event -> mouseDown(true));
		back.setOnMouseReleased(event -> mouseUp());
		back.setOnMouseExited(event -> mouseUp());
		forward.setOnMousePressed(event -> mouseDown(false));
		forward.setOnMouseReleased(event -> mouseUp());
		forward.setOnMouseExited(event -> mouseUp());

		this.getChildren().addAll(viewPane, back, forward);

		content.boundsInLocalProperty().addListener(o -> showHideButtons());
		viewPane.boundsInParentProperty().addListener(o -> showHideButtons());

		showHideButtons();

		this.setOnScroll(event -> {
			if (enableScrollWheel) setScrollPos(scroll - event.getDeltaY());
		});
	}

	public void setScrollWheelEnable(boolean enableScrollWheel) {
		this.enableScrollWheel = enableScrollWheel;
	}

	public double getScrollMin() { return scrollMin; }

	public double getScrollMax() { return scrollMax; }

	public void setScrollPos(double pos) {
		scroll = Math.min(scrollMax, Math.max(pos, scrollMin));
		if (horizontal) innerPane.setTranslateX(-scroll);
			else innerPane.setTranslateY(-scroll);
	}

	private boolean isVisible = false;

	private void showHideButtons() {
		final Bounds vp = viewPane.getBoundsInParent();
		final Bounds cn = innerPane.getBoundsInLocal();

		final boolean v;
		if (horizontal) v = cn.getWidth() > vp.getWidth();
		else v = cn.getHeight() > vp.getHeight();

		back.setVisible(v);
		forward.setVisible(v);

		if (v) {
			scrollMin = -20;
			if (horizontal) {
				scrollMax = cn.getWidth() - vp.getWidth() + 20;
			} else {
				scrollMax = cn.getHeight() - vp.getHeight() + 20;
			}

		} else if (isVisible) {
			scrollMin = 0;
			scrollMax = 0;
			setScrollPos(0);
		}

		isVisible = v;
	}

	private ScrollAnimation animation = null;

	private void mouseDown(boolean back) {
		if (animation == null) {
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
		private final static double SCROLL_SPEED = 180;

		final double max;
		final double start;
		final boolean back;

		public ScrollAnimation(boolean back) {
			this.back = back;
			this.setInterpolator(Interpolator.LINEAR);
			if (back) {
				max = scrollMin;
			} else {
				max = scrollMax;
			}
			start = scroll;
			setCycleDuration(new Duration((Math.abs(max - start) / SCROLL_SPEED) * 1000));
		}

		public void interpolate(double frac) {
			final double pos;
			if (back) pos = ((start - max) * (1 - frac)) + max;
			else pos = ((max - start) * frac) + start;

			setScrollPos(pos);
		}
	}

}

