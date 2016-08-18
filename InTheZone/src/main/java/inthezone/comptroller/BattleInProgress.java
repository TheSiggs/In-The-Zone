package inthezone.comptroller;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.CommandRequest;
import inthezone.battle.commands.EndTurnCommand;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import isogame.engine.MapPoint;
import javafx.application.Platform;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class BattleInProgress implements Runnable {
	private final Battle battle;
	private final Player thisPlayer;
	private final boolean thisPlayerGoesFirst;
	private final BattleListener listener;

	private final BlockingQueue<CommandRequest> commandRequests =
		new LinkedBlockingQueue<>();
	
	public BattleInProgress(
		StartBattleCommand cmd, Player thisPlayer,
		GameDataFactory gameData, BattleListener listener
	) {
		this(
			cmd.doCmd(gameData),
			thisPlayer,
			cmd.p1GoesFirst == (thisPlayer == Player.PLAYER_A),
			listener);
	}

	public BattleInProgress(
		Battle battle, Player thisPlayer,
		boolean thisPlayerGoesFirst, BattleListener listener
	) {
		this.battle = battle;
		this.thisPlayer = thisPlayer;
		this.thisPlayerGoesFirst = thisPlayerGoesFirst;
		this.listener = listener;
	}


	@Override
	public void run() {
		boolean gameOver = false;
		Platform.runLater(() -> listener.updateCharacters(
			battle.battleState.cloneCharacters()));

		if (!thisPlayerGoesFirst) {
			otherTurn();
		}

		while (!gameOver) {
			turn();
			otherTurn();
			// TODO: check for game over condition
		}

		// TODO: determine win condition
		Platform.runLater(() -> listener.endBattle(true));
	}

	private void turn() {
		while(true) {
			try {
				CommandRequest r = commandRequests.take();
				Command cmd = r.makeCommand(battle.battleState);
				Platform.runLater(() -> listener.command(cmd));
				// TODO: hook into network code here
				if (cmd instanceof EndTurnCommand) {
					return;
				}
			} catch (CommandException e) {
				Platform.runLater(() -> listener.badCommand(e));
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}

	private void otherTurn() {
		// TODO: hook into network code here
		return;
	}

	public void requestCommand(CommandRequest cmd) {
		boolean retry = true;
		while (retry) {
			try {
				commandRequests.put(cmd);
				retry = false;
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	public synchronized Future<Collection<MapPoint>> getMoveRange(Character c) {
		return null;
	}

	public synchronized Future<Collection<MapPoint>> getTargetingInfo(Character c, Ability a) {
		return null;
	}
}

