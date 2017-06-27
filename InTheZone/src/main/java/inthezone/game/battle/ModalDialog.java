package inthezone.game.battle;

import java.util.Optional;
import java.util.function.Consumer;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import inthezone.game.battle.BattleView;

public class ModalDialog extends Group {
	private final BattleView view;

	private Optional<DialogPane> currentDialog = Optional.empty();
	public Optional<DialogPane> getCurrentDialog() { return currentDialog; }

	private boolean isShowing = false;
	public boolean isShowing() { return isShowing; }

	public ModalDialog(final BattleView view) {
		this.view = view;
		this.setVisible(false);
	}

	public void showDialog(
		final DialogPane pane, final Consumer<ButtonType> continuation
	) {
		closeModalDialog();

		for (ButtonType bt : pane.getButtonTypes()) {
			pane.lookupButton(bt).setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					closeModalDialog();
					continuation.accept(bt);
				}
			});
		}

		view.hud.modalStart();
		currentDialog = Optional.of(pane);
		this.getChildren().add(pane);
		this.setVisible(true);
		isShowing = true;
	}

	public void doDefault() {
		currentDialog.ifPresent(pane -> {
			pane.getButtonTypes().stream()
				.filter(def -> def.getButtonData().isDefaultButton())
				.findFirst().ifPresent(b -> pressButton(b));
		});
	}

	public void doCancel() {
		currentDialog.ifPresent(pane -> {
			pane.getButtonTypes().stream()
				.filter(def -> def.getButtonData().isCancelButton())
				.findFirst().ifPresent(b -> pressButton(b));
		});
	}

	private void pressButton(final ButtonType b) {
		currentDialog.ifPresent(pane -> {
			final Node n = pane.lookupButton(b);

			final Bounds l = n.getBoundsInLocal();
			final Point2D scene = n.localToScene(l.getMinY() + 2, l.getMinY() + 2);
			final Point2D screen = n.localToScreen(l.getMinY() + 2, l.getMinY() + 2);

			n.fireEvent(new MouseEvent(
				null, n,
				MouseEvent.MOUSE_CLICKED,
				scene.getX(), scene.getY(),
				screen.getX(), screen.getY(),
				MouseButton.PRIMARY, 1,
				false, false, false, false,
				true, false, false,
				true, false, true, null));
		});
	}

	public void closeModalDialog() {
		currentDialog = Optional.empty();
		this.getChildren().clear();
		view.hud.modalEnd();
		isShowing = false;
		this.setVisible(false);
	}
}
