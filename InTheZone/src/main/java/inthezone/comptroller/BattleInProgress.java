package inthezone.comptroller;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.BattleOutcome;
import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.CommandRequest;
import inthezone.battle.commands.EndTurnCommand;
import inthezone.battle.commands.InstantEffectCommand;
import inthezone.battle.commands.ResignCommand;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.battle.LineOfSight;
import inthezone.battle.Targetable;
import isogame.engine.MapPoint;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class BattleInProgress implements Runnable {
	private final Battle battle;
	private final Player thisPlayer;
	private final boolean thisPlayerGoesFirst;
	private final BattleListener listener;
	private final CommandGenerator otherPlayer;

	private final Network network;
	private final BlockingQueue<Action> commandRequests =
		new LinkedBlockingQueue<>();
	
	private final Queue<Command> commandQueue = new LinkedList<>();
	
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

	private volatile boolean accepting = true;
	public void shutdownActionQueue() {
		Collection<Action> actions = new ArrayList<>();
		synchronized (this) {
			this.accepting = false;
			commandRequests.drainTo(actions);
		}

		for (Action a : actions) a.cancel();
	}

	@Override
	public void run() {
		if (!thisPlayerGoesFirst) {
			otherTurn();
		}

		Optional<BattleOutcome> outcome =
			battle.battleState.getBattleOutcome(thisPlayer);
		while (!outcome.isPresent()) {
			turn();
			outcome = battle.battleState.getBattleOutcome(thisPlayer);
			if (outcome.isPresent()) break;
			otherTurn();
			outcome = battle.battleState.getBattleOutcome(thisPlayer);
		}

		if (network != null) network.gameOver();
		final BattleOutcome finalOutcome = outcome.get();
		Platform.runLater(() -> listener.endBattle(finalOutcome));

		shutdownActionQueue();
	}

	private void turn() {
		try {
			commandQueue.addAll(battle.doTurnStart(thisPlayer));
			if (doCommands()) return;
		} catch (CommandException e) {
			Platform.runLater(() -> listener.badCommand(e));
		}

		Platform.runLater(() ->
			listener.startTurn(battle.battleState.cloneCharacters()));

		while(true) {
			try {
				Action a = commandRequests.take();

				// handle a command request
				if (a.crq.isPresent()) {
					try {
						commandQueue.addAll(a.crq.get().makeCommand(battle.battleState));
						if (doCommands()) return;
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
				a.teleportRange.ifPresent(teleportRange ->
					teleportRange.complete(computeTeleportRange(a.subject, a.range)));

				// handle targeting request
				a.targeting.ifPresent(targeting -> {
						targeting.complete(battle.battleState.getTargetableArea(
							a.subject.getPos(), a.castFrom, a.ability));
					});
				a.attackArea.ifPresent(attackArea ->
					attackArea.complete(battle.battleState.getAffectedArea(
						a.subject.getPos(), AbilityAgentType.CHARACTER, a.castFrom, a.ability, a.target)));

				// handle command completion
				if (a.completion.isPresent()) {
					try {
						Command cmd = commandQueue.peek();
						if (cmd == null)
							throw new CommandException("No command to complete");
						InstantEffectCommand i = (InstantEffectCommand) cmd;
						i.complete(a.completion.get());

						if (doCommands()) return;
					} catch (ClassCastException e) {
						Platform.runLater(() -> listener.badCommand(
							new CommandException("Expected instant effect command", e)));
					}
				}
			} catch (InterruptedException e) {
				// Do nothing
			} catch (CommandException e) {
				Platform.runLater(() -> listener.badCommand(e));
			}
		}
	}

	/**
	 * @return true to end turn, false otherwise
	 * */
	private boolean doCommands() throws CommandException {
		while (!commandQueue.isEmpty()) {
			Command cmd = commandQueue.peek();

			if (cmd instanceof InstantEffectCommand) {
				InstantEffectCommand i = (InstantEffectCommand) cmd;
				if (!i.isCompletedOrRequestCompletion()) {
					Platform.runLater(() -> listener.completeEffect(i.getEffect()));
					return false;
				}
			}

			commandQueue.poll();

			// A single command could get expanded into multiple commands if we
			// trigger any traps or zones
			List<Targetable> affected = new ArrayList<>();
			List<Command> allCmds = cmd.doCmdComputingTriggers(battle, affected);

			List<Targetable> clonedAffected = affected.stream()
				.map(t -> t.clone()).collect(Collectors.toList());
			for (Command cmd0 : allCmds) {
				Platform.runLater(() -> listener.command(cmd0, clonedAffected));

				// don't send the command to the network until it's been completed
				// locally.  This allows the commands to update themselves when we have
				// instant effects that mess with the game state.  Also important for
				// dealing with triggers.
				if (network != null) network.sendCommand(cmd0);
			}

			if (battle.battleState.getBattleOutcome(thisPlayer).isPresent()) {
				return true;
			}

			if (cmd instanceof EndTurnCommand) return true;
		}

		return false;
	}

	private void otherTurn() {
		battle.doTurnStart(thisPlayer.otherPlayer());
		Platform.runLater(() ->
			listener.endTurn(battle.battleState.cloneCharacters()));

		otherPlayer.generateCommands(battle, listener, thisPlayer.otherPlayer());
	}

	/**
	 * Put an action on the queue, retrying if interrupted
	 * */
	private synchronized void queueActionWithRetry(Action a) {
		if (!accepting) throw new RuntimeException(
			"Attempted to enqueue a action, but the queue is not accepting new actions");

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

	public synchronized void requestCommand(CommandRequest cmd) {
		if (!accepting) return;
		queueActionWithRetry(new Action(cmd));
	}

	/**
	 * Get a path for a character to a target.
	 * @return the empty list if there is no path.
	 * */
	public synchronized Future<List<MapPoint>> getPath(Character c, MapPoint target) {
		CompletableFuture<List<MapPoint>> r = new CompletableFuture<>();

		if (!accepting) r.cancel(true); else {
			queueActionWithRetry(Action.path(c, target, r));
		}
		return r;
	}

	/**
	 * Get all the points that a character could move to on this turn.
	 * */
	public synchronized Future<Collection<MapPoint>> getMoveRange(Character c) {
		CompletableFuture<Collection<MapPoint>> r = new CompletableFuture<>();

		if (!accepting) r.cancel(true); else {
			queueActionWithRetry(Action.moveRange(c, r));
		}
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

	private Collection<MapPoint> computeTeleportRange(Character c, int range) {
		Collection<MapPoint> diamond = LineOfSight.getDiamond(c.getPos(), range);
		return diamond.stream()
			.filter(p -> battle.battleState.isSpaceFree(p))
			.collect(Collectors.toList());
	}

	/**
	 * Get all of the possible targets for an ability.
	 * */
	public synchronized Future<Collection<MapPoint>> getTargetingInfo(
		Character c, MapPoint castFrom, Ability a
	) {
		CompletableFuture<Collection<MapPoint>> r = new CompletableFuture<>();

		if (!accepting) r.cancel(true); else {
			queueActionWithRetry(Action.targeting(c, castFrom, a, r));
		}
		return r;
	}

	/**
	 * Get all of the points that would be affected if we target the specified
	 * square.
	 * */
	public synchronized Future<Collection<MapPoint>> getAttackArea(
		Character c, MapPoint castFrom, MapPoint target, Ability a
	) {
		CompletableFuture<Collection<MapPoint>> r = new CompletableFuture<>();

		if (!accepting) r.cancel(true); else {
			queueActionWithRetry(Action.attackArea(c, castFrom, target, a, r));
		}
		return r;
	}

	/**
	 * Get all the tiles we can teleport to.
	 * */
	public synchronized Future<Collection<MapPoint>> getTeleportRange(
		Character c, int range
	) {
		CompletableFuture<Collection<MapPoint>> r = new CompletableFuture<>();

		if (!accepting) r.cancel(true); else {
			queueActionWithRetry(Action.teleportRange(c, range, r));
		}
		return r;
	}

	/**
	 * Complete a command (e.g. a teleport command that requires extra targeting
	 * information)
	 * */
	public synchronized void completeEffect(List<MapPoint> completion) {
		if (accepting) {
			queueActionWithRetry(new Action(completion));
		}
	}
}

