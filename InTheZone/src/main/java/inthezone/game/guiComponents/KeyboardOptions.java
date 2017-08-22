package inthezone.game.guiComponents;

import isogame.engine.KeyBinding;
import isogame.engine.KeyBindingTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import inthezone.game.ClientConfig;
import inthezone.game.InTheZoneKeyBinding;

public class KeyboardOptions extends DialogPane {
	private final ButtonType doneButton =
		new ButtonType("Done", ButtonBar.ButtonData.OK_DONE);
	
	private final Button resetButton = new Button("Reset to defaults");
	private final VBox content = new VBox();
	private final GridPane headers = new GridPane();
	private final GridPane bindings = new GridPane();
	private final ScrollPane bindingsWrapper;

	private static final double KeysHeight = 300d;

	public KeyboardOptions(final ClientConfig config) {
		bindingsWrapper = new ScrollPane(bindings);
		headers.addRow(0,
			new Label(),
			new Label("Primary key"),
			new Label("Secondary key"));
		content.getChildren().addAll(headers, bindingsWrapper, resetButton);

		bindingsWrapper.setPrefHeight(KeysHeight);
		bindingsWrapper.setMinHeight(KeysHeight);
		bindingsWrapper.setMaxHeight(KeysHeight);

		this.setHeaderText("Keyboard bindings");
		this.setContent(content);
		this.getButtonTypes().addAll(ButtonType.CANCEL, doneButton);

		initialize(config.getKeyBindingTable());
	}

	private void initialize(final KeyBindingTable table) {
		bindings.getChildren().clear();

		final Map<KeyBinding, List<KeyCodeCombination>> reverseTable =
			new HashMap<>();

		for (final KeyCodeCombination k : table.keys.keySet()) {
			final KeyBinding b = table.keys.get(k);
			final List<KeyCodeCombination> l =
				reverseTable.getOrDefault(b, new ArrayList<>());
			l.add(k);
			reverseTable.put(b, l);
		}

		int rowNum = 0;
		for (final KeyBinding b : InTheZoneKeyBinding.allBindings()) {
			final Label action = new Label(b.toString());
			final List<KeyCodeCombination> l =
				reverseTable.getOrDefault(b, new ArrayList<>());
			final KeyField primary = new KeyField(
				Optional.ofNullable(l.size() > 0 ? l.get(0) : null));
			final KeyField secondary = new KeyField(
				Optional.ofNullable(l.size() > 1 ? l.get(1) : null));

			primary.setPromptText("click to set key");
			secondary.setPromptText("click to set key");

			bindings.addRow(rowNum, action, primary, secondary);
			rowNum += 1;
		}
	}
}

