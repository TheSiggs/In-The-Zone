package inthezone.game.lobby;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.battle.data.Player;
import inthezone.game.ClientConfig;
import inthezone.game.DialogScreen;
import inthezone.game.guiComponents.RollingChoiceBox;
import isogame.engine.CorruptDataException;
import isogame.engine.FacingDirection;
import isogame.engine.KeyBinding;
import isogame.engine.MapPoint;
import isogame.engine.MapView;
import isogame.engine.Sprite;
import isogame.engine.Stage;

/**
 * Gather all the information required to issue / respond to a challenge.
 * */
public class ChallengePane extends DialogScreen<StartBattleCommandRequest> {
	private final ObservableList<String> stages =
		FXCollections.observableArrayList();
	private final ObservableList<Loadout> loadouts =
		FXCollections.observableArrayList();
	
	private final AnchorPane guiRoot = new AnchorPane();
	private final RollingChoiceBox<String> stage = new RollingChoiceBox<>(stages);
	private final ComboBox<Loadout> loadout = new ComboBox<>(loadouts);
	private final VBox buttonsPanel = new VBox(10);
	private final Label challengingLabel = new Label();
	private final Button cancelButton = new Button("Cancel");
	private final Button doneButton = new Button("Challenge!");

	private final CharacterSelector characterSelector = new CharacterSelector();

	private final VBox characterInfoLeft = new VBox(10);
	private final VBox characterInfoRight = new VBox(10);

	private final Player player;
	private final String thisPlayerName;
	private final String otherPlayerName;
	private Stage currentStage = null;

	private final MapView startPosChooser;

	private final Map<String, MapPoint> characterPositions = new HashMap<>();
	private final Map<MapPoint, CharacterProfile> characterByPosition =
		new HashMap<>();
	
	private final GameDataFactory gameData;

