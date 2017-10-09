package inthezone.game.guiComponents;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 * A field for entering key bindings.
 * */
public class KeyField extends TextField {
	private final Set<KeyCode> invalidKeys = new HashSet<>();

	public final ObjectProperty<KeyCodeCombination> keyProperty =
		new SimpleObjectProperty<>(null);
	
	private Optional<KeyCodeCombination> previousValue = Optional.empty();

	/**
	 * @param key the initial key to display
	 * */
	public KeyField(final Optional<KeyCodeCombination> key) {
		invalidKeys.add(KeyCode.ESCAPE);
		invalidKeys.add(KeyCode.ENTER);
		invalidKeys.add(KeyCode.CONTROL);
		invalidKeys.add(KeyCode.ALT);
		invalidKeys.add(KeyCode.SHIFT);
		invalidKeys.add(KeyCode.META);
		invalidKeys.add(KeyCode.SHORTCUT);

		addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.TAB) event.consume();
		});

		keyProperty.addListener((o, k0, k1) ->
			textProperty().set(k1 == null? "" : k1.getDisplayText()));

		key.ifPresent(keyProperty::setValue);

		this.setOnKeyReleased(event -> {
			final KeyCode code = event.getCode();
			if (invalidKeys.contains(code)) return;

			final KeyCombination.ModifierValue control =
				event.isControlDown()?
					KeyCombination.ModifierValue.DOWN :
					KeyCombination.ModifierValue.UP;

			final KeyCombination.ModifierValue shift =
				event.isShiftDown()?
					KeyCombination.ModifierValue.DOWN :
					KeyCombination.ModifierValue.UP;

			final KeyCombination.ModifierValue alt =
				event.isAltDown()?
					KeyCombination.ModifierValue.DOWN :
					KeyCombination.ModifierValue.UP;

			final KeyCombination.ModifierValue meta =
				event.isMetaDown()?
					KeyCombination.ModifierValue.DOWN :
					KeyCombination.ModifierValue.UP;

			final KeyCombination.ModifierValue shortcut =
				event.isShortcutDown()?
					KeyCombination.ModifierValue.DOWN :
					KeyCombination.ModifierValue.UP;

			final KeyCodeCombination c =
				new KeyCodeCombination(code, shift, control, alt, meta, shortcut);

			keyProperty.setValue(c);
			previousValue = Optional.empty();
		});

		this.focusedProperty().addListener((o, f0, gainingFocus) -> {
			if (gainingFocus) {
				previousValue = Optional.ofNullable(keyProperty.getValue());
				keyProperty.setValue(null);
			} else {
				previousValue.ifPresent(keyProperty::setValue);
			}
		});
	}
}

