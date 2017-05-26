package inthezone.dataEditor;

import inthezone.battle.data.GameDataFactory;
import isogame.engine.SpriteInfo;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * A dialog box to edit ability graphics and sounds
 * */
public class AbilityGraphicsDialog extends Dialog<Boolean> {
	private final AbilityInfoModel model;
	private final GameDataFactory gameData;
	private final GridPane content = new GridPane();

	private final TextField name = new TextField();
	private final ChoiceBox<SpriteInfo> zoneTrapSprite = new ChoiceBox<>();

	private boolean changed = false;

	private final InvalidationListener markChanged = o -> {
		changed = true;
	};

	public AbilityGraphicsDialog(
		GameDataFactory gameData, AbilityInfoModel model
	) {
		this.gameData = gameData;
		this.model = model;

		name.setEditable(false);
		name.setText(model.getName());

		zoneTrapSprite.setItems(zoneTrapSprites());
		zoneTrapSprite.getSelectionModel().select(model.getZoneTrapSprite());
		model.zoneTrapSpriteProperty().bind(zoneTrapSprite.getSelectionModel().selectedItemProperty());
		zoneTrapSprite.getSelectionModel().selectedItemProperty().addListener(markChanged);

		content.addRow(0, new Label("Name"), name);
		content.addRow(1, new Label("Zone/trap sprite"), zoneTrapSprite);

		this.getDialogPane().setContent(content);
		this.getDialogPane().getButtonTypes().add(ButtonType.OK);
		this.setResultConverter(button -> changed);
	}

	private ObservableList<SpriteInfo> zoneTrapSprites() {
		final ObservableList<SpriteInfo> spriteList = FXCollections.observableArrayList();
		final int TRAP = gameData.getPriorityLevel("TRAP");
		final int ZONE = gameData.getPriorityLevel("ZONE");
		for (SpriteInfo i : gameData.getGlobalSprites()) {
			if (i.priority == ZONE || i.priority == TRAP) spriteList.add(i);
		}
		return spriteList;
	}
}

