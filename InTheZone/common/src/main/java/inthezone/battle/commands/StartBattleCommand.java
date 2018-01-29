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
import isogame.engine.MapPoint;
import isogame.engine.Sprite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;
import ssjsjs.JSONable;

/**
 * Contains all the data needed to start a new battle.  When executed, this
 * command initializes a new battle.
 * */
public class StartBattleCommand implements JSONable {
	private final String kind = "Start";

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

	@JSON
	private StartBattleCommand(
		@Field("kind") final String kind,
		@Field("stage") final String stage,
		@Field("p1GoesFirst") final boolean p1GoesFirst,
		@Field("p1") final Loadout p1,
		@Field("p2") final Loadout p2,
		@Field("p1start") final List<MapPoint> p1start,
		@Field("p2start") final List<MapPoint> p2start,
		@Field("p1Name") final String p1Name,
		@Field("p2Name") final String p2Name
	) throws ProtocolException {
		this(stage, p1GoesFirst, p1, p2, p1start, p2start, p1Name, p2Name);

		if (!kind.equals("Start"))
			throw new ProtocolException("Expected start command");
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

	public Battle doCmd(final GameDataFactory gameData)
		throws CorruptDataException
	{
		return new Battle(
			new BattleState(gameData.getStage(stage), characters),
			gameData.getStandardSprites());
	}
}

