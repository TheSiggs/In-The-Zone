package inthezone.game.lobby;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import isogame.engine.CorruptDataException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.Optional;

/**
 * Gather all the information required to issue / respond to a challenge.
 * */
public class ChallengePane extends VBox {
	private final ObservableList<String> stages =
		FXCollections.observableArrayList();
	private final ObservableList<String> players =
		FXCollections.observableArrayList("Player 1", "Player 2");
	private final ObservableList<String> loadouts =
		FXCollections.observableArrayList();

	public ChallengePane(GameDataFactory gameData, Optional<String> useStage)
		throws CorruptDataException
	{
		final FlowPane toolbar = new FlowPane();
		final ComboBox<String> stage = new ComboBox<>(stages);
		final ComboBox<String> player = new ComboBox<>(players);
		final ComboBox<String> loadout = new ComboBox<>(loadouts);
		toolbar.getChildren().addAll(
			new Label("Stage"), stage, player,
			new Label("Loadout"), loadout);

		gameData.getStages().stream().map(x -> x.name).forEach(n -> stages.add(n));

		if (useStage.isPresent()) {
			String s = useStage.get();
			if (!stages.contains(s))
				throw new CorruptDataException("Unknown stage " + s);
			stage.getSelectionModel().select(s);
			stage.setDisable(true);
		}

		// TODO: add a panel here to choose the starting positions

		this.getChildren().addAll(toolbar);
	}

	public StartBattleCommandRequest getStartBattleRequest() {
		// TODO: implement this method
		return null;
	}
}

