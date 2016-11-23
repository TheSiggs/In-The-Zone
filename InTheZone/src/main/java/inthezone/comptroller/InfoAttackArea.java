package inthezone.comptroller;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import isogame.engine.MapPoint;
import java.util.Collection;

public class InfoAttackArea extends InfoRequest<Collection<MapPoint>> {
	private final Character subject;
	private final MapPoint castFrom;
	private final Ability ability;
	private final MapPoint target;

	public InfoAttackArea(
		Character subject, Ability ability, MapPoint castFrom, MapPoint target
	) {
		this.subject = subject;
		this.ability = ability;
		this.castFrom = castFrom;
		this.target = target;
	}

	@Override public void completeAction(Battle battle) {
		complete.complete(battle.battleState.getAffectedArea(
			subject.getPos(), AbilityAgentType.CHARACTER,
			castFrom, ability, target));
	}
}