	/**
	 * @param gameData The game data
	 * @param useStage Force the player to use the indicated stage.  Used when
	 * accepting a challenge.
	 * @param player The player to play.  For the challenger, chosen randomly.
	 * For the challenged, the opposite of the challenger.
	 * */
	public ChallengePane(
		final GameDataFactory gameData,
		final ClientConfig config,
		final Optional<String> useStage,
		final Player player,
		final String thisPlayerName,
		final String otherPlayerName
	)
		throws CorruptDataException
	{
		this.player = player;
		this.thisPlayerName = thisPlayerName;
		this.otherPlayerName = otherPlayerName;
		this.gameData = gameData;
		this.setMinSize(0, 0);
		this.getStylesheets().add("/GUI.css");

		stage.setFocusTraversable(false);
		loadout.setFocusTraversable(false);
		cancelButton.setFocusTraversable(false);
		doneButton.setFocusTraversable(false);

		setDoneButtonState();

		buttonsPanel.getStyleClass().addAll("panel", "padded-panel");
		challengingLabel.getStyleClass().add("character-indicator-panel-title");
		doneButton.getStyleClass().addAll("gui-button", "gui-green-button");
		cancelButton.getStyleClass().add("gui-button");
		doneButton.setMaxWidth(Double.MAX_VALUE);
		cancelButton.setMaxWidth(Double.MAX_VALUE);
		buttonsPanel.setFillWidth(true);
		challengingLabel.setText("Challenging: " + otherPlayerName);
		buttonsPanel.setMaxWidth(220);
		buttonsPanel.setMinWidth(220);

		final StackPane doneButtonWrapper = new StackPane(doneButton);
		Tooltip.install(doneButtonWrapper, new Tooltip(
			"Place all your characters on the map to start the challenge"));

		doneButton.setTooltip(new Tooltip(
			"Click here to challenge " + otherPlayerName + " to battle"));

		buttonsPanel.getChildren().addAll(
			challengingLabel, cancelButton, doneButtonWrapper);

		loadout.getStyleClass().add("gui-combo");
		loadout.setPromptText("Choose loadout");
		stage.setPromptText("Choose map");

		Tooltip.install(stage, new Tooltip("Click here to choose a map"));
		loadout.setTooltip(new Tooltip("Click here to choose your loadout"));
		cancelButton.setTooltip(new Tooltip(
			"Cancel this challenge and return to the lobby"));

		AnchorPane.setTopAnchor(loadout, 0d);
		AnchorPane.setLeftAnchor(loadout, 0d);

		final StackPane stageWrapper = new StackPane(stage);
		AnchorPane.setTopAnchor(stageWrapper, 0d);
		AnchorPane.setLeftAnchor(stageWrapper, 0d);
		AnchorPane.setRightAnchor(stageWrapper, 0d);

		AnchorPane.setTopAnchor(buttonsPanel, 0d);
		AnchorPane.setRightAnchor(buttonsPanel, 0d);

		AnchorPane.setBottomAnchor(characterInfoLeft, 0d);
		AnchorPane.setLeftAnchor(characterInfoLeft, 0d);

		AnchorPane.setBottomAnchor(characterInfoRight, 0d);
		AnchorPane.setRightAnchor(characterInfoRight, 0d);

		final StackPane characterSelectorWrapper =
			new StackPane(characterSelector);
		AnchorPane.setBottomAnchor(characterSelectorWrapper, 0d);
		AnchorPane.setLeftAnchor(characterSelectorWrapper, 0d);
		AnchorPane.setRightAnchor(characterSelectorWrapper, 0d);

		guiRoot.getChildren().addAll(
			characterSelectorWrapper,
			characterInfoLeft, characterInfoRight,
			stageWrapper, loadout, buttonsPanel);

		gameData.getStages().stream().map(x -> x.name).forEach(n -> stages.add(n));
		for (Loadout l : config.loadouts) loadouts.add(l);

		final Paint[] highlights =
			new Paint[] {Color.rgb(0x00, 0x00, 0xFF, 0.2)};

		this.startPosChooser = new MapView(this,
			useStage.map(s -> gameData.getStage(s)).orElse(null),
			true, false, highlights);
		startPosChooser.startAnimating();

		startPosChooser.keyBindings.keys.put(
			new KeyCodeCombination(KeyCode.W), KeyBinding.scrollUp);
		startPosChooser.keyBindings.keys.put(
			new KeyCodeCombination(KeyCode.A), KeyBinding.scrollLeft);
		startPosChooser.keyBindings.keys.put(
			new KeyCodeCombination(KeyCode.S), KeyBinding.scrollDown);
		startPosChooser.keyBindings.keys.put(
			new KeyCodeCombination(KeyCode.D), KeyBinding.scrollRight);

		startPosChooser.setFocusTraversable(true);
		// make sure other controls can't take focus
		stage.focusedProperty().addListener(x -> {
			if (stage.isFocused()) startPosChooser.requestFocus();
		});
		loadout.focusedProperty().addListener(x -> {
			if (loadout.isFocused()) startPosChooser.requestFocus();
		});

		this.getChildren().addAll(startPosChooser, guiRoot);

		stage.getSelectionModel().selectedItemProperty()
			.addListener((o, s0, s) -> setStage(s));
	
		if (useStage.isPresent()) {
			final String s = useStage.get();
			if (!stages.contains(s))
				throw new CorruptDataException("Unknown stage " + s);
			stage.getSelectionModel().select(s);
			stage.setDisable(true);
			doneButton.setText("Go!");
		}

		loadout.getSelectionModel().selectedItemProperty()
			.addListener((o, s0, s) -> setLoadout(s));

		startPosChooser.doOnSelection((selection, button) -> {
			if (button == MouseButton.PRIMARY) {
				selection.spritePriority().ifPresent(p -> placeCharacter(p));
			}
		});

		cancelButton.setOnAction(event -> {
			startPosChooser.stopAnimating();
			onDone.accept(Optional.empty());
		});

		doneButton.setOnAction(event -> {
			final String s = stage.getSelectionModel().getSelectedItem();
			final Loadout l = loadout.getSelectionModel().getSelectedItem();
			if (s == null || l == null ||
				characterPositions.size() != l.characters.size())
			{
				final Alert a = new Alert(Alert.AlertType.ERROR,
					"Stage, loadout, or placement not complete", ButtonType.OK);
				a.showAndWait();
			} else {
				final List<MapPoint> startTiles = l.characters.stream()
					.map(c -> characterPositions.get(c.rootCharacter.name))
					.collect(Collectors.toList());
				startPosChooser.stopAnimating();
				onDone.accept(Optional.of(new StartBattleCommandRequest(
					s, player, thisPlayerName, l, startTiles)));
			}
		});
	}

