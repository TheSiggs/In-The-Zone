package inthezone.battle.commands;

import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This one works a bit different.  Player 1 generates a
 * StartBattleCommandRequest and sends to it player 2.  Player 2 then generates
 * his StartBattleCommandRequest and uses it to make a StartBattleCommand,
 * which he executes then sends to player 1, who executes it.  Then, the battle
 * begins.
 * */
public class StartBattleCommandRequest implements HasJSONRepresentation {
	private final String stage;
	private final Loadout me;
	private final List<MapPoint> startTiles;

	/**
	 * @param stage The stage to battle on
	 * @param me My loadout
	 * @param startTiles The start tiles for my characters.  In the same order as
	 * listed in the loadout.
	 * */
	public StartBattleCommandRequest(
		String stage, Loadout me, List<MapPoint> startTiles
	) {
		this.stage = stage;
		this.me = me;
		this.startTiles = startTiles;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		JSONArray a = new JSONArray();
		startTiles.stream().map(x -> x.getJSON()).forEach(x -> a.add(x));
		r.put("name", "startBattleReq");
		r.put("stage", stage);
		r.put("starts", a);
		r.put("loadout", me.getJSON());
		return r;
	}

	public static StartBattleCommandRequest fromJSON(
		JSONObject json, GameDataFactory gameData
	) throws ProtocolException {

		Object oname = json.get("name");
		Object ostage = json.get("stage");
		Object ostarts = json.get("starts");
		Object oloadout = json.get("loadout");
		if (oname == null) throw new ProtocolException("Missing name in start battle request");
		if (ostage == null) throw new ProtocolException("Missing stage in start battle request");
		if (ostarts == null) throw new ProtocolException("Missing start positions in battle request");
		if (oloadout == null) throw new ProtocolException("Missing loadout in start battle request");

		try {
			String name = (String) oname;
			String stage = (String) ostage;
			List<MapPoint> starts = jsonArrayToList((JSONArray) ostarts, MapPoint.class);
			Loadout loadout = Loadout.fromJSON((JSONObject) oloadout, gameData);
			if (!name.equals("startBattleReq"))
				throw new ProtocolException("Expected start battle request");
			return new StartBattleCommandRequest(stage, loadout, starts);

		} catch (ClassCastException e) {
			throw new ProtocolException("Type error in start battle request");
		} catch (CorruptDataException e) {
			throw new ProtocolException("Parse error in start battle request", e);
		}
	}

	/**
	 * Only called by the challenged player.
	 * */
	public StartBattleCommand makeCommand(
		StartBattleCommandRequest p1, GameDataFactory factory
	) throws CorruptDataException {
		Stage si = factory.getStage(stage);
		Collection<MapPoint> p1ps = si.terrain.getPlayerStartTiles();
		Collection<MapPoint> p2ps = si.terrain.getAIStartTiles();

		if (
			!startTiles.stream().allMatch(t -> p1ps.contains(t)) ||
			!p1.startTiles.stream().allMatch(t -> p2ps.contains(t))
		) {
			throw new CorruptDataException(
				"Invalid start positions in start battle request");
		}

		if (
			startTiles.size() != me.characters.size() ||
			p1.startTiles.size() != p1.me.characters.size()
		) {
			throw new CorruptDataException(
				"Wrong number of start positions inn start battle request");
		}

		return new StartBattleCommand(
			stage, Math.random() < 0.5, p1.me, me,
			startTiles, p1.startTiles);
	}

	private static <T> List<T> jsonArrayToList(JSONArray a, Class<T> clazz)
		throws ClassCastException
	{
		List<T> r = new ArrayList<>();
		int limit = a.size();
		for (int i = 0; i < limit; i++) {
			r.add(clazz.cast(a.get(i)));
		}
		return r;
	}
}

