package inthezone.game;

import inthezone.comptroller.Network;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import java.util.Optional;

public class DisconnectedView extends FlowPane {
	private final Network network;

	private final Button login = new Button("Connect to server");
	private final Button loadout = new Button("Edit loadouts offline");

	public DisconnectedView(
		Network network, String server, int port, Optional<String> cachedName
	) {
		this.network = network;

		login.setOnAction(event -> {
			String playerName = "";
			if (cachedName.isPresent()) {
				playerName = cachedName.get();
			} else {
				while (playerName.equals("")) {
					TextInputDialog ti = new TextInputDialog("<player name>");
					ti.setHeaderText("Enter player name");
					playerName = ti.showAndWait().orElse("");
					if (playerName.equals("")) {
						Alert a = new Alert(Alert.AlertType.ERROR,
							"You must enter a player name", ButtonType.OK, ButtonType.CANCEL);
						a.showAndWait().ifPresent(x -> {
							if (x == ButtonType.CANCEL) return;
						});
					}
				}
			}
			startConnecting();
			network.connectToServer(server, port, playerName);
		});

		this.getChildren().addAll(login, loadout);
	}

	public void startConnecting() {
		// TODO: show an animation of some sort to indicate that we are connecting
	}
	
	public void endConnecting() {
	}
}

