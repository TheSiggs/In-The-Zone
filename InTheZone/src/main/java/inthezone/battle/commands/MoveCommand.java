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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
		if (path.size() < 2) throw new CommandException("Bad path in move command");
		this.path = path;
		this.isPanic = isPanic;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		JSONArray a = new JSONArray();
		r.put("kind", CommandKind.MOVE.toString());
		for (MapPoint p : path) a.add(p.getJSON());
		r.put("path", a);
		return r;
	}

	public static MoveCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object opath = json.get("path");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (opath == null) throw new ProtocolException("Missing move path");

		if (CommandKind.fromString((String) okind) != CommandKind.MOVE)
			throw new ProtocolException("Expected move command");

		try {
			JSONArray rawPath = (JSONArray) opath;
			List<MapPoint> path = new ArrayList<>();
			for (int i = 0; i < rawPath.size(); i++) {
				path.add(MapPoint.fromJSON((JSONObject) rawPath.get(i)));
			}

			// isPanic is always false here, because at this point the triggers have
			// been resolved
			return new MoveCommand(path, false);
		} catch (ClassCastException|CorruptDataException|CommandException e) {
			throw new ProtocolException("Error parsing move command", e);
		}
	}

	@Override
	public List<Targetable> doCmd(Battle battle) throws CommandException {
		if (!battle.battleState.canMove(path)) throw new CommandException("Invalid move command");
		Optional<Character> oc = battle.battleState.getCharacterAt(path.get(0));

		battle.doMove(path);

		List<Targetable> r = new ArrayList<>();
		oc.ifPresent(c -> r.add(c));
		return r;
	}

	@Override
	public List<Command> doCmdComputingTriggers(
		Battle turn, List<Targetable> targeted) throws CommandException
	{
		List<Command> r = new ArrayList<>();

		List<MapPoint> path1 = turn.battleState.trigger.shrinkPath(path);
		
		if (path1.size() >= 2) {
			Command move1 = new MoveCommand(path1, false);

			r.add(move1);
			targeted.addAll(move1.doCmd(turn));
		}

		MapPoint loc = path1.get(path1.size() - 1);
		List<Command> triggers = turn.battleState.trigger.getAllTriggers(loc);
		for (Command c : triggers) {
			r.addAll(c.doCmdComputingTriggers(turn, targeted));
		}

		if (isPanic && !triggers.isEmpty()) {
			turn.battleState.getCharacterAt(loc)
				.ifPresent(c -> r.addAll(c.continueTurnReset(turn)));
		}

		return r;
	}
}

