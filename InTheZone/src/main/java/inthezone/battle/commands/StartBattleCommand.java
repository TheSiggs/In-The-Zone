package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.battle.data.Player;
import isogame.engine.FacingDirection;
import isogame.engine.MapPoint;
import isogame.engine.Sprite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains all the data needed to start a new battle.  When executed, this
 * command initializes a new battle.
 * */
public class StartBattleCommand {
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
				Player.PLAYER_A, p1start.get(i), id++));
		}
		for (int i = 0; i < p2start.size(); i++) {
			characters.add(new Character(p2.characters.get(i),
				Player.PLAYER_B, p2start.get(i), id++));
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

	public Battle doCmd(GameDataFactory gameData) {
		return new Battle(new BattleState(gameData.getStage(stage), characters));
	}
}

