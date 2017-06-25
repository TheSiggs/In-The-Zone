package inthezone.game.battle;

import static javafx.scene.control.ButtonBar.ButtonData;

import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.AbilityInfo;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class AbilityConfirmationDialog extends Alert {
	final Text text = new Text();
	final StackPane textWrapper = new StackPane(text);

	final ButtonType confirmButton =
		new ButtonType("Confirm", ButtonData.OK_DONE);
	
	final BattleView view;

	public AbilityConfirmationDialog(
		final BattleView view, final Window parent
	) {
		super(Alert.AlertType.CONFIRMATION);
		this.view = view;

		this.initStyle(StageStyle.UNDECORATED);
		this.initOwner(parent);
		this.initModality(Modality.NONE);
		this.setResizable(false);
		this.getDialogPane().getStylesheets().add("dialogs.css");
		this.setHeaderText(null);

		textWrapper.getStyleClass().add("text-container");
		text.setWrappingWidth(400);
		text.getStyleClass().add("text");
		this.getDialogPane().setContent(textWrapper);

		this.getButtonTypes().clear();
		this.getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

		this.setOnHiding(event -> {
			if (this.isShowing()) {
				view.modalEnd();
				view.selectCharacter(view.getSelectedCharacter());
			}
		});

		this.resultProperty().addListener((v, r0, r) -> {
			this.hide();
			view.modalEnd();
			if (r == confirmButton) continuation.run();
		});

		final Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
		stage.setAlwaysOnTop(true);
		stage.setResizable(false);
		stage.toFront();
	}

	private Runnable continuation = null;

	public void showConfirmation(
		final AbilityInfo info, final Runnable continuation
	) {
		this.continuation = continuation;

		final String description =
			(new AbilityDescription(info)).toString();
		text.setText(description);
		this.setGraphic(new ImageView(info.media.icon));

		view.modalStart();
		this.show();
	}
}

