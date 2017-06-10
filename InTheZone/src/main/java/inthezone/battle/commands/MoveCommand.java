package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MoveCommand extends Command {
	public final List<MapPoint> path;

	private final boolean isPanic;

	/**
	 * @param isPanic Set to true if this move command was created by the panic
	 * status effect, and the traps and zones have not been triggered yet.
	 * */
	public MoveCommand(List<MapPoint> path, boolean isPanic)
		throws CommandException
	{
		if (path.size() < 2) throw new CommandException("20: Bad path in move command");
		this.path = path;
		this.isPanic = isPanic;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		final JSONArray a = new JSONArray();
		r.put("kind", CommandKind.MOVE.toString());
		for (MapPoint p : path) a.put(p.getJSON());
		r.put("path", a);
		return r;
	}

	public static MoveCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		try {
			final CommandKind kind = CommandKind.fromString(json.getString("kind"));
			final JSONArray rawPath = json.getJSONArray("path");

			if (kind != CommandKind.MOVE)
				throw new ProtocolException("Expected move command");

			final List<MapPoint> path = new ArrayList<>();
			for (int i = 0; i < rawPath.length(); i++) {
				path.add(MapPoint.fromJSON(rawPath.getJSONObject(i)));
			}

			// isPanic is always false here, because at this point the triggers have
			// been resolved
			return new MoveCommand(path, false);

		} catch (CorruptDataException|CommandException|JSONException e) {
			throw new ProtocolException("Error parsing move command", e);
		}
	}

	@Override
	public List<Targetable> doCmd(Battle battle) throws CommandException {
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
	public List<ExecutedCommand> doCmdComputingTriggers(Battle turn)
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
		
		if (path1.size() >= 2) {
			final Command move1 = new MoveCommand(path1, false);
			r.add(new ExecutedCommand(move1, move1.doCmd(turn)));
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
	}
}

