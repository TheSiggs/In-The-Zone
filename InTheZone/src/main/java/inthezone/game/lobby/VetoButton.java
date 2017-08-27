package inthezone.game.lobby;

import isogame.engine.Stage;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;

import inthezone.battle.data.GameDataFactory;

public class VetoButton extends CheckBox {
	private final StackPane root = new StackPane();
	private final ImageView background;
	private final Label name;
	private final Line cross1;
	private final Line cross2;

	public VetoButton(final GameDataFactory gameData, final Stage stage) {
		this.setAllowIndeterminate(false);
		final Image img = gameData.getThumbnail(stage);
		background = new ImageView(img);
		name = new Label(stage.name);
		cross1 = new Line(0, 0, img.getWidth(), img.getHeight());
		cross2 = new Line(0, img.getHeight(), img.getWidth(), 0);
		cross1.visibleProperty().bind(selectedProperty());
		cross2.visibleProperty().bind(selectedProperty());

		root.getChildren().addAll(background, name, cross1, cross2);
		setGraphic(root);
		setText(null);
	}
}

