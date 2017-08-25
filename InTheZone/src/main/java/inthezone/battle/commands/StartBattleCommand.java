package inthezone.battle.commands;

import isogame.engine.CorruptDataException;
import isogame.engine.FacingDirection;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import isogame.engine.Sprite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.battle.data.Player;
import inthezone.protocol.ProtocolException;

/**
 * Contains all the data needed to start a new battle.  When executed, this
 * command initializes a new battle.
 * */
public class StartBattleCommand implements HasJSONRepresentation {
	public final String stage;
	public final boolean p1GoesFirst;
	private final Loadout p1;
	private final Loadout p2;
	private final List<MapPoint> p1start;
	private final List<MapPoint> p2start;

	public final String p1Name;
	public final String p2Name;

	private final Collection<Character> characters;

	public List<MapPoint> getStartTiles(final Player player) {
		if (player == Player.PLAYER_A) {
			return new ArrayList<>(p1start);
		} else {
			return new ArrayList<>(p2start);
		}
	}

	public StartBattleCommand(
		final String stage,
		final boolean p1GoesFirst,
		final Loadout p1,
		final Loadout p2,
		final List<MapPoint> p1start,
		final List<MapPoint> p2start,
		final String p1Name,
		final String p2Name
	) {
		this.stage = stage;
		this.p1GoesFirst = p1GoesFirst;
		this.p1 = p1;
		this.p2 = p2;
		this.p1start = p1start;
		this.p2start = p2start;

		this.p1Name = p1Name;
		this.p2Name = p2Name;

		characters = new ArrayList<>();
		int id = 0;
		for (int i = 0; i < p1start.size(); i++) {
			characters.add(new Character(p1.characters.get(i),
				Player.PLAYER_A, false, p1start.get(i), id++));
		}
		for (int i = 0; i < p2start.size(); i++) {
			characters.add(new Character(p2.characters.get(i),
				Player.PLAYER_B, false, p2start.get(i), id++));
		}
	}

	public Collection<Sprite> makeSprites() {
		final Collection<Sprite> sprites = new ArrayList<>();
		for (final Character c : characters) {
			Sprite s = new Sprite(c.sprite);
			s.userData = c.id;
			s.setPos(c.getPos());
			s.setDirection(FacingDirection.DOWN);
			sprites.add(s);
		}

		return sprites;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		final JSONArray a1 = new JSONArray();
		final JSONArray a2 = new JSONArray();

		for (final MapPoint p : p1start) a1.put(p.getJSON());
		for (final MapPoint p : p2start) a2.put(p.getJSON());

		r.put("kind", "Start");
		r.put("stage", stage);
		r.put("p1First", p1GoesFirst);
		r.put("p1", p1.getJSON());
		r.put("p2", p2.getJSON());
		r.put("p1Start", a1);
		r.put("p2Start", a2);
		r.put("p1Name", p1Name);
		r.put("p2Name", p2Name);
		return r;
	}

	public static StartBattleCommand fromJSON(
		final JSONObject json, final GameDataFactory gameData
	) throws ProtocolException
	{
		try {
			final String kind = json.getString("kind");
			final String stage = json.getString("stage");
			final boolean p1First = json.getBoolean("p1First");
			final Loadout p1 = Loadout.fromJSON(json.getJSONObject("p1"), gameData);
			final Loadout p2 = Loadout.fromJSON(json.getJSONObject("p2"), gameData);
			final JSONArray rawp1Start = json.getJSONArray("p1Start");
			final JSONArray rawp2Start = json.getJSONArray("p2Start");
			final String p1Name = json.optString("p1Name", "Player A");
			final String p2Name = json.optString("p2Name", "Player B");

			if (!kind.equals("Start"))
				throw new ProtocolException("Expected start command");

			final List<MapPoint> p1Start = new ArrayList<>();
			final List<MapPoint> p2Start = new ArrayList<>();

			for (int i = 0; i < rawp1Start.length(); i++) {
				p1Start.add(MapPoint.fromJSON(rawp1Start.getJSONObject(i)));
			}
			for (int i = 0; i < rawp2Start.length(); i++) {
				p2Start.add(MapPoint.fromJSON(rawp2Start.getJSONObject(i)));
			}

			return new StartBattleCommand(
				stage, p1First, p1, p2, p1Start, p2Start, p1Name, p2Name);

		} catch (final JSONException|CorruptDataException e) {
			throw new ProtocolException("Error parsing start command", e);
		}
	}

	public Battle doCmd(final GameDataFactory gameData)
		throws CorruptDataException
	{
		return new Battle(
			new BattleState(gameData.getStage(stage), characters),
			gameData.getStandardSprites());
	}
}

