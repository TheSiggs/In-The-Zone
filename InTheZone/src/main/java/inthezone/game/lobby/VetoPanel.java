package inthezone.game.lobby;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class VetoPanel extends VBox {
	private final Button resetButton = new Button("Reset");
	private final Button joinButton = new Button("Join queue");
	private final Button cancelButton = new Button("Cancel");
	private final HBox buttonBar = new HBox();
	private final Label title = new Label(
		"Click on any maps you don't want to play to veto them");
	private final ScrollPane vetoWrapper;
	private final FlowPane vetoButtons = new FlowPane();

	public VetoPanel() {
		vetoWrapper = new ScrollPane(vetoButtons);

		final Separator s1 = new Separator();
		final Separator s2 = new Separator();
		s1.setMaxWidth(Double.MAX_VALUE); s2.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(s1, Priority.ALWAYS); HBox.setHgrow(s2, Priority.ALWAYS);
		buttonBar.getChildren().addAll(resetButton, s1, joinButton, s2, cancelButton);

		this.getChildren().addAll(title, vetoWrapper, buttonBar);
	}
}

