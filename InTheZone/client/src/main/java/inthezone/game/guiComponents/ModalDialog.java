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
import javafx.scene.layout.Region;

/**
 * This class is a real oddity.  We can't use true modal dialogs (such as the
 * Alert class) because JavaFX often opens them on the wrong monitor or places
 * them underneath the main window, and they don't move with the main window.
 * The solution adopted here is add a DialogPane directly to the main stage
 * scene graph, and disable all the other controls while that DialogPane is
 * displayed.  The usual pattern is to make a StackPane, then add the main
 * content as the first child of the StackPane and a ModalDialog as the second
 * child.  The API of the ModalDialog layer is a bit unusual so take care.
 *
 * This class can only show one dialog at a time.  If another dialog is opened
 * while the first dialog is still showing, then the second dialog is added to
 * a queue and it will be shown when the first dialog is closed.
 *
 * To show overlapping modal dialogs, we must create a ModalDialog for every
 * layer we want to show. (i.e. if we want to show two dialogs at the same time
 * then we need two ModalDialogs in the scene graph.)
 * */
public class ModalDialog extends Group {
	private Queue<DialogPane> nextDialog = new LinkedList<>();
	private Optional<DialogPane> currentDialog = Optional.empty();
	public Optional<DialogPane> getCurrentDialog() { return currentDialog; }

	private final static double ALERT_WIDTH = 380d;

	public boolean isShowing() { return currentDialog.isPresent(); }

	private Runnable onShow = () -> {};
	private Runnable onClose = () -> {};

	/**
	 * Set the action to run when a dialog is displayed to the user.  This action
	 * should disable the main part of the GUI so that the user is forced to
	 * respond to the modal dialog.
	 * */
	public void setOnShow(final Runnable r) { this.onShow = r; }

	/**
	 * Set the action to run when a dialog is close.  This action should restore
	 * the main part of the GUI to its normal state, ready to respond to user
	 * input
	 * */
	public void setOnClose(final Runnable r) { this.onClose = r; }

	public ModalDialog() {
		this.setVisible(false);
	}

	/**
	 * Show a custom dialog.
	 * @param pane the dialog to display.
	 * @param continuation the action to execute when the dialog is closed.
	 * */
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

	/**
	 * Get the next dialog in the queue.
	 * */
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

	/**
	 * Simulate a pressing of the default button.
	 * */
	public void doDefault() {
		currentDialog.ifPresent(pane -> {
			pane.getButtonTypes().stream()
				.filter(def -> def.getButtonData().isDefaultButton())
				.findFirst().ifPresent(b -> pressButton(b));
		});
	}

	/**
	 * Simulate a pressing of the cancel button.
	 * */
	public void doCancel() {
		currentDialog.ifPresent(pane -> {
			pane.getButtonTypes().stream()
				.filter(def -> def.getButtonData().isCancelButton())
				.findFirst().ifPresent(b -> pressButton(b));
		});
	}

	/**
	 * Simulate pressing a button.
	 * @param b the button to simulate.
	 * */
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

	/**
	 * Manually close the current dialog.
	 * */
	public void closeModalDialog() {
		currentDialog = Optional.empty();
		this.getChildren().clear();
		this.setVisible(false);
		onClose.run();
		nextDialog();
	}

	/**
	 * Show an error dialog now.
	 * @param e the error
	 * @param header a message to show in the header area
	 * */
	public void showError(final Exception e, final String header) {
		showError(e, header, () -> {});
	}

	/**
	 * Show an error dialog now, with a custom continuation.
	 * @param e the error to show
	 * @param header a message to show in the header area
	 * @param k the continuation.
	 * */
	public void showError(
		final Exception e, final String header, final Runnable k
	) {
		final DialogPane dialog = new DialogPane();
		dialog.getButtonTypes().addAll(ButtonType.CLOSE);
		dialog.getStylesheets().add("dialogs.css");
		dialog.setHeaderText(header);
		dialog.setGraphic(null);
		dialog.setContentText(e == null? null : e.getMessage());
		dialog.setMinHeight(Region.USE_PREF_SIZE);
		dialog.setMaxWidth(ALERT_WIDTH);
		showDialog(dialog, r -> k.run());
	}

	/**
	 * Show a message dialog now.
	 * @param message the message to show
	 * */
	public void showMessage(final String message) {
		final DialogPane dialog = new DialogPane();
		dialog.getButtonTypes().addAll(ButtonType.OK);
		dialog.getStylesheets().add("dialogs.css");
		dialog.setHeaderText(message);
		dialog.setGraphic(null);
		dialog.setContentText(null);
		dialog.setMinHeight(Region.USE_PREF_SIZE);
		dialog.setMaxWidth(ALERT_WIDTH);
		showDialog(dialog, r -> {});
	}

	/**
	 * Show a confirmation dialog now.  The confirmation dialog has ButtonType.NO
	 * and ButtonType.YES.
	 * @param prompt the prompt
	 * @param message a message to show in the header
	 * @param k the continuation
	 * */
	public void showConfirmation(
		final String prompt, final String message, final Consumer<ButtonType> k
	) {
		final DialogPane dialog = new DialogPane();
		dialog.getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
		dialog.getStylesheets().add("dialogs.css");
		dialog.setHeaderText(message);
		dialog.setGraphic(null);
		dialog.setContentText(prompt);
		dialog.setMinHeight(Region.USE_PREF_SIZE);
		dialog.setMaxWidth(ALERT_WIDTH);
		showDialog(dialog, k);
	}
}

