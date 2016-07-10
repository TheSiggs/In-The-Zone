package inthezone.game.lobby;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.game.DialogScreen;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import isogame.engine.MapView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
	private final ObservableList<String> loadouts =
		FXCollections.observableArrayList();
	
	private final FlowPane toolbar = new FlowPane();
	private final ComboBox<String> stage = new ComboBox<>(stages);
	private final ComboBox<String> loadout = new ComboBox<>(loadouts);
	private final Button doneButton = new Button("Done");

	private final int player;

	private final MapView startPosChooser;

	/**
	 * @param gameData The game data
	 * @param useStage Force the player to use the indicated stage.  Used when
	 * accepting a challenge.
	 * @param player The player (0 or 1) to play.  For the challenger, chosen
	 * randomly.  For the challenged, the opposite of the challenger.
	 * */
	public ChallengePane(
		GameDataFactory gameData, Optional<String> useStage, int player
	)
		throws CorruptDataException
	{
		super();

		this.player = player;

		toolbar.getChildren().addAll(
			new Label("Stage"), stage,
			new Label("Loadout"), loadout,
			doneButton);

		gameData.getStages().stream().map(x -> x.name).forEach(n -> stages.add(n));

		if (useStage.isPresent()) {
			String s = useStage.get();
			if (!stages.contains(s))
				throw new CorruptDataException("Unknown stage " + s);
			stage.getSelectionModel().select(s);
			stage.setDisable(true);
		}

		final Paint[] highlights =
			new Paint[] {Color.rgb(0x00, 0x00, 0xFF, 0.2)};

		this.startPosChooser = new MapView(this,
			useStage.map(s -> gameData.getStage(s)).orElse(null),
			true, highlights);

		this.getChildren().addAll(startPosChooser, toolbar);
	}
}

