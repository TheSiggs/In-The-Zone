package inthezone.game.battle;

import static javafx.scene.control.ButtonBar.ButtonData;

import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.AbilityInfo;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class AbilityConfirmationDialog extends DialogPane {
	private final Text text = new Text();
	private final StackPane textWrapper = new StackPane(text);

	public final ButtonType confirmButton =
		new ButtonType("Confirm", ButtonData.OK_DONE);
	
	public AbilityConfirmationDialog(final AbilityInfo info) {
		this.getStylesheets().add("dialogs.css");
		this.setHeaderText(null);

		textWrapper.getStyleClass().add("text-container");
		text.setWrappingWidth(400);
		text.getStyleClass().add("text");
		this.setContent(textWrapper);

		this.getButtonTypes().clear();
		this.getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

		final String description =
			(new AbilityDescription(info)).toString();
		text.setText(description);
		this.setGraphic(new ImageView(info.media.icon));
	}
}

