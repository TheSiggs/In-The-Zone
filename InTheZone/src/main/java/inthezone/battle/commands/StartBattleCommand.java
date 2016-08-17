package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.battle.data.Player;
import isogame.engine.MapPoint;
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
	}

	public Battle doCmd(GameDataFactory factory) {
		/*ArrayList<Character> cs = new ArrayList<>();
		cs.add(p1.c1.cloneTo(p1Start1, Player.PLAYER_A));
		cs.add(p1.c2.cloneTo(p1Start2, Player.PLAYER_A));
		cs.add(p1.c3.cloneTo(p1Start3, Player.PLAYER_A));
		cs.add(p1.c4.cloneTo(p1Start4, Player.PLAYER_A));
		cs.add(p2.c1.cloneTo(p2Start1, Player.PLAYER_B));
		cs.add(p2.c2.cloneTo(p2Start2, Player.PLAYER_B));
		cs.add(p2.c3.cloneTo(p2Start3, Player.PLAYER_B));
		cs.add(p2.c4.cloneTo(p2Start4, Player.PLAYER_B));

		return new Battle(new BattleState(factory.getStage(stage), cs));*/
		return null;
	}
}

