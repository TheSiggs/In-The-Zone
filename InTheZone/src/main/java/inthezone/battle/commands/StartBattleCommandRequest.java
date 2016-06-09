package inthezone.battle.commands;

import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.concurrent.ThreadLocalRandom;
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

	private static final MapPoint[] mapPointArray = new MapPoint[0];

	public StartBattleCommandRequest(String stage, Loadout me) {
		this.stage = stage;
		this.me = me;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("name", "startBattleReq");
		r.put("stage", stage);
		r.put("loadout", me.getJSON());
		return r;
	}

	public static StartBattleCommandRequest fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object oname = json.get("name");
		Object ostage = json.get("stage");
		Object oloadout = json.get("loadout");
		if (oname == null) throw new ProtocolException("Missing name in start battle request");
		if (ostage == null) throw new ProtocolException("Missing stage in start battle request");
		if (oloadout == null) throw new ProtocolException("Missing loadout in start battle request");

		try {
			String name = (String) oname;
			String stage = (String) ostage;
			Loadout loadout = Loadout.fromJSON((JSONObject) oloadout);
			if (!name.equals("startBattleReq"))
				throw new ProtocolException("Expected start battle request");
			return new StartBattleCommandRequest(stage, loadout);

		} catch (ClassCastException e) {
			throw new ProtocolException("Type error in start battle request");
		} catch (CorruptDataException e) {
			throw new ProtocolException("Parse error in start battle request", e);
		}
	}

	public StartBattleCommand makeCommand(
		StartBattleCommandRequest p1, GameDataFactory factory
	) {
		Stage si = factory.getStage(stage);
		MapPoint[] p1ps = si.terrain.getPlayerStartTiles().toArray(mapPointArray);
		MapPoint[] p2ps = si.terrain.getAIStartTiles().toArray(mapPointArray);
		shuffleN(p1ps, 4);
		shuffleN(p2ps, 4);

		return new StartBattleCommand(
			stage, Math.random() < 0.5, p1.me, me,
			p1ps[0], p1ps[1], p1ps[2], p1ps[3],
			p2ps[0], p2ps[1], p2ps[2], p2ps[3]);
	}

	private <T> void shuffleN(T[] data, int n) {
		int l = 0;
		T t;
		while (l < n) {
			int s = ThreadLocalRandom.current().nextInt(l, n);
			t = data[l];
			data[l] = data[s];
			data[s] = t;
			l += 1;
		}
	}
}

