package inthezone.game;

import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import java.util.Collection;

public class LobbyView extends FlowPane {
	public LobbyView() {
		this.getChildren().add(new Label("This is the lobby"));
	}

	public void setPlayers(Collection<String> players) {
	}
}

