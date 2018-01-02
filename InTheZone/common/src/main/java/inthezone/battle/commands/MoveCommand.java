package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSONConstructor;

/**
 * A character moves.
 * */
public class MoveCommand extends Command {
	private CommandKind kind = CommandKind.MOVE;

	private final boolean isPanic;
	public final List<MapPoint> path = new ArrayList<>();

	/**
	 * @param isPanic Set to true if this move command was created by the panic
	 * status effect, and the traps and zones have not been triggered yet.
	 * */
	public MoveCommand(
		final List<MapPoint> path,
		final boolean isPanic
	) throws ProtocolException {
		if (path.size() < 2) throw new ProtocolException("20: Bad path in move command");
		path.addAll(path);
		this.isPanic = isPanic;
	}

	@JSONConstructor
	public MoveCommand(
		@Field("kind") final CommandKind kind,
		@Field("path") final MapPoint[] path
	) throws ProtocolException
	{
		this(Arrays.asList(path), false);

		if (kind != CommandKind.MOVE)
			throw new ProtocolException("Expected move command");
	}

	@Override
	public List<Targetable> doCmd(final Battle battle) throws CommandException {
		final Optional<Character> oc = battle.battleState.getCharacterAt(path.get(0));

		if (isPanic && oc.map(c -> !c.isPanicked()).orElse(true)) {
			// this is an obsolete panic command, remove it.
			return new ArrayList<>();
		}

		if (!battle.battleState.canMove(path))
			throw new CommandException("21: Invalid move command");

		battle.doMove(path, true);

		final List<Targetable> r = new ArrayList<>();
		oc.ifPresent(c -> r.add(c));
		return r;
	}

	@Override
	public List<ExecutedCommand> doCmdComputingTriggers(final Battle turn)
		throws CommandException
	{
		final List<ExecutedCommand> r = new ArrayList<>();

		final Character agent = turn.battleState.getCharacterAt(path.get(0))
			.orElseThrow(() ->
				new CommandException("MV1: No character at start of path"));

		if (isPanic && !agent.isPanicked()) {
			// this is an obsolete panic command, remove it.
			return new ArrayList<>();
		}

		final List<MapPoint> path1 =
			turn.battleState.reduceToValidPath(
				turn.battleState.trigger.shrinkPath(agent, path));
		
		try {
			if (path1.size() >= 2) {
				final MoveCommand move1 = new MoveCommand(path1, false);
				final List<Targetable> r1 = move1.doCmd(turn);
				if (!r1.isEmpty()) r.add(new ExecutedCommand(move1, r1));
			}

			final MapPoint loc = path1.isEmpty()?
				path.get(0) : path1.get(path1.size() - 1);
			final List<Command> triggers = turn.battleState.trigger.getAllTriggers(loc);
			for (Command c : triggers) r.addAll(c.doCmdComputingTriggers(turn));

			agent.currentZone = turn.battleState.getZoneAt(loc);

			if (isPanic && agent.getMP() > 0) {
				final Optional<Character> oc = turn.battleState.getCharacterAt(loc);
				if (oc.isPresent()) {
					final List<Command> cont = oc.get().continueTurnReset(turn);
					for (Command c : cont) r.addAll(c.doCmdComputingTriggers(turn));
				}
			}

			return r;
		} catch (final ProtocolException e) {
			throw new CommandException("Error constructing move command", e);
		}
	}
}

