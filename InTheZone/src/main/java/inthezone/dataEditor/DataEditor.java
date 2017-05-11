package inthezone.dataEditor;

import inthezone.battle.data.GameDataFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.Map;
import java.util.Optional;
 
public class DataEditor extends Application {
	public static void main(final String[] arguments) {
		Application.launch(arguments);
	}

	@Override
	public void start(Stage primaryStage) {
		Map<String, String> args = getParameters().getNamed();

		BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 960, 540);

		try {
			Optional<File> dataDir;
			dataDir = Optional.ofNullable(args.get("basedir")).map(s -> new File(s));
			if (!dataDir.isPresent()) {
				dataDir = Optional.ofNullable(getDataDir(primaryStage));
			}

			if (!dataDir.isPresent()) System.exit(1);
			GameDataFactory factory = new GameDataFactory(dataDir, false);

			// used to detect changes in the data so we know if it is necessary to save
			final SimpleBooleanProperty changed = new SimpleBooleanProperty(false);

			AbilitiesPane abilitiesPane = new AbilitiesPane(changed, factory);
			CharactersPane charactersPane = new CharactersPane(
				factory, dataDir.get(), changed, abilitiesPane);

			root.setLeft(charactersPane);
			root.setCenter(abilitiesPane);
			root.setBottom(null);

			primaryStage.setOnCloseRequest(event -> {
				if (charactersPane != null && !charactersPane.gameDataSaved()) {
					final Alert save = new Alert(Alert.AlertType.CONFIRMATION,
						"Save before closing?", ButtonType.YES, ButtonType.NO);
					final Optional<ButtonType> r = save.showAndWait();

					if (!r.isPresent()) {
						event.consume();
					} else {
						ButtonType rr = r.get();
						if (rr.equals(ButtonType.YES)) {
							charactersPane.saveGameData();
							if (!charactersPane.gameDataSaved()) event.consume();
						}
					}
				}
			});

			primaryStage.setTitle("Data editor");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
			Alert error = new Alert(Alert.AlertType.ERROR);
			error.setTitle("Unexpected error");
			error.setHeaderText(e.toString());
			error.showAndWait();
			System.exit(1);
		}
	}

	public File getDataDir(Stage primaryStage) {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select data directory...");
		return directoryChooser.showDialog(primaryStage);
	}
}

