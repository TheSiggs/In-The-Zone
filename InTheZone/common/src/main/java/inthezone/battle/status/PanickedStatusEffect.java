package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.MoveCommand;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.PathFinderNode;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import nz.dcoder.ai.astar.Node;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

public class PanickedStatusEffect extends StatusEffect {
	@JSON
	public PanickedStatusEffect(
		@Field("info") final StatusEffectInfo info,
		@Field("startTurn") final int startTurn
	) {
		super(info, startTurn);
	}

	public List<Command> doBeforeTurn(final Battle battle, final Character c) {
		if (c.isDead()) return new LinkedList<>();

		final Set<MapPoint> obstacles = battle.battleState.spaceObstacles();

		Node<MapPoint> p = new PathFinderNode(
			null, battle.battleState.terrain.terrain, obstacles,
			c.getPos(), c.getPos());

		Object[] ns;
		for (int i = 0; i < c.getMP(); i++) {
			ns = p.getAdjacentNodes().toArray();
			p = randomNode(ns);
		}

		final List<MapPoint> r = new LinkedList<>();
		while (p != null) {
			r.add(0, p.getPosition());
			p = p.getParent();
		}

		final List<Command> cmds = new LinkedList<>();
		try {
			if (r.size() >= 2) cmds.add(new MoveCommand(r, true));
		} catch (final ProtocolException e) {
			throw new RuntimeException("Panicked status generated an invalid move path");
		}

		return cmds;
	}

	@SuppressWarnings("unchecked")
	private static final Node<MapPoint> randomNode(Object[] ns) {
		return (Node<MapPoint>) ns[(int) (Math.random() * ((double) ns.length))];
	}


	@Override public boolean isBeforeTurnExhaustive() {return true;}
}

