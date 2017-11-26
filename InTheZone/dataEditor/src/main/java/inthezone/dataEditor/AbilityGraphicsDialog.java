package inthezone.dataEditor;

import inthezone.battle.data.GameDataFactory;
import isogame.engine.SpriteInfo;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import java.io.File;
import java.io.IOException;

/**
 * A dialog box to edit ability graphics and sounds.
 * */
public class AbilityGraphicsDialog extends Dialog<Boolean> {
	private final File dataRoot;
	private final File gfxRoot;

	private final AbilityInfoModel model;
	private final GameDataFactory gameData;
	private final GridPane content = new GridPane();

	private Button icon;
	private final TextField name = new TextField();
	private final ChoiceBox<SpriteInfo> zoneTrapSprite = new ChoiceBox<>();
	private final ChoiceBox<SpriteInfo> obstacleSprite = new ChoiceBox<>();

	private boolean changed = false;

	private final InvalidationListener markChanged = o -> {
		changed = true;
	};

	public AbilityGraphicsDialog(
		GameDataFactory gameData, File dataRoot, AbilityInfoModel model
	) {
		this.dataRoot = dataRoot;
		this.gfxRoot = new File(dataRoot, "gfx");

		this.setTitle("Choose graphics and sounds");

		this.gameData = gameData;
		this.model = model;

		try {
			icon = new Button(null, makeIconImage(model.getIcon()));
		} catch (IOException e) {
			Alert error = new Alert(Alert.AlertType.ERROR);
			error.setTitle("Cannot load image from file " + model.getIcon());
			error.setHeaderText(e.toString());
			error.showAndWait();
			icon = new Button("Click to add icon");
		}

		name.setEditable(false);
		name.setText(model.getName());

		zoneTrapSprite.setItems(zoneTrapSprites());
		zoneTrapSprite.getSelectionModel().select(model.getZoneTrapSprite());
		model.zoneTrapSpriteProperty().bind(zoneTrapSprite.getSelectionModel().selectedItemProperty());
		zoneTrapSprite.getSelectionModel().selectedItemProperty().addListener(markChanged);

		obstacleSprite.setItems(obstacleSprites());
		obstacleSprite.getSelectionModel().select(model.getObstacleSprite());
		model.obstacleSpriteProperty().bind(obstacleSprite.getSelectionModel().selectedItemProperty());
		obstacleSprite.getSelectionModel().selectedItemProperty().addListener(markChanged);

		content.addRow(0, new Label("Icon"), icon);
		content.addRow(1, new Label("Name"), name);
		content.addRow(2, new Label("Obstacle sprite"), obstacleSprite);
		content.addRow(3, new Label("Zone/trap sprite"), zoneTrapSprite);

		icon.setOnAction(event -> {
			changed = true;

			FileChooser fc = new FileChooser();
			fc.setTitle("Choose icon file");
			fc.setInitialDirectory(gfxRoot);
			fc.getExtensionFilters().addAll(new ExtensionFilter("Graphics files",
				"*.png", "*.PNG", "*.jpg", "*.JPG",
				"*.jpeg", "*.JPEG", "*.bmp", "*.BMP"));
			File r = fc.showOpenDialog(this.getDialogPane().getScene().getWindow());
			if (r != null) {
				try {
					String path = gfxRoot.toPath().relativize(r.toPath()).toString();
					icon.setText(null);
					icon.setGraphic(makeIconImage(path));
					model.iconProperty().set(path);
				} catch (IOException e) {
					Alert error = new Alert(Alert.AlertType.ERROR);
					error.setTitle("Cannot load image from file " + r.toString());
					error.setHeaderText(e.toString());
					error.showAndWait();
				}
			}
		});

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

	private ObservableList<SpriteInfo> obstacleSprites() {
		final ObservableList<SpriteInfo> spriteList = FXCollections.observableArrayList();
		final int OBSTACLE = gameData.getPriorityLevel("OBSTACLE");
		for (SpriteInfo i : gameData.getGlobalSprites()) {
			if (i.priority == OBSTACLE) spriteList.add(i);
		}

		return spriteList;
	}

	private ImageView makeIconImage(String iconFile)
		throws IOException
	{
			return new ImageView(new Image(gameData.loc.gfx(iconFile)));
	}
}

