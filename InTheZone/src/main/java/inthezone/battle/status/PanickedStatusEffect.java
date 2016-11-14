package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.MoveCommand;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.PathFinderNode;
import isogame.engine.MapPoint;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import nz.dcoder.ai.astar.Node;

public class PanickedStatusEffect extends StatusEffect {
	public PanickedStatusEffect(StatusEffectInfo info) {
		super(info);
	}

	public List<Command> doBeforeTurn(Battle battle, Character c) {
		final Set<MapPoint> obstacles = battle.battleState.spaceObstacles(c.player);

		Node<MapPoint> p = new PathFinderNode(
			null, battle.battleState.terrain.terrain, obstacles,
			c.getPos(), c.getPos());

		@SuppressWarnings("unchecked")
		Node<MapPoint>[] ns = new Node[0];
		for (int i = 0; i < c.getMP(); i++) {
			ns = p.getAdjacentNodes().toArray(ns);
			p = ns[(int) (Math.random() * ((double) ns.length))];
		}

		List<MapPoint> r = new LinkedList<>();
		while (p != null) {
			r.add(0, p.getPosition());
			p = p.getParent();
		}

		List<Command> cmds = new LinkedList<>();
		try {
			if (r.size() >= 2) cmds.add(new MoveCommand(r, true));
		} catch (CommandException e) {
			throw new RuntimeException("Panicked status generated an invalid move path");
		}

		return cmds;
	}

	@Override public boolean isBeforeTurnExhaustive() {return true;}
}

