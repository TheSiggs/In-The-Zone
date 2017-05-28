package inthezone.game;

import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

public class RollerScrollPane extends StackPane {
	private final ScrollPane pane = new ScrollPane();
	private final boolean horizontal;
	private final Button back;
	private final Button forward;
	private final Pane inner;

	private final static double SCROLL_BUTTON_SIZE = 10;

	public RollerScrollPane(Node content, boolean horizontal) {
		super();
		this.horizontal = horizontal;

		pane.setContent(content);
		pane.setFitToHeight(true);
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
			back.setMaxHeight(SCROLL_BUTTON_SIZE);
			forward.setMaxHeight(SCROLL_BUTTON_SIZE);
		}

		//pane.getStyleClass().add("clear-panel");
		//pane.setStyle("-fx-padding:0px;");
		pane.setStyle("-fx-background-color:red;");
		back.getStyleClass().add("gui-img-button");
		forward.getStyleClass().add("gui-img-button");

		if (horizontal) {
			inner = new HBox();
			HBox.setHgrow(pane, Priority.ALWAYS);
		} else {
			inner = new VBox();
			VBox.setVgrow(pane, Priority.ALWAYS);
		}

		back.setVisible(false);
		forward.setVisible(false);
		back.setManaged(false);
		forward.setManaged(false);
		inner.getChildren().addAll(back, pane, forward);

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

		this.getChildren().add(inner);
	}

	public void showHideButtons() {
		final Bounds vp = pane.getViewportBounds();
		final Bounds cn = pane.getContent().getBoundsInLocal();

		final boolean v;
		if (horizontal) v = vp.getWidth() < cn.getWidth();
		else v = vp.getHeight() < cn.getHeight();

		back.setVisible(v);
		back.setManaged(v);
		forward.setVisible(v);
		forward.setManaged(v);
 
		if (!v) pane.requestFocus();
	}

	public void setContent(Node content) {
		pane.setContent(content);
		content.boundsInLocalProperty().addListener(o -> showHideButtons());
		showHideButtons();
	}
}

