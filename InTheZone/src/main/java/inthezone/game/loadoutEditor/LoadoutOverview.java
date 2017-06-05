package inthezone.game.loadoutEditor;

import inthezone.battle.data.Loadout;
import inthezone.game.ContentPane;
import inthezone.game.DialogScreen;
import isogame.engine.CorruptDataException;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.Optional;

public class LoadoutOverview extends DialogScreen<Void> {
	private final AnchorPane root = new AnchorPane();
	private final Button newLoadout = new Button("New loadout");
	private final Button back = new Button("Back");
	private final Label title = new Label("Loadouts");
	private final ScrollBar scrollLoadouts = new ScrollBar();
	private final ScrollPane loadoutsWrapper = new ScrollPane();
	private final HBox loadouts = new HBox(-16);
	private final VBox centerWrapper = new VBox(32);

	private final ContentPane parent;
	
	public LoadoutOverview(ContentPane parent) {
		this.parent = parent;

		this.getStylesheets().add("/GUI.css");
		this.getStyleClass().add("gui-pane");

		newLoadout.getStyleClass().add("gui-button");
		back.getStyleClass().add("gui-button");

		newLoadout.setOnMouseClicked(event -> newLoadout());
		back.setOnMouseClicked(event -> onDone.accept(null));

		title.getStyleClass().add("panel-title");
		title.setAlignment(Pos.BASELINE_CENTER);
		loadoutsWrapper.getStyleClass().add("scroll-loadouts");
		scrollLoadouts.getStyleClass().add("loadouts-scrollbar");

		loadoutsWrapper.setHbarPolicy(ScrollBarPolicy.NEVER);
		loadoutsWrapper.setVbarPolicy(ScrollBarPolicy.NEVER);
		loadoutsWrapper.setContent(loadouts);

		AnchorPane.setTopAnchor(newLoadout, 10d);
		AnchorPane.setLeftAnchor(newLoadout, 10d);

		AnchorPane.setTopAnchor(back, 10d);
		AnchorPane.setRightAnchor(back, 10d);

		AnchorPane.setTopAnchor(title, 20d);
		AnchorPane.setLeftAnchor(title, 0d);
		AnchorPane.setRightAnchor(title, 0d);

		AnchorPane.setTopAnchor(centerWrapper, 0d);
		AnchorPane.setBottomAnchor(centerWrapper, 0d);
		AnchorPane.setLeftAnchor(centerWrapper, 20d);
		AnchorPane.setRightAnchor(centerWrapper, 20d);

		scrollLoadouts.maxProperty().bind(loadoutsWrapper.hmaxProperty());
		scrollLoadouts.minProperty().bind(loadoutsWrapper.hminProperty());
		loadoutsWrapper.hvalueProperty().bind(scrollLoadouts.valueProperty());

		final Separator spacer1 = new Separator(Orientation.VERTICAL);
		final Separator spacer2 = new Separator(Orientation.VERTICAL);
		spacer1.setMinHeight(94);
		VBox.setVgrow(spacer1, Priority.ALWAYS);
		VBox.setVgrow(spacer2, Priority.ALWAYS);
		centerWrapper.getChildren().addAll(
			spacer1, scrollLoadouts, loadoutsWrapper, spacer2);

		loadoutsWrapper.setPannable(true);
		loadoutsWrapper.viewportBoundsProperty().addListener(v -> adjustScrollbar());
		loadouts.boundsInLocalProperty().addListener(v -> adjustScrollbar());

		try {
			for (Loadout l : parent.config.loadouts) {
				final LoadoutModel m = new LoadoutModel(parent.gameData, l);
				loadouts.getChildren().add(new LoadoutFrame(parent, this, Optional.of(m)));
			}
			loadouts.getChildren().add(new LoadoutFrame(parent, this, Optional.empty()));
		} catch (CorruptDataException e) {
			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Game data corrupt");
			a.showAndWait();
			System.exit(1);
		}

		root.getChildren().addAll(centerWrapper, title, newLoadout, back);
		this.getChildren().add(root);
	}

	private void adjustScrollbar() {
		final Bounds vp = loadoutsWrapper.getViewportBounds();
		final Bounds cn = loadouts.getBoundsInLocal();

		if (cn.getWidth() <= vp.getWidth()) {
			scrollLoadouts.setVisible(false);
		} else {
			scrollLoadouts.setVisible(true);
			scrollLoadouts.setVisibleAmount((vp.getWidth() / cn.getWidth()));
			scrollLoadouts.layout();
		}
	}

	public void newLoadout() {
		try {
			final LoadoutModel m = new LoadoutModel(parent.gameData);
			final LoadoutFrame cell = new LoadoutFrame(parent, this, Optional.of(m));
			loadouts.getChildren().add(0, cell);
			parent.showScreen(
				new LoadoutView(parent.config, parent.gameData, m),
				v -> cell.updateView());

		} catch (CorruptDataException e) {
			Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
			a.setHeaderText("Game data corrupt");
			a.showAndWait();
			System.exit(1);
		}
	}
}

class LoadoutFrame extends VBox {
	private final Optional<LoadoutModel> model;
	private final StackPane frame = new StackPane();
	private final Label title = new Label(" ");

	private static final double frameW = 651;
	private static final double frameH = 435;

	private static final double portraitW = 100;
	private static final double portraitH = 380;

	public LoadoutFrame(
		ContentPane parent, LoadoutOverview overview, Optional<LoadoutModel> model
	) {
		this.model = model;

		this.getStyleClass().add("loadout-cell");
		frame.getStyleClass().add("loadout-cell-frame");

		this.setOnMouseClicked(event -> {
			if (model.isPresent()) {
				parent.showScreen(
					new LoadoutView(parent.config, parent.gameData, model.get()),
					v -> updateView());
			} else {
				overview.newLoadout();
			}
		});

		frame.setMinWidth(frameW);
		frame.setMaxWidth(frameW);
		frame.setMinHeight(frameH);
		frame.setMaxHeight(frameH);

		this.setAlignment(Pos.TOP_CENTER);
		this.getChildren().addAll(frame, title);

		updateView();
	}

	public void updateView() {
		if (model.isPresent()) {
			final HBox characters = new HBox();
			characters.setMaxWidth(frameW);
			for (CharacterProfileModel c : model.get().usedProfiles) {
				final StackPane wrapper = new StackPane();
				wrapper.setMinWidth(100);
				final ImageView img =
					new ImageView(c.profileProperty().get().rootCharacter.bigPortrait);
				img.setPreserveRatio(true);
				img.setFitHeight(portraitH);
				wrapper.getChildren().add(img);
				characters.getChildren().add(wrapper);
			}
			frame.getChildren().clear();
			frame.getChildren().add(characters);

			title.setText(model.get().name.get());

		} else {
			frame.getChildren().clear();
			frame.getChildren().add(new Label("New loadout"));
		}
	}
}


