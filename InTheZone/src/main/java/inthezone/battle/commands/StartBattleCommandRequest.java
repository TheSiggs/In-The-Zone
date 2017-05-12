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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This one works a bit different.  Player 1 generates a
 * StartBattleCommandRequest and sends to it player 2.  Player 2 then generates
 * his StartBattleCommandRequest and uses it to make a StartBattleCommand,
 * which he executes then sends to player 1, who executes it.  Then, the battle
 * begins.
 * */
public class StartBattleCommandRequest implements HasJSONRepresentation {
	public final String stage;
	public final Player player;
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
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		final JSONArray a = new JSONArray();
		startTiles.stream().map(x -> x.getJSON()).forEach(x -> a.put(x));
		r.put("name", "startBattleReq");
		r.put("stage", stage);
		r.put("player", player.toString());
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

		try {
			final String name = json.getString("name");
			final String stage = json.getString("stage");
			final Player player = Player.fromString(json.getString("player"));
			final JSONArray rawStarts = json.getJSONArray("starts");
			final Loadout loadout = Loadout.fromJSON(json.getJSONObject("loadout"), gameData);

			final List<MapPoint> starts = new ArrayList<>();
			for (int i = 0; i < rawStarts.length(); i++) {
				starts.add(MapPoint.fromJSON(rawStarts.getJSONObject(i)));
			}

			if (!name.equals("startBattleReq"))
				throw new ProtocolException("Expected start battle request");

			return new StartBattleCommandRequest(stage, player, loadout, starts);

		} catch (JSONException|CorruptDataException e) {
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
}

