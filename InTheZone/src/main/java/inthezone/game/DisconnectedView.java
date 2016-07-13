package inthezone.game;

import inthezone.battle.data.GameDataFactory;
import inthezone.comptroller.Network;
import inthezone.game.loadoutEditor.LoadoutView;
import inthezone.game.lobby.ChallengePane;
import isogame.engine.CorruptDataException;
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
	private final Button sandpit = new Button("Sandpit mode");

	public DisconnectedView(
		ContentPane parent,
		GameDataFactory gameData,
		ClientConfig config,
		String server,
		int port,
		Optional<String> cachedName
	) {
		this.network = parent.network;

		login.setOnAction(event -> {
			String playerName = "";
			if (cachedName.isPresent()) {
				playerName = cachedName.get();
			} else {
				while (playerName.equals("")) {
					TextInputDialog ti = new TextInputDialog("<player name>");
					ti.setTitle("Enter player name");
					ti.setHeaderText("Enter player name");
					Optional<String> oPlayerName = ti.showAndWait();
					if (!oPlayerName.isPresent()) return;
					playerName = oPlayerName.orElse("");
					if (playerName.equals("")) {
						Alert a = new Alert(Alert.AlertType.INFORMATION,
							"You must enter a player name", ButtonType.OK, ButtonType.CANCEL);
						a.setHeaderText("");
						Optional<ButtonType> r = a.showAndWait();
						if (r.isPresent() && r.get() == ButtonType.CANCEL) return;
					}
				}
			}
			startConnecting();
			network.connectToServer(server, port, playerName);
		});

		loadout.setOnAction(event -> {
			parent.showScreen(
				new LoadoutView(parent.config, parent.gameData),
				v -> {});
		});

		sandpit.setOnAction(event -> {
			try {
				parent.showScreen(
					new ChallengePane(gameData, config, Optional.empty(), 0), oCmdReq -> {});
			} catch (CorruptDataException e) {
				Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
				a.setHeaderText("Error initialising challenge panel");
				a.showAndWait();
			}
		});

		this.getChildren().addAll(login, loadout, sandpit);
	}

	public void startConnecting() {
		// TODO: show an animation of some sort to indicate that we are connecting
	}
	
	public void endConnecting() {
	}
}

