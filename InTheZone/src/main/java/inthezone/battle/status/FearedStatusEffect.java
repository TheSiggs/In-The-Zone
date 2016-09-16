package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.InstantEffectCommand;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.instant.Push;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class FearedStatusEffect extends StatusEffect {
	private final Character agent;
	private final int g;

	public FearedStatusEffect(Character agent, int g) {
		this.g = g;
		this.agent = agent;
	}

	public List<Command> doBeforeTurn(Battle battle, Character c) {
		Collection<MapPoint> targets = new ArrayList<>();
		targets.add(c.getPos());

		Push p = Push.getEffect(
			battle.battleState,
			new InstantEffectInfo(InstantEffectType.PUSH, g),
			agent.getPos(), targets);

		List<Command> r = new ArrayList<>();
		r.add(new InstantEffectCommand(p, Optional.empty(), Optional.empty()));
		return r;
	}
}


