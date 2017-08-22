package inthezone.game.loadoutEditor;

import isogame.engine.CorruptDataException;

import java.util.Optional;

import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Loadout;
import inthezone.game.ContentPane;
import inthezone.game.DialogScreen;
import inthezone.game.battle.ModalDialog;

public class LoadoutOverview extends DialogScreen<Void> {
	private final AnchorPane root = new AnchorPane();
	private final Button newLoadout = new Button("New loadout");
	private final Button back = new Button("Back");
	private final Label title = new Label("Loadouts");
	private final ScrollBar scrollLoadouts = new ScrollBar();
	private final ScrollPane loadoutsWrapper = new ScrollPane();
	private final HBox loadouts = new HBox(-16);
	private final VBox centerWrapper = new VBox();

	private final ContentPane parent;

	private final ModalDialog modalDialog = new ModalDialog();
	
	public LoadoutOverview(final ContentPane parent) {
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
		loadoutsWrapper.hvalueProperty().bindBidirectional(scrollLoadouts.valueProperty());

		loadoutsWrapper.setOnScroll(event -> {
			loadoutsWrapper.setHvalue(loadoutsWrapper.getHvalue() -
				(6 * event.getDeltaY() / loadouts.getWidth()));
		});

		final Separator spacer1 = new Separator(Orientation.VERTICAL);
		final Separator spacer2 = new Separator(Orientation.VERTICAL);
		spacer1.setMinHeight(88);
		VBox.setVgrow(spacer1, Priority.ALWAYS);
		VBox.setVgrow(spacer2, Priority.ALWAYS);
		centerWrapper.getChildren().addAll(
			spacer1, scrollLoadouts, loadoutsWrapper, spacer2);

		loadoutsWrapper.setPannable(true);
		loadoutsWrapper.viewportBoundsProperty().addListener(v -> adjustScrollbar());
		loadouts.boundsInLocalProperty().addListener(v -> adjustScrollbar());

		for (Loadout l : parent.config.loadouts) {
			loadouts.getChildren().add(new LoadoutFrame(
				parent, this, Optional.of(l), modalDialog));
		}
		loadouts.getChildren().add(new LoadoutFrame(
			parent, this, Optional.empty(), modalDialog));

		modalDialog.setOnShow(() -> {
			root.setMouseTransparent(true);
			modalDialog.requestFocus();
		});

		modalDialog.setOnClose(() -> root.setMouseTransparent(false));

		root.getChildren().addAll(centerWrapper, title, newLoadout, back);
		this.getChildren().addAll(root, modalDialog);

		this.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ESCAPE) onDone.accept(null);
		});

		newLoadout.setTooltip(new Tooltip("Create a new loadout"));
		back.setTooltip(new Tooltip(
			"Back to the previous screen (your loadouts will be saved)"));
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
			m.name.setValue(parent.config.newLoadoutName());
			final LoadoutFrame cell =
				new LoadoutFrame(parent, this,
					Optional.of(m.encodeLoadout()), modalDialog);
			loadouts.getChildren().add(0, cell);
			parent.config.loadouts.add(0, m.encodeLoadout());
			parent.showScreen(
				new LoadoutView(parent.config, parent.gameData, m, 0),
				v -> cell.updateView(v));

		} catch (final CorruptDataException e) {
			modalDialog.showError(e, "Game data corrupt");
			System.exit(1);
		}
	}

	public void removeLoadoutFrame(final LoadoutFrame frame) {
		loadouts.getChildren().remove(frame);
		loadoutsWrapper.layout();
	}
}

class LoadoutFrame extends VBox {
	private static final Image badLoadoutImage =
		new Image("/gui_assets/bad_loadout.png");

	private Optional<Loadout> mloadout;
	private final StackPane topSpacer = new StackPane();
	private final StackPane frame = new StackPane();
	private final HBox infoLine = new HBox(10);
	private final Label title = new Label(" ");
	private final ImageView badLoadout = new ImageView(badLoadoutImage);
	private final Button delete = new Button(null,
		new ImageView(new Image("/gui_assets/x.png")));
	
	private final Tooltip frameTooltip = new Tooltip();

	private static final double frameW = 651;
	private static final double frameH = 435;

