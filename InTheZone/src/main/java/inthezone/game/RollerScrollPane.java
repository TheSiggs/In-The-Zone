package inthezone.game;

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

		back.setOnAction(event -> {
			if (horizontal) {
				pane.setHvalue(Math.max(pane.getHmin(), pane.getHvalue() - 0.04));
			} else {
				pane.setVvalue(Math.max(pane.getVmin(), pane.getVvalue() - 0.04));
			}
		});

		forward.setOnAction(event -> {
			if (horizontal) {
				pane.setHvalue(Math.min(pane.getHmax(), pane.getHvalue() + 0.04));
			} else {
				pane.setVvalue(Math.min(pane.getVmax(), pane.getVvalue() + 0.04));
			}
		});

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
}

