package inthezone.battle.commands;

import java.util.ArrayList;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.battle.Player;
import isogame.engine.MapPoint;

public class StartBattleCommand {
	private final String stage;
	private final boolean p1GoesFirst;
	private final Loadout p1;
	private final Loadout p2;
	private final MapPoint p1Start1;
	private final MapPoint p1Start2;
	private final MapPoint p1Start3;
	private final MapPoint p1Start4;
	private final MapPoint p2Start1;
	private final MapPoint p2Start2;
	private final MapPoint p2Start3;
	private final MapPoint p2Start4;

	public StartBattleCommand(
		String stage, boolean p1GoesFirst, Loadout p1, Loadout p2,
		MapPoint p1Start1, MapPoint p1Start2, MapPoint p1Start3, MapPoint p1Start4,
		MapPoint p2Start1, MapPoint p2Start2, MapPoint p2Start3, MapPoint p2Start4
	) {
		this.stage = stage;
		this.p1GoesFirst = p1GoesFirst;
		this.p1 = p1;
		this.p2 = p2;
		this.p1Start1 = p1Start1;
		this.p1Start2 = p1Start2;
		this.p1Start3 = p1Start3;
		this.p1Start4 = p1Start4;
		this.p2Start1 = p2Start1;
		this.p2Start2 = p2Start2;
		this.p2Start3 = p2Start3;
		this.p2Start4 = p2Start4;
	}

	public Battle doCmd(GameDataFactory factory) {
		ArrayList<Character> cs = new ArrayList<>();
		cs.add(p1.c1.cloneTo(p1Start1, Player.PLAYER_A));
		cs.add(p1.c2.cloneTo(p1Start2, Player.PLAYER_A));
		cs.add(p1.c3.cloneTo(p1Start3, Player.PLAYER_A));
		cs.add(p1.c4.cloneTo(p1Start4, Player.PLAYER_A));
		cs.add(p2.c1.cloneTo(p2Start1, Player.PLAYER_B));
		cs.add(p2.c2.cloneTo(p2Start2, Player.PLAYER_B));
		cs.add(p2.c3.cloneTo(p2Start3, Player.PLAYER_B));
		cs.add(p2.c4.cloneTo(p2Start4, Player.PLAYER_B));

		return new Battle(new BattleState(factory.getStage(stage), cs));
	}
}