	private void setDoneButtonState() {
		final String s = stage.getSelectionModel().getSelectedItem();
		final Loadout l = loadout.getSelectionModel().getSelectedItem();

		final boolean disable = s == null || l == null ||
			characterPositions.size() != l.characters.size() ;

		doneButton.setDisable(disable);
	}

	private void setLoadout(final Loadout l) {
		if (l != null && l.isLegitimate()) {
			characterSelector.setCharacters(l.characters, player);

			for (MapPoint p : characterPositions.values())
				currentStage.clearTileOfSprites(p);

			characterInfoLeft.getChildren().clear();
			characterInfoRight.getChildren().clear();

			characterPositions.clear();
			characterByPosition.clear();

			for (int i = 0; i < l.characters.size(); i++) {
				final CharacterProfile profile = l.characters.get(i);
				final SmallCharacterInfoPanel panel =
					new SmallCharacterInfoPanel(profile);
				panel.setOnMouseClicked(event ->
					characterSelector.setSelectedCharacter(Optional.of(profile)));

				if (i < 2) characterInfoLeft.getChildren().add(panel);
				else characterInfoRight.getChildren().add(panel);
			}

		}

		setDoneButtonState();
	}

	private void setStage(final String s) {
		if (s != null) {
			characterPositions.clear();
			characterByPosition.clear();

			this.currentStage = gameData.getStage(s);
			startPosChooser.setStage(currentStage);
			final Collection<MapPoint> tiles = player == Player.PLAYER_A?
				currentStage.terrain.getPlayerStartTiles() :
				currentStage.terrain.getAIStartTiles();
			if (tiles.size() > 0) {
				startPosChooser.centreOnTile(tiles.iterator().next());
			}
			startPosChooser.setSelectable(tiles);
			startPosChooser.setHighlight(tiles, 0);
		}

		setDoneButtonState();
	}

	private void placeCharacter(final MapPoint p) {
		final Optional<CharacterProfile> o =
			characterSelector.getSelectedCharacter();

		o.ifPresent(c -> {
			if (currentStage != null) {
				final Sprite s = new Sprite(player == Player.PLAYER_A?
					c.rootCharacter.spriteA : c.rootCharacter.spriteB);
				s.setAnimation("walk");
				s.setPos(p);
				s.setDirection(FacingDirection.UP);

				characterSelector.setSelectedCharacter(Optional.empty());

				if (characterByPosition.containsKey(p)) {
					currentStage.clearTileOfSprites(p);
					characterSelector.setSelectedCharacter(
						Optional.ofNullable(characterByPosition.get(p)));
					characterPositions.remove(characterByPosition.get(p)
						.rootCharacter.name);
				}

				if (characterPositions.containsKey(c.rootCharacter.name)) {
					MapPoint oldP = characterPositions.get(c.rootCharacter.name);
					currentStage.clearTileOfSprites(oldP);
					characterByPosition.remove(oldP);
				}
				currentStage.addSprite(s);
				characterPositions.put(c.rootCharacter.name, p);
				characterByPosition.put(p, c);

				final Loadout l = loadout.getSelectionModel().getSelectedItem();
				doneButton.setDisable(
					l == null || characterPositions.size() != l.characters.size());
			}
		});

		setDoneButtonState();
	}
}

