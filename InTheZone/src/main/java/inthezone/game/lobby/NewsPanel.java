package inthezone.game.lobby;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class NewsPanel extends StackPane {
	private final VBox inner = new VBox();
	private final Label title = new Label("News");
	private final WebView content = new WebView();

	private final static String userAgent = "InTheZone";

	private final static String newsURL =
		"http://www.dreamprodigy.co.nz/vm973eoknid2ic3qpl02gg364iikof84/news/index.html";

	public NewsPanel() {
		this.setId("news-panel");
		inner.setId("news-panel-inner");
		title.getStyleClass().add("title");
		inner.getChildren().addAll(title, content);
		this.getChildren().add(inner);

		content.setContextMenuEnabled(false);
		content.getEngine().setJavaScriptEnabled(false);
		content.getEngine().setUserAgent(userAgent);
		content.getEngine().load(newsURL);

		title.setMaxWidth(Double.MAX_VALUE);
		content.setMaxWidth(Double.MAX_VALUE);
		content.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(content, Priority.ALWAYS);
	}
}

