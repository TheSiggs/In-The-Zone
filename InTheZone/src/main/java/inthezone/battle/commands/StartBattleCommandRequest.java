package inthezone.battle.commands;

import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.battle.data.Player;
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
	public final String stage;
	private final Player player;
	private final Loadout me;
	private final List<MapPoint> startTiles;

	/**
	 * @param stage The stage to battle on
	 * @param me My loadout
	 * @param startTiles The start tiles for my characters.  In the same order as
	 * listed in the loadout.
	 * */
	public StartBattleCommandRequest(
		String stage, Player player, Loadout me, List<MapPoint> startTiles
	) {
		this.stage = stage;
		this.player = player;
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
		r.put("player", player);
		r.put("starts", a);
		r.put("loadout", me.getJSON());
		return r;
	}

	public Player getOtherPlayer() {
		return player.otherPlayer();
	}

	public static StartBattleCommandRequest fromJSON(
		JSONObject json, GameDataFactory gameData
	) throws ProtocolException {

		Object oname = json.get("name");
		Object ostage = json.get("stage");
		Object oplayer = json.get("player");
		Object ostarts = json.get("starts");
		Object oloadout = json.get("loadout");
		if (oname == null) throw new ProtocolException("Missing name in start battle request");
		if (ostage == null) throw new ProtocolException("Missing stage in start battle request");
		if (oplayer == null) throw new ProtocolException("Missing player in start battle request");
		if (ostarts == null) throw new ProtocolException("Missing start positions in battle request");
		if (oloadout == null) throw new ProtocolException("Missing loadout in start battle request");

		try {
			String name = (String) oname;
			String stage = (String) ostage;
			Player player = Player.fromString((String) oplayer);
			List<MapPoint> starts = jsonArrayToList((JSONArray) ostarts, MapPoint.class);
			Loadout loadout = Loadout.fromJSON((JSONObject) oloadout, gameData);
			if (!name.equals("startBattleReq"))
				throw new ProtocolException("Expected start battle request");
			return new StartBattleCommandRequest(stage, player, loadout, starts);

		} catch (ClassCastException e) {
			throw new ProtocolException("Type error in start battle request");
		} catch (CorruptDataException e) {
			throw new ProtocolException("Parse error in start battle request", e);
		}
	}

	/**
	 * Only called by the challenged player.  The resulting command will be sent
	 * back to the challenger.
	 * */
	public StartBattleCommand makeCommand(
		StartBattleCommandRequest op, GameDataFactory factory
	) throws CorruptDataException {
		if (op.player == this.player)
			throw new CorruptDataException("Both parties tried to play the same side");

		Stage si = factory.getStage(stage);
		Collection<MapPoint> myps = getStartTiles(si, player);
		Collection<MapPoint> opps = getStartTiles(si, op.player);

		if (
			!startTiles.stream().allMatch(t -> myps.contains(t)) ||
			!op.startTiles.stream().allMatch(t -> opps.contains(t))
		) {
			throw new CorruptDataException(
				"Invalid start positions in start battle request");
		}

		if (
			startTiles.size() != me.characters.size() ||
			op.startTiles.size() != op.me.characters.size()
		) {
			throw new CorruptDataException(
				"Wrong number of start positions inn start battle request");
		}

		if (player == Player.PLAYER_A) {
			return new StartBattleCommand(
				stage, Math.random() < 0.5, me, op.me, startTiles, op.startTiles);
		} else {
			return new StartBattleCommand(
				stage, Math.random() < 0.5, op.me, me, op.startTiles, startTiles);
		}
	}

	private static Collection<MapPoint> getStartTiles(Stage s, Player p) {
		return p == Player.PLAYER_A ?
			s.terrain.getPlayerStartTiles() :
			s.terrain.getAIStartTiles();
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

