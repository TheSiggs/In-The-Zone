package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
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

	public MoveCommand(List<MapPoint> path) throws CommandException {
		if (path.size() < 2) throw new CommandException("Bad path in move command");
		this.path = path;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		JSONArray a = new JSONArray();
		r.put("kind", CommandKind.MOVE);
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
			return new MoveCommand(path);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing move command", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing move command", e);
		} catch (CommandException e) {
			throw new ProtocolException("Error parsing move command", e);
		}
	}

	@Override
	public List<Character> doCmd(Battle battle) throws CommandException {
		if (!battle.battleState.canMove(path)) throw new CommandException("Invalid move command");
		Optional<Character> oc = battle.battleState.getCharacterAt(path.get(0));

		battle.doMove(path);

		List<Character> r = new ArrayList<>();
		oc.ifPresent(c -> r.add(c.clone()));
		return r;
	}
}

