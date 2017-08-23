package inthezone.game.guiComponents;

import isogame.engine.KeyBinding;
import isogame.engine.KeyBindingTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import inthezone.game.ClientConfig;
import inthezone.game.InTheZoneKeyBinding;

public class KeyboardOptions extends DialogPane {
	public static final ButtonType doneButton =
		new ButtonType("Done", ButtonBar.ButtonData.OK_DONE);

	public static final ButtonType resetButton =
		new ButtonType("Reset to defaults", ButtonBar.ButtonData.OTHER);
	
	private final VBox content = new VBox();
	private final GridPane headers = new GridPane();
	private final GridPane bindings = new GridPane();
	private final ScrollPane bindingsWrapper;

	private final Label header0 = new Label();
	private final Label header1 = new Label("Primary key");
	private final Label header2 = new Label("Secondary key");

	private static final double KeysHeight = 300d;
	private static final double keyFieldWidth = 180d;

	public final KeyBindingTable resultTable = new KeyBindingTable();

	@Override protected Node createButtonBar() {
		final ButtonBar node = (ButtonBar) super.createButtonBar();
		node.setButtonOrder("U+CO");
		return node;
	}

	public KeyboardOptions(final ClientConfig config) {
		this.setId("keyboard");

		bindingsWrapper = new ScrollPane(bindings);
		bindingsWrapper.setFitToWidth(true);
		header0.setMaxWidth(Double.MAX_VALUE);
		header1.setMaxWidth(keyFieldWidth);
		header2.setMaxWidth(keyFieldWidth);
		header1.setPrefWidth(keyFieldWidth);
		header2.setPrefWidth(keyFieldWidth);
		header0.setAlignment(Pos.BASELINE_LEFT);
		header1.setAlignment(Pos.BASELINE_LEFT);
		header2.setAlignment(Pos.BASELINE_LEFT);
		headers.addRow(0, header0, header1, header2);
		content.getChildren().addAll(headers, bindingsWrapper);

		final ColumnConstraints h0 =
			new ColumnConstraints(keyFieldWidth, keyFieldWidth, Double.MAX_VALUE);
		final ColumnConstraints h1 =
			new ColumnConstraints(keyFieldWidth, keyFieldWidth, keyFieldWidth);
		h0.setHgrow(Priority.ALWAYS);
		headers.getColumnConstraints().addAll(h0, h1, h1);

		initialize(config.getKeyBindingTable());

		bindingsWrapper.setPrefHeight(KeysHeight);
		bindingsWrapper.setMinHeight(KeysHeight);
		bindingsWrapper.setMaxHeight(KeysHeight);
		bindingsWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		bindingsWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		headers.getStyleClass().addAll("headers");
		bindings.getStyleClass().addAll("table-outer");
		bindings.setVgap(4d);

		this.getStylesheets().add("dialogs.css");

		this.setHeaderText("Keyboard bindings");
		this.setContent(content);
		this.getButtonTypes().addAll(resetButton, ButtonType.CANCEL, doneButton);

		lookupButton(resetButton).setOnMouseClicked(event ->
			initialize(config.defaultKeyBindingTable));
	}

	private final Map<KeyBinding, KeyField> primaries = new HashMap<>();
	private final Map<KeyBinding, KeyField> secondaries = new HashMap<>();

	private void initialize(final KeyBindingTable table) {
		resultTable.loadBindings(table);
		bindings.getChildren().clear();

		primaries.clear();
		secondaries.clear();

		final Map<KeyBinding, KeyCodeCombination> kp =
			resultTable.getPrimaryKeys();
		final Map<KeyBinding, KeyCodeCombination> ks =
			resultTable.getSecondaryKeys();

		int rowNum = 0;
		for (final KeyBinding b : InTheZoneKeyBinding.allBindings()) {
			final Label action = new Label(b.toString());
			final KeyField primary = new KeyField(Optional.ofNullable(kp.get(b)));
			final KeyField secondary = new KeyField(Optional.ofNullable(ks.get(b)));

			primary.setPromptText("click to set key");
			secondary.setPromptText("click to set key");

			action.setMaxWidth(Double.MAX_VALUE);
			action.setPrefWidth(keyFieldWidth * 1.5d);
			primary.setMaxWidth(keyFieldWidth);
			secondary.setMaxWidth(keyFieldWidth);
			primary.setPrefWidth(keyFieldWidth);
			secondary.setPrefWidth(keyFieldWidth);
			action.getStyleClass().addAll("table-row", "table-row-first");
			primary.getStyleClass().add("table-row");
			secondary.getStyleClass().add("table-row");

			primaries.put(b, primary);
			secondaries.put(b, secondary);

			primary.keyProperty.addListener((o, k0, k1) -> {
				if (k1 != null) {
					final KeyBinding last = resultTable.setPrimaryKey(b, k1);
					if (last != null && last != b) {
						primaries.get(last).keyProperty.setValue(null);
						secondaries.get(last).keyProperty.setValue(null);
					}
				}
			});
			secondary.keyProperty.addListener((o, k0, k1) -> {
				if (k1 != null) {
					final KeyBinding last = resultTable.setSecondaryKey(b, k1);
					if (last != null && last != b) {
						primaries.get(last).keyProperty.setValue(null);
						secondaries.get(last).keyProperty.setValue(null);
					}
				}
			});

			bindings.addRow(rowNum, action, primary, secondary);
			rowNum += 1;
		}
	}
}

