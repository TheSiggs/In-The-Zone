package inthezone.battle.commands;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import isogame.engine.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.battle.data.Player;
import inthezone.protocol.ProtocolException;

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
	public final String playerName;
	private final Optional<Loadout> me;
	private final Optional<List<MapPoint>> startTiles;

	/**
	 * @param stage The stage to battle on
	 * @param me My loadout
	 * @param startTiles The start tiles for my characters.  In the same order as
	 * listed in the loadout.
	 * */
	public StartBattleCommandRequest(
		final String stage,
		final Player player,
		final String playerName,
		final Optional<Loadout> me,
		final Optional<List<MapPoint>> startTiles
	) {
		this.stage = stage;
		this.player = player;
		this.playerName = playerName;
		this.me = me;
		this.startTiles = startTiles;
	}

	@Override
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		final JSONArray a = new JSONArray();

		startTiles.ifPresent(s -> {
			s.stream().map(x -> x.getJSON()).forEach(x -> a.put(x));
			r.put("starts", a);
		});

		r.put("name", "startBattleReq");
		r.put("stage", stage);
		r.put("player", player.toString());
		r.put("playerName", playerName);
		me.ifPresent(d -> r.put("loadout", d.getJSON()));
		return r;
	}

	public Player getOtherPlayer() {
		return player.otherPlayer();
	}

	public static StartBattleCommandRequest fromJSON(
		final JSONObject json, final GameDataFactory gameData
	) throws ProtocolException {

		try {
			final String name = json.getString("name");
			final String playerName = json.getString("playerName");
			final String stage = json.getString("stage");
			final Player player = Player.fromString(json.getString("player"));
			final JSONArray rawStarts = json.optJSONArray("starts");
			final JSONObject mloadout = json.optJSONObject("loadout");

			final Optional<Loadout> loadout;
			if (mloadout != null) {
				loadout = Optional.of(Loadout.fromJSON(mloadout, gameData));
			} else {
				loadout = Optional.empty();
			}

			final Optional<List<MapPoint>> starts;
			if (rawStarts == null) {
				starts = Optional.empty();
			} else {
				final List<MapPoint> ss = new ArrayList<>();
				for (int i = 0; i < rawStarts.length(); i++) {
					ss.add(MapPoint.fromJSON(rawStarts.getJSONObject(i)));
				}
				starts = Optional.of(ss);
			}

			if (!name.equals("startBattleReq"))
				throw new ProtocolException("Expected start battle request");

			return new StartBattleCommandRequest(
				stage, player, playerName, loadout, starts);

		} catch (final JSONException|CorruptDataException e) {
			throw new ProtocolException("Parse error in start battle request", e);
		}
	}

	/**
	 * Only called by the challenged player.  The resulting command will be sent
	 * back to the challenger.
	 * */
	public StartBattleCommand makeCommand(
		final StartBattleCommandRequest op, final GameDataFactory factory
	) throws CorruptDataException {
		if (op.player == this.player)
			throw new CorruptDataException("Both parties tried to play the same side");

		if (
			!op.me.isPresent() || !op.startTiles.isPresent() ||
			!me.isPresent() || !startTiles.isPresent()
		) throw new CorruptDataException(
			"Attempted to start a battle with incomplete information");

		final Stage si = factory.getStage(stage);
		final Collection<MapPoint> myps = getStartTiles(si, player);
		final Collection<MapPoint> opps = getStartTiles(si, op.player);

		if (
			!startTiles.get().stream().allMatch(t -> myps.contains(t)) ||
			!op.startTiles.get().stream().allMatch(t -> opps.contains(t))
		) {
			throw new CorruptDataException(
				"Invalid start positions in start battle request");
		}

		if (
			startTiles.get().size() != me.get().characters.size() ||
			op.startTiles.get().size() != op.me.get().characters.size()
		) {
			throw new CorruptDataException(
				"Wrong number of start positions in start battle request");
		}

		if (player == Player.PLAYER_A) {
			return new StartBattleCommand(
				stage, Math.random() < 0.5, me.get(), op.me.get(),
				startTiles.get(), op.startTiles.get(), playerName, op.playerName);
		} else {
			return new StartBattleCommand(
				stage, Math.random() < 0.5, op.me.get(), me.get(),
				op.startTiles.get(), startTiles.get(), op.playerName, playerName);
		}
	}

	private static Collection<MapPoint> getStartTiles(
		final Stage s, final Player p
	) {
		return p == Player.PLAYER_A ?
			s.terrain.getPlayerStartTiles() :
			s.terrain.getAIStartTiles();
	}
}

