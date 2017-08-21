package inthezone.game.lobby;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class NewsPanel extends StackPane {
	private final VBox inner = new VBox();
	private final Label title = new Label("News");
	private final Label content = new Label("No news");

	public NewsPanel() {
		this.setId("news-panel");
		inner.setId("news-panel-inner");
		title.getStyleClass().add("title");
		inner.getChildren().addAll(title, content);
		this.getChildren().add(inner);

		title.setMaxWidth(Double.MAX_VALUE);
		content.setMaxWidth(Double.MAX_VALUE);
		content.setMaxHeight(Double.MAX_VALUE);
		content.setAlignment(Pos.CENTER);
		VBox.setVgrow(content, Priority.ALWAYS);
	}
}

