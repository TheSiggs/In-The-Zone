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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BattleInProgress implements Runnable {
	private final Battle battle;
	private final Player thisPlayer;
	private final boolean thisPlayerGoesFirst;
	private final BattleListener listener;

	private final BlockingQueue<Action> commandRequests =
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

	private class Action {
		public Optional<CommandRequest> crq = Optional.empty();

		// the subject of move range and targeting information requests
		public Character subject = null;
		public Optional<CompletableFuture<Collection<MapPoint>>> moveRange = Optional.empty();
		public Optional<CompletableFuture<Collection<MapPoint>>> targeting = Optional.empty();

		public Action(CommandRequest crq) {
			this.crq = Optional.of(crq);
		}

		public Action(
			Character subject,
			CompletableFuture<Collection<MapPoint>> moveRange,
			CompletableFuture<Collection<MapPoint>> targeting
		) {
			this.subject = subject;
			this.moveRange = Optional.ofNullable(moveRange);
			this.targeting = Optional.ofNullable(targeting);
		}
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
				Action a = commandRequests.take();

				// handle a command request
				a.crq.ifPresent(crq -> {
					try {
						Command cmd = crq.makeCommand(battle.battleState);
						Platform.runLater(() -> listener.command(cmd));
						// TODO: hook into network code here
						if (cmd instanceof EndTurnCommand) {
							return;
						}
					} catch (CommandException e) {
						Platform.runLater(() -> listener.badCommand(e));
					}
				});

				// handle a move range request
				a.moveRange.ifPresent(moveRange ->
					moveRange.complete(computeMoveRange(a.subject)));
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}

	private void otherTurn() {
		// TODO: hook into network code here
		return;
	}

	/**
	 * Put an action on the queue, retrying if interrupted
	 * */
	private void queueActionWithRetry(Action a) {
		boolean retry = true;
		while (retry) {
			try {
				commandRequests.put(a);
				retry = false;
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	public void requestCommand(CommandRequest cmd) {
		queueActionWithRetry(new Action(cmd));
	}

	public Future<Collection<MapPoint>> getMoveRange(Character c) {
		CompletableFuture<Collection<MapPoint>> r = new CompletableFuture<>();
		queueActionWithRetry(new Action(c, r, null));
		return r;
	}

	private Collection<MapPoint> computeMoveRange(Character c) {
		Set<MapPoint> r = new HashSet<>();
		int w = battle.battleState.terrain.terrain.w;
		int h = battle.battleState.terrain.terrain.h;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				MapPoint p = new MapPoint(x, y);
				if (r.contains(p)) continue;
				List<MapPoint> path = battle.battleState.findPath(c.getPos(), p, c.player);
				if (battle.battleState.canMove(path)) r.add(p);
			}
		}

		return r;
	}

	public Future<Collection<MapPoint>> getTargetingInfo(Character c, Ability a) {
		CompletableFuture<Collection<MapPoint>> r = new CompletableFuture<>();
		queueActionWithRetry(new Action(c, null, r));
		return r;
	}
}

