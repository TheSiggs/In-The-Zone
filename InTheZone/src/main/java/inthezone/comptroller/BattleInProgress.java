package inthezone.comptroller;

import inthezone.ai.CommandGenerator;
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
	private final CommandGenerator otherPlayer;

	private final Network network;
	private final BlockingQueue<Action> commandRequests =
		new LinkedBlockingQueue<>();
	
	public BattleInProgress(
		StartBattleCommand cmd, Player thisPlayer,
		CommandGenerator otherPlayer,
		Network network,
		GameDataFactory gameData, BattleListener listener
	) {
		this(
			cmd.doCmd(gameData),
			thisPlayer,
			otherPlayer,
			network,
			cmd.p1GoesFirst == (thisPlayer == Player.PLAYER_A),
			listener);
	}

	/**
	 * @param network may be null if we are not in network mode
	 * */
	public BattleInProgress(
		Battle battle, Player thisPlayer,
		CommandGenerator otherPlayer,
		Network network,
		boolean thisPlayerGoesFirst, BattleListener listener
	) {
		this.battle = battle;
		this.thisPlayer = thisPlayer;
		this.otherPlayer = otherPlayer;
		this.network = network;
		this.thisPlayerGoesFirst = thisPlayerGoesFirst;
		this.listener = listener;
	}

	private class Action {
		public Optional<CommandRequest> crq = Optional.empty();

		// the subject of move range and targeting information requests
		public Character subject = null;
		public Ability ability = null;
		public MapPoint target = null;
		public Optional<CompletableFuture<Collection<MapPoint>>> moveRange = Optional.empty();
		public Optional<CompletableFuture<Collection<MapPoint>>> targeting = Optional.empty();
		public Optional<CompletableFuture<List<MapPoint>>> path = Optional.empty();

		public Action(CommandRequest crq) {
			this.crq = Optional.of(crq);
		}

		public Action(
			Character subject,
			Ability ability,
			MapPoint target,
			CompletableFuture<Collection<MapPoint>> moveRange,
			CompletableFuture<Collection<MapPoint>> targeting,
			CompletableFuture<List<MapPoint>> path
		) {
			this.subject = subject;
			this.ability = ability;
			this.target = target;
			this.moveRange = Optional.ofNullable(moveRange);
			this.targeting = Optional.ofNullable(targeting);
			this.path = Optional.ofNullable(path);
		}
	}

	@Override
	public void run() {
		boolean gameOver = false;

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
		battle.doTurnStart(thisPlayer);
		Platform.runLater(() ->
			listener.startTurn(battle.battleState.cloneCharacters()));

		while(true) {
			try {
				Action a = commandRequests.take();

				// handle a command request
				if (a.crq.isPresent()) {
					try {
						Command cmd = a.crq.get().makeCommand(battle.battleState);
						if (network != null) network.sendCommand(cmd);
						if (cmd instanceof EndTurnCommand) {
							return;
						}

						List<Character> affectedCharacters = cmd.doCmd(battle);
						Platform.runLater(() -> {
							listener.command(cmd, affectedCharacters);
						});
					} catch (CommandException e) {
						Platform.runLater(() -> listener.badCommand(e));
					}
				}

				// handle a move range request
				a.moveRange.ifPresent(moveRange ->
					moveRange.complete(computeMoveRange(a.subject)));
				a.path.ifPresent(path ->
					path.complete(battle.battleState.findValidPath(
						a.subject.getPos(), a.target, a.subject.player)));

				// handle targeting request
				a.targeting.ifPresent(targeting ->
					targeting.complete(computeTargeting(a.subject, a.ability)));
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}

	private void otherTurn() {
		battle.doTurnStart(thisPlayer.otherPlayer());
		Platform.runLater(() ->
			listener.endTurn(battle.battleState.cloneCharacters()));

		otherPlayer.generateCommands(battle, listener);
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

	/**
	 * Get a path for a character to a target.
	 * @return the empty list if there is no path.
	 * */
	public Future<List<MapPoint>> getPath(Character c, MapPoint target) {
		CompletableFuture<List<MapPoint>> r = new CompletableFuture<>();
		queueActionWithRetry(new Action(c, null, target, null, null, r));
		return r;
	}

	/**
	 * Get all the points that a character could move to on this turn.
	 * */
	public Future<Collection<MapPoint>> getMoveRange(Character c) {
		CompletableFuture<Collection<MapPoint>> r = new CompletableFuture<>();
		queueActionWithRetry(new Action(c, null, null, r, null, null));
		return r;
	}

	/**
	 * Compute all the points that a character could move to on this turn.
	 * */
	private Collection<MapPoint> computeMoveRange(Character c) {
		Set<MapPoint> r = new HashSet<>();
		int w = battle.battleState.terrain.terrain.w;
		int h = battle.battleState.terrain.terrain.h;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				MapPoint p = new MapPoint(x, y);
				List<MapPoint> path = battle.battleState.findPath(c.getPos(), p, c.player);
				if (battle.battleState.canMove(path)) r.add(p);
			}
		}

		return r;
	}

	/**
	 * Compute all the points this ability could hit.
	 * */
	private Collection<MapPoint> computeTargeting(Character c, Ability a) {
		return battle.battleState.getTargetableArea(c.getPos(), a);
	}

	/**
	 * Get all of the possible targets for an ability.
	 * */
	public Future<Collection<MapPoint>> getTargetingInfo(Character c, Ability a) {
		CompletableFuture<Collection<MapPoint>> r = new CompletableFuture<>();
		queueActionWithRetry(new Action(c, a, null, null, r, null));
		return r;
	}
}

