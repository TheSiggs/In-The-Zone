package inthezone.game;

import inthezone.battle.BattleOutcome;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.comptroller.BattleInProgress;
import inthezone.comptroller.BattleListener;
import isogame.engine.MapView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class BattleView
	extends DialogScreen<BattleOutcome> implements BattleListener
{
	private final MapView canvas;

	public BattleView(
		StartBattleCommand startBattle, Player player, GameDataFactory gameData
	) {
		super();

		final Paint[] highlights =
			new Paint[] {
				Color.rgb(0x00, 0xFF, 0x00, 0.2),
				Color.rgb(0xFF, 0x00, 0x00, 0.2),
				Color.rgb(0x00, 0x00, 0xFF, 0.2)};

		this.canvas = new MapView(this,
			gameData.getStage(startBattle.stage), true, highlights);
		canvas.widthProperty().bind(this.widthProperty());
		canvas.heightProperty().bind(this.heightProperty());
		canvas.startAnimating();
		canvas.setFocusTraversable(true);

		startBattle.makeSprites().stream()
			.forEach(s -> canvas.getStage().addSprite(s));

		BattleInProgress battle =
			new BattleInProgress(startBattle, player, gameData, this);
		(new Thread(battle)).start();

		this.getChildren().addAll(canvas);
	}

	public void startTurn() {
	}

	public void endTurn() {
	}

	public void endBattle(boolean playerWins) {
	}

	public void badCommand(CommandException e) {
	}

	public void command(Command cmd) {
	}
}

