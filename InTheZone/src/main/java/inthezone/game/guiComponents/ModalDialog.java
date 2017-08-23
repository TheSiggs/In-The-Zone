package inthezone.game.battle;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class ModalDialog extends Group {
	private Queue<DialogPane> nextDialog = new LinkedList<>();
	private Optional<DialogPane> currentDialog = Optional.empty();
	public Optional<DialogPane> getCurrentDialog() { return currentDialog; }

	public boolean isShowing() { return currentDialog.isPresent(); }

	private Runnable onShow = () -> {};
	private Runnable onClose = () -> {};

	public void setOnShow(final Runnable r) { this.onShow = r; }
	public void setOnClose(final Runnable r) { this.onClose = r; }

	public ModalDialog() {
		this.setVisible(false);
	}

	public void showDialog(
		final DialogPane pane, final Consumer<ButtonType> continuation
	) {
		for (final ButtonType bt : pane.getButtonTypes()) {
			if (bt.getButtonData() != ButtonBar.ButtonData.OTHER) {
				pane.lookupButton(bt).setOnMouseClicked(event -> {
					if (event.getButton() == MouseButton.PRIMARY) {
						closeModalDialog();
						continuation.accept(bt);
					}
				});
			}
		}

		nextDialog.add(pane);
		nextDialog();
	}

	private void nextDialog() {
		if (!isShowing()) {
			currentDialog = Optional.ofNullable(nextDialog.poll());
			currentDialog.ifPresent(pane -> {
				this.getChildren().add(pane);
				this.setVisible(true);
				onShow.run();
			});
		}
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
		this.setVisible(false);
		onClose.run();
		nextDialog();
	}

	public void showError(final Exception e, final String header) {
		showError(e, header, () -> {});
	}

	public void showError(
		final Exception e, final String header, final Runnable k
	) {
		final DialogPane dialog = new DialogPane();
		dialog.getButtonTypes().addAll(ButtonType.CLOSE);
		dialog.getStylesheets().add("dialogs.css");
		dialog.setHeaderText(header);
		dialog.setGraphic(null);
		dialog.setContentText(e == null? null : e.getMessage());
		showDialog(dialog, r -> k.run());
	}

	public void showMessage(final String message) {
		final DialogPane dialog = new DialogPane();
		dialog.getButtonTypes().addAll(ButtonType.OK);
		dialog.getStylesheets().add("dialogs.css");
		dialog.setHeaderText(message);
		dialog.setGraphic(null);
		dialog.setContentText(null);
		showDialog(dialog, r -> {});
	}

	public void showConfirmation(
		final String prompt, final String message, final Consumer<ButtonType> k
	) {
		final DialogPane dialog = new DialogPane();
		dialog.getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
		dialog.getStylesheets().add("dialogs.css");
		dialog.setHeaderText(message);
		dialog.setGraphic(null);
		dialog.setContentText(prompt);
		showDialog(dialog, k);
	}
}