	private static final double portraitW = 100;
	private static final double portraitH = 380;

	private static final double badLoadoutH = 320;

	private final ModalDialog modalDialog;

	public LoadoutFrame(
		final ContentPane parent,
		final LoadoutOverview overview,
		final Optional<Loadout> mloadout,
		final ModalDialog modalDialog
	) {
		this.modalDialog = modalDialog;
		this.mloadout = mloadout;

		this.getStyleClass().add("loadout-cell");
		frame.getStyleClass().add("loadout-cell-frame");
		delete.getStyleClass().add("gui-img-button");

		badLoadout.setFitHeight(badLoadoutH);
		badLoadout.setPreserveRatio(true);

		frame.setOnMouseClicked(event -> {
			if (!event.isStillSincePress()) return;
			try {
				if (this.mloadout.isPresent()) {
					final int id = parent.config.loadouts.indexOf(this.mloadout.get());
					parent.showScreen(
						new LoadoutView(parent.config, parent.gameData,
							new LoadoutModel(parent.gameData, this.mloadout.get()), id),
						v -> updateView(v));
				} else {
					overview.newLoadout();
				}
			} catch (final CorruptDataException e) {
				modalDialog.showError(e, "Game data corrupt");
				System.exit(1);
			}
		});

		delete.setOnMouseClicked(event -> {
			final String prompt =
				"Really remove loadout " + this.mloadout.get().name + "?";
			modalDialog.showConfirmation(prompt, null, bt -> {
				if (bt == ButtonType.YES) {
					parent.config.loadouts.remove(this.mloadout.get());
					parent.config.writeConfig();
					overview.removeLoadoutFrame(this);
				}
			});
		});

		frame.setMinWidth(frameW);
		frame.setMaxWidth(frameW);
		frame.setMinHeight(frameH);
		frame.setMaxHeight(frameH);

		this.setAlignment(Pos.TOP_CENTER);

		final Separator spacer1 = new Separator(Orientation.HORIZONTAL);
		final Separator spacer2 = new Separator(Orientation.HORIZONTAL);
		HBox.setHgrow(spacer1, Priority.ALWAYS);
		HBox.setHgrow(spacer2, Priority.ALWAYS);
		infoLine.setMaxWidth(frameW);
		infoLine.setAlignment(Pos.CENTER_LEFT);
		infoLine.getChildren().addAll(spacer1, title);
		if (this.mloadout.isPresent()) infoLine.getChildren().add(delete);
		infoLine.getChildren().add(spacer2);

		topSpacer.setMinHeight(32);
		topSpacer.setMaxHeight(32);
		this.getChildren().addAll(topSpacer, frame, infoLine);

		frameTooltip.setWrapText(true);
		frameTooltip.setMaxWidth(300);
		Tooltip.install(frame, frameTooltip);
		delete.setTooltip(new Tooltip("Delete this loadout"));

		updateView(this.mloadout);
	}

	public void updateView(final Optional<Loadout> mloadout) {
		this.mloadout = mloadout;

		if (this.mloadout.isPresent()) {
			final HBox characters = new HBox();
			characters.setMaxWidth(frameW);
			for (CharacterProfile c : this.mloadout.get().characters) {
				final StackPane wrapper = new StackPane();
				wrapper.setMinWidth(100);
				final ImageView img =
					new ImageView(c.rootCharacter.bigPortrait);
				img.setPreserveRatio(true);
				img.setFitHeight(portraitH);
				wrapper.getChildren().add(img);
				characters.getChildren().add(wrapper);
			}
			frame.getChildren().clear();
			if (!mloadout.get().isLegitimate()) {
				frameTooltip.setText("There is a problem with this loadout.  Click here to fix it.");
				frame.getChildren().add(badLoadout);
			} else {
				frameTooltip.setText("Click here to edit this loadout");
			}
			frame.getChildren().add(characters);

			title.setText(this.mloadout.get().name);
			title.setTooltip(new Tooltip(title.getText()));

		} else {
			frameTooltip.setText("Click here to make a new loadout");
			frame.getChildren().clear();
			frame.getChildren().add(new Label("New loadout"));
		}
	}
}


