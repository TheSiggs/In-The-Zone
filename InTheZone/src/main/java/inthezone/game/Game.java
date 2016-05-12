package inthezone.game;

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Game extends Application {
	public static void main(final String[] arguments) {
		Application.launch(arguments);
	}

	private final Button login = new Button("Connect to server");
	private final Button loadout = new Button("Edit loadouts offline");

	@Override
	public void start(Stage primaryStage) {
		FlowPane root = new FlowPane();
		Scene scene = new Scene(root, 960, 540);

		root.getChildren().addAll(login, loadout);

		primaryStage.setTitle("In the Zone!");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}

