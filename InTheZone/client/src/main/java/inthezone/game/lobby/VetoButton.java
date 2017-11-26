package inthezone.game.lobby;

import isogame.engine.Stage;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

import inthezone.battle.data.GameDataFactory;

/**
 * Custom control for the buttons on the veto panel.
 * */
public class VetoButton extends CheckBox {
	public Stage stage;

	private final AnchorPane root = new AnchorPane();
	private final ImageView background;
	private final Label name;
	private final Line cross1;
	private final Line cross2;

	private static final double cW = 64d;

	public VetoButton(final GameDataFactory gameData, final Stage stage) {
		this.stage = stage;

		this.getStyleClass().add("veto-button");

		this.setAllowIndeterminate(false);
		final Image img = gameData.getThumbnail(stage);
		double iw = img.getWidth();
		double ih = img.getWidth() * (9d/16d);

		background = new ImageView(img);
		root.setPrefHeight(ih);

		name = new Label(stage.name);
		name.setMaxWidth(Double.MAX_VALUE);
		name.setMaxHeight(Double.MAX_VALUE);

		cross1 = new Line(0, 0, cW, cW);
		cross2 = new Line(0, cW, cW, 0);
		cross1.visibleProperty().bind(selectedProperty());
		cross2.visibleProperty().bind(selectedProperty());

		cross1.getStyleClass().add("cross");
		cross2.getStyleClass().add("cross");
		cross1.setStrokeLineCap(StrokeLineCap.ROUND);
		cross2.setStrokeLineCap(StrokeLineCap.ROUND);

		AnchorPane.setLeftAnchor(cross1, 10d);
		AnchorPane.setTopAnchor(cross1, 18d);
		AnchorPane.setLeftAnchor(cross2, 10d);
		AnchorPane.setTopAnchor(cross2, 18d);

		AnchorPane.setLeftAnchor(name, 0d);
		AnchorPane.setRightAnchor(name, 0d);
		AnchorPane.setBottomAnchor(name, 0d);

		AnchorPane.setLeftAnchor(background, 0d);
		AnchorPane.setRightAnchor(background, 0d);
		AnchorPane.setTopAnchor(background, 0d);

		root.getChildren().addAll(background, name, cross1, cross2);
		setGraphic(root);
		setText(null);
	}
}

