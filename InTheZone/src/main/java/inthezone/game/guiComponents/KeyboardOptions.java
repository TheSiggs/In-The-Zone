package inthezone.game.guiComponents;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;

import inthezone.game.ClientConfig;

public class KeyboardOptions extends DialogPane {
	private final ButtonType doneButton =
		new ButtonType("Done", ButtonBar.ButtonData.OK_DONE);
	
	private final GridPane headers = new GridPane();
	private final GridPane bindings = new GridPane();
	private final ScrollPane bindingsWrapper;

	public KeyboardOptions(final ClientConfig config) {
		bindingsWrapper = new ScrollPane(bindings);
	}
}

