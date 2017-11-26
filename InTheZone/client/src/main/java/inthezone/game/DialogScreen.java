package inthezone.game;

import javafx.scene.layout.StackPane;
import java.util.function.Consumer;
import java.util.Optional;

/**
 * A screen that returns a value (used e.g. for the loadout editor).
 * */
public abstract class DialogScreen<T> extends StackPane {
	protected Consumer<Optional<T>> onDone = x -> {};

	public void doOnDone(Consumer<Optional<T>> onDone) {
		this.onDone = onDone;
	}
}

