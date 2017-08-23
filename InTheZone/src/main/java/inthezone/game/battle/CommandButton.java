package inthezone.game.battle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Popup;

public class CommandButton extends Button {
	private final Label description = new Label();
	private final Popup descriptionWindow = new Popup();

	public final BooleanProperty cannotUseThis = new SimpleBooleanProperty(false);

	public CommandButton(final Image icon, final String description) {
		super(null, new ImageView(icon));

		descriptionWindow.getScene().getStylesheets().add("HUD.css");
		this.description.getStylesheets().add("HUD.css");

		this.description.setWrapText(true);
		this.description.setPrefWidth(300);
		this.description.setText(description);
		descriptionWindow.getContent().add(this.description);
		descriptionWindow.setAutoHide(false);
		descriptionWindow.setAutoFix(true);
		descriptionWindow.setHideOnEscape(true);

		this.getStyleClass().add("command-button");
		this.description.getStyleClass().add("command-button-label");
		cannotUseThis.addListener((v, o, n) -> {
			if (n) {
				this.getStyleClass().add("command-button-disabled");
			} else {
				this.getStyleClass().remove("command-button-disabled");
			}
		});

		this.setOnMouseEntered(event -> {
			final Bounds b = this.localToScreen(this.getBoundsInLocal());
			double height = descriptionWindow.getHeight();
			descriptionWindow.show(this, b.getMinX(), b.getMinY() - height);
		});

		this.setOnMouseExited(event -> {
			descriptionWindow.hide();
		});

		setOnAction(event -> doCommand());
	}

	private Runnable action = () -> {};
	public void setButtonAction(final Runnable action) {
		this.action = action;
	}

	public void doCommand() {
		if (!cannotUseThis.get()) action.run();
	}
}

