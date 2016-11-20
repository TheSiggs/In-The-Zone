package inthezone.game.lobby;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.battle.data.Player;
import inthezone.game.ClientConfig;
import inthezone.game.DialogScreen;
import isogame.engine.CorruptDataException;
import isogame.engine.FacingDirection;
import isogame.engine.Highlighter;
import isogame.engine.MapPoint;
import isogame.engine.MapView;
import isogame.engine.Sprite;
import isogame.engine.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gather all the information required to issue / respond to a challenge.
 * */
public class ChallengePane extends DialogScreen<StartBattleCommandRequest> {
	private final ObservableList<String> stages =
		FXCollections.observableArrayList();
	private final ObservableList<Loadout> loadouts =
		FXCollections.observableArrayList();
	
	private final BorderPane guiRoot = new BorderPane();
	private final FlowPane toolbar = new FlowPane();
	private final ComboBox<String> stage = new ComboBox<>(stages);
	private final ComboBox<Loadout> loadout = new ComboBox<>(loadouts);
	private final Button cancelButton = new Button("Cancel");
	private final Button doneButton = new Button("Done");

	private final CharacterSelector characterSelector = new CharacterSelector();

	private final Player player;
	private Stage currentStage = null;

	private final MapView startPosChooser;

	private final Map<String, MapPoint> characterPositions = new HashMap<>();
	private final Map<MapPoint, CharacterProfile> characterByPosition = new HashMap<>();

	/**
	 * @param gameData The game data
	 * @param useStage Force the player to use the indicated stage.  Used when
	 * accepting a challenge.
	 * @param player The player to play.  For the challenger, chosen randomly.
	 * For the challenged, the opposite of the challenger.
	 * */
	public ChallengePane(
		GameDataFactory gameData, ClientConfig config,
		Optional<String> useStage, Player player
	)
		throws CorruptDataException
	{
		super();
		this.player = player;
		this.setMinSize(0, 0);

		toolbar.setFocusTraversable(false);
		stage.setFocusTraversable(false);
		loadout.setFocusTraversable(false);
		cancelButton.setFocusTraversable(false);
		doneButton.setFocusTraversable(false);

		doneButton.setDisable(true);

		toolbar.getChildren().addAll(
			new Label("Stage"), stage,
			new Label("Loadout"), loadout,
			cancelButton, doneButton);
		toolbar.setStyle("-fx-background-color:#FFFFFF");
		guiRoot.setTop(toolbar);

		final HBox ccentre = new HBox();
		ccentre.setAlignment(Pos.CENTER);
		ccentre.getChildren().addAll(characterSelector);
		guiRoot.setBottom(ccentre);

		gameData.getStages().stream().map(x -> x.name).forEach(n -> stages.add(n));
		for (Loadout l : config.loadouts) loadouts.add(l);

		final Highlighter[] highlights =
			new Highlighter[] {new Highlighter(Color.rgb(0x00, 0x00, 0xFF, 0.2))};

		this.startPosChooser = new MapView(this,
			useStage.map(s -> gameData.getStage(s)).orElse(null),
			true, false, highlights);
		startPosChooser.widthProperty().bind(this.widthProperty());
		startPosChooser.heightProperty().bind(this.heightProperty());
		startPosChooser.startAnimating();

		startPosChooser.setFocusTraversable(true);
		// make sure other controls can't take focus
		stage.focusedProperty().addListener(x -> {
			if (stage.isFocused()) startPosChooser.requestFocus();
		});
		loadout.focusedProperty().addListener(x -> {
			if (loadout.isFocused()) startPosChooser.requestFocus();
		});

		this.getChildren().addAll(startPosChooser, guiRoot);

		stage.getSelectionModel().selectedItemProperty().addListener((o, s0, s) -> {
			if (s != null) {
				currentStage = gameData.getStage(s);
				startPosChooser.setStage(currentStage);
				Collection<MapPoint> tiles = player == Player.PLAYER_A?
					currentStage.terrain.getPlayerStartTiles() :
					currentStage.terrain.getAIStartTiles();
				startPosChooser.setSelectable(tiles);
				startPosChooser.setHighlight(tiles, 0);
			}
		});

		if (useStage.isPresent()) {
			String s = useStage.get();
			if (!stages.contains(s))
				throw new CorruptDataException("Unknown stage " + s);
			stage.getSelectionModel().select(s);
			stage.setDisable(true);
		}

		loadout.getSelectionModel().selectedItemProperty().addListener((o, s0, s) -> {
			if (s != null) {
				characterSelector.setCharacters(s.characters);
			}
		});

		startPosChooser.doOnSelection(p -> {
			Optional<CharacterProfile> o = characterSelector.getSelectedCharacter();
			o.ifPresent(c -> {
				if (currentStage != null) {
					Sprite s = new Sprite(c.rootCharacter.sprite);
					s.setAnimation("walk");
					s.pos = p;
					s.direction = FacingDirection.UP;

					characterSelector.setSelectedCharacter(Optional.empty());

					if (characterByPosition.containsKey(p)) {
						currentStage.clearTileOfSprites(p);
						characterSelector.setSelectedCharacter(
							Optional.ofNullable(characterByPosition.get(p)));
						characterPositions.remove(characterByPosition.get(p).rootCharacter.name);
					}

					if (characterPositions.containsKey(c.rootCharacter.name)) {
						MapPoint oldP = characterPositions.get(c.rootCharacter.name);
						currentStage.clearTileOfSprites(oldP);
						characterByPosition.remove(oldP);
					}
					currentStage.addSprite(s);
					characterPositions.put(c.rootCharacter.name, p);
					characterByPosition.put(p, c);

					Loadout l = loadout.getSelectionModel().getSelectedItem();
					doneButton.setDisable(
						l == null || characterPositions.size() != l.characters.size());
				}
			});
		});

		cancelButton.setOnAction(event -> {
			onDone.accept(Optional.empty());
		});

		doneButton.setOnAction(event -> {
			String s = stage.getSelectionModel().getSelectedItem();
			Loadout l = loadout.getSelectionModel().getSelectedItem();
			if (s == null || l == null ||
				characterPositions.size() != l.characters.size())
			{
				Alert a = new Alert(Alert.AlertType.ERROR,
					"Stage, loadout, or placement not complete", ButtonType.OK);
				a.showAndWait();
			} else {
				List<MapPoint> startTiles = l.characters.stream()
					.map(c -> characterPositions.get(c.rootCharacter.name))
					.collect(Collectors.toList());
				onDone.accept(Optional.of(new StartBattleCommandRequest(
					s, player, l, startTiles)));
			}
		});
	}
}

