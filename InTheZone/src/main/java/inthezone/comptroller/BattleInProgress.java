package inthezone.comptroller;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Battle;
import inthezone.battle.BattleOutcome;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.CommandRequest;
import inthezone.battle.commands.EndTurnCommand;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.commands.InstantEffectCommand;
import inthezone.battle.commands.ResignCommandRequest;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartTurnCommand;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.battle.Targetable;
import inthezone.battle.Zone;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

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
	) throws CorruptDataException {
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

		for (Action a : actions)
			if (a instanceof InfoRequest) ((InfoRequest) a).cancel();
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
		final List<Zone> zones = battle.doTurnStart(thisPlayer);

		try {
			commandQueue.addAll(battle.getTurnStart(thisPlayer));

			final boolean commandsComming = !commandQueue.isEmpty();
			Platform.runLater(() -> {
				final List<Targetable> affected = new ArrayList<>();
				affected.addAll(battle.battleState.cloneCharacters());
				affected.addAll(zones);
				try {
					listener.command(
						(new StartTurnCommand(thisPlayer, affected, commandsComming))
						.doCmdComputingTriggers(battle).get(0));
				} catch (CommandException e) {
					listener.badCommand(e);
				}
			});

			if (doCommands()) return;
		} catch (CommandException e) {
			Platform.runLater(() -> listener.badCommand(e));
		}

		while(true) {
			try {
				Action a = commandRequests.take();
				a.completeAction(battle);
				Optional<CommandRequest> crq = a.getCommandRequest();

				// handle a command request
				if (crq.isPresent()) {
					// If we're resigning, cancel any incomplete commands first.
					if (crq.get() instanceof ResignCommandRequest) commandQueue.clear(); 
					commandQueue.addAll(crq.get().makeCommand(battle.battleState));
					if (doCommands()) return;
				}

				// handle command completion
				if (a instanceof ActionComplete) {
					Command cmd = commandQueue.peek();
					if (cmd == null)
						throw new CommandException("No command to complete");

					try {
						((ActionComplete) a).completeCommand((InstantEffectCommand) cmd);
						if (doCommands()) return;
					} catch (ClassCastException e) {
						Platform.runLater(() -> listener.badCommand(
							new CommandException("Expected instant effect command", e)));
					}

				}

				// handle command cancellation
				if (a instanceof ActionCancel) {
					Command cmd = commandQueue.peek();
					if (cmd != null) {
						if (!cmd.canCancel())
							throw new CommandException("Cannot cancel command");
						commandQueue.clear();
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
			final Command cmd = commandQueue.peek();

			if (cmd instanceof InstantEffectCommand) {
				final InstantEffectCommand i = (InstantEffectCommand) cmd;
				if (!i.isCompletedOrRequestCompletion()) {
					Platform.runLater(() -> listener.completeEffect(i.getEffect(), i.canCancel()));
					return false;
				}
			}

			commandQueue.poll();

			// A single command could get expanded into multiple commands if we
			// trigger any traps or zones
			final List<ExecutedCommand> allCmds = cmd.doCmdComputingTriggers(battle);

			// mark the last command in the queue
			if (commandQueue.isEmpty()) {
				final ExecutedCommand last;
				if (allCmds.isEmpty()) {
					// cmd does nothing, so we can get away with this.
					last = new ExecutedCommand(cmd, new ArrayList<>()); 
				} else {
					last = allCmds.remove(allCmds.size() - 1);
				}
				allCmds.add(last.markLastInSequence());
			}

			for (ExecutedCommand ec : allCmds) {
				Platform.runLater(() -> listener.command(ec));

				// don't send the command to the network until it's been completed
				// locally.  This allows the commands to update themselves when we have
				// instant effects that mess with the game state.  Also important for
				// dealing with triggers.
				if (network != null) network.sendCommand(ec.cmd);
			}

			if (battle.battleState.getBattleOutcome(thisPlayer).isPresent()) {
				return true;
			}

			if (cmd instanceof EndTurnCommand) return true;
		}

		return false;
	}

	private void otherTurn() {
		List<Zone> zones = battle.doTurnStart(thisPlayer.otherPlayer());
		battle.getTurnStart(thisPlayer.otherPlayer()); // Do this for the side-effects only
		Platform.runLater(() -> {
			List<Targetable> affected = new ArrayList<>();
			affected.addAll(battle.battleState.cloneCharacters());
			affected.addAll(zones);
			try {
				listener.command((new StartTurnCommand(thisPlayer.otherPlayer(), affected, false))
					.doCmdComputingTriggers(battle).get(0));
			} catch (CommandException e) {
				listener.badCommand(e);
			}
		});

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

	/**
	 * Send a command request.
	 * */
	public synchronized void requestCommand(CommandRequest cmd) {
		if (!accepting) return;
		queueActionWithRetry(new Action(Optional.of(cmd)));
	}

	/**
	 * Send a request for information, getting the result as a future.
	 * */
	public synchronized <T> Future<T> requestInfo(InfoRequest<T> r) {
		if (!accepting) r.cancel(); else {
			queueActionWithRetry(r);
		}
		return r.complete;
	}

	/**
	 * Complete a command (e.g. a teleport command that requires extra targeting
	 * information)
	 * */
	public synchronized void completeEffect(List<MapPoint> completion) {
		if (accepting) {
			queueActionWithRetry(new ActionComplete(battle.battleState, completion));
		}
	}

	/**
	 * Cancel any incomplete commands.
	 * */
	public synchronized void cancel() {
		if (accepting) queueActionWithRetry(new ActionCancel());
	}
}

