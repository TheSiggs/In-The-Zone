package inthezone.game.lobby;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.game.ClientConfig;
import inthezone.game.DialogScreen;
import isogame.engine.CorruptDataException;
import isogame.engine.FacingDirection;
import isogame.engine.MapPoint;
import isogame.engine.MapView;
import isogame.engine.Sprite;
import isogame.engine.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.util.Collection;
import java.util.Optional;

/**
 * Gather all the information required to issue / respond to a challenge.
 * */
public class ChallengePane extends DialogScreen<StartBattleCommandRequest> {
	private final ObservableList<String> stages =
		FXCollections.observableArrayList();
	private final ObservableList<Loadout> loadouts =
		FXCollections.observableArrayList();
	private final ObservableList<CharacterProfile> charactersModel =
		FXCollections.observableArrayList();
	
	private final BorderPane guiRoot = new BorderPane();
	private final FlowPane toolbar = new FlowPane();
	private final ComboBox<String> stage = new ComboBox<>(stages);
	private final ComboBox<Loadout> loadout = new ComboBox<>(loadouts);
	private final ListView<CharacterProfile> characters = new ListView<>(charactersModel);
	private final Button doneButton = new Button("Done");

	private final int player;
	private Stage currentStage = null;

	private final MapView startPosChooser;

	private final Map<String, MapPos> characterPositions = new HashMap<>();

	/**
	 * @param gameData The game data
	 * @param useStage Force the player to use the indicated stage.  Used when
	 * accepting a challenge.
	 * @param player The player (0 or 1) to play.  For the challenger, chosen
	 * randomly.  For the challenged, the opposite of the challenger.
	 * */
	public ChallengePane(
		GameDataFactory gameData, ClientConfig config,
		Optional<String> useStage, int player
	)
		throws CorruptDataException
	{
		super();
		this.player = player;

		toolbar.setFocusTraversable(false);
		stage.setFocusTraversable(false);
		loadout.setFocusTraversable(false);
		characters.setFocusTraversable(false);
		doneButton.setFocusTraversable(false);

		doneButton.setDisable(true);

		toolbar.getChildren().addAll(
			new Label("Stage"), stage,
			new Label("Loadout"), loadout,
			doneButton);
		toolbar.setStyle("-fx-background-color:#FFFFFF");
		guiRoot.setTop(toolbar);

		characters.setPrefHeight(4 * 30);
		AnchorPane charactersAnchor = new AnchorPane();
		AnchorPane.setLeftAnchor(characters, 0.0);
		AnchorPane.setBottomAnchor(characters, 0.0);
		charactersAnchor.getChildren().add(characters);
		guiRoot.setLeft(charactersAnchor);

		gameData.getStages().stream().map(x -> x.name).forEach(n -> stages.add(n));
		for (Loadout l : config.loadouts) loadouts.add(l);

		final Paint[] highlights =
			new Paint[] {Color.rgb(0x00, 0x00, 0xFF, 0.2)};

		this.startPosChooser = new MapView(this,
			useStage.map(s -> gameData.getStage(s)).orElse(null),
			true, highlights);
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
		characters.focusedProperty().addListener(x -> {
			if (characters.isFocused()) startPosChooser.requestFocus();
		});

		this.getChildren().addAll(startPosChooser, guiRoot);

		stage.getSelectionModel().selectedItemProperty().addListener((o, s0, s) -> {
			if (s != null) {
				currentStage = gameData.getStage(s);
				startPosChooser.setStage(currentStage);
				Collection<MapPoint> tiles = player == 0?
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
				charactersModel.clear();
				charactersModel.addAll(s.characters);
			}
		});

		startPosChooser.doOnSelection(p -> {
			CharacterProfile c = characters.getSelectionModel().getSelectedItem();
			if (c != null && currentStage != null && !characterPositions.containsValue(p)) {
				Sprite s = new Sprite(c.rootCharacter.sprite);
				s.pos = p;
				s.direction = FacingDirection.UP;

				if (characterPositions.containsKey(c.rootCharacter.name)) {
					currentStage.removeSprite(characterPositions.get(c.rootCharacter.name));
				}
				currentStage.addSprite(s);
				characterPositions.put(c.rootCharacter.name, p);

				Loadout l = loadout.getSelectionModel().getSelectedItem;
				doneButton.setDisable(
					l == null || characterPositions.size != l.characters.size);
			}
		});

		doneButton.setOnAction(event -> {
		});
	}
}

