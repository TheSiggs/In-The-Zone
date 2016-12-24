package inthezone.comptroller;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.LineOfSight;
import isogame.engine.MapPoint;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Get the path the character will follow to a specific point.
 * */
public class InfoPush extends InfoRequest<List<MapPoint>> {
	private final Character subject;

	public InfoPush(Character subject) {
		this.subject = subject;
	}

	@Override public void completeAction(Battle battle) {
		final MapPoint agent = subject.getPos();
		complete.complete(LineOfSight.getDiamond(agent, 1).stream()
			.filter(p -> battle.battleState.getTargetableAt(p).stream()
				.anyMatch(t -> t.isPushable() &&
					battle.battleState.isSpaceFree(agent.addScale(p.subtract(agent), 2))))
			.collect(Collectors.toList()));
	}
}

