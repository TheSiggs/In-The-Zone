package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.battle.data.Player;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.FacingDirection;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import isogame.engine.Sprite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

	private final Collection<Character> characters;

	public StartBattleCommand(
		String stage, boolean p1GoesFirst, Loadout p1, Loadout p2,
		List<MapPoint> p1start, List<MapPoint> p2start
	) {
		this.stage = stage;
		this.p1GoesFirst = p1GoesFirst;
		this.p1 = p1;
		this.p2 = p2;
		this.p1start = p1start;
		this.p2start = p2start;


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
		Collection<Sprite> sprites = new ArrayList<>();
		for (Character c : characters) {
			Sprite s = new Sprite(c.sprite);
			s.userData = c.id;
			s.pos = c.getPos();
			s.direction = FacingDirection.DOWN;
			sprites.add(s);
		}

		return sprites;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		JSONArray a1 = new JSONArray();
		JSONArray a2 = new JSONArray();

		for (MapPoint p : p1start) a1.add(p.getJSON());
		for (MapPoint p : p2start) a2.add(p.getJSON());

		r.put("kind", "Start");
		r.put("stage", stage);
		r.put("p1First", p1GoesFirst);
		r.put("p1", p1.getJSON());
		r.put("p2", p2.getJSON());
		r.put("p1Start", a1);
		r.put("p2Start", a2);
		return r;
	}

	public static StartBattleCommand fromJSON(
		JSONObject json, GameDataFactory gameData
	) throws ProtocolException
	{
		Object okind = json.get("kind");
		Object ostage = json.get("stage");
		Object op1First = json.get("p1First");
		Object op1 = json.get("p1");
		Object op2 = json.get("p2");
		Object op1Start = json.get("p1Start");
		Object op2Start = json.get("p2Start");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (ostage == null) throw new ProtocolException("Missing stage");
		if (op1First == null) throw new ProtocolException("Missing p1First");
		if (op1 == null) throw new ProtocolException("Missing p1 loadout");
		if (op2 == null) throw new ProtocolException("Missing p2 loadout");
		if (op1Start == null) throw new ProtocolException("Missing p1 start positions");
		if (op2Start == null) throw new ProtocolException("Missing p2 start positions");

		if (!((String) okind).equals("Start"))
			throw new ProtocolException("Expected start command");

		try {
			String stage = (String) ostage;
			boolean p1First = (Boolean) op1First;
			Loadout p1 = Loadout.fromJSON((JSONObject) op1, gameData);
			Loadout p2 = Loadout.fromJSON((JSONObject) op2, gameData);
			JSONArray rawp1Start = (JSONArray) op1Start;
			JSONArray rawp2Start = (JSONArray) op2Start;
			List<MapPoint> p1Start = new ArrayList<>();
			List<MapPoint> p2Start = new ArrayList<>();

			for (int i = 0; i < rawp1Start.size(); i++) {
				p1Start.add(MapPoint.fromJSON((JSONObject) rawp1Start.get(i)));
			}
			for (int i = 0; i < rawp2Start.size(); i++) {
				p2Start.add(MapPoint.fromJSON((JSONObject) rawp2Start.get(i)));
			}

			return new StartBattleCommand(stage, p1First, p1, p2, p1Start, p2Start);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing start command", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing start command", e);
		}
	}

	public Battle doCmd(GameDataFactory gameData) {
		return new Battle(new BattleState(gameData.getStage(stage), characters));
	}
}

