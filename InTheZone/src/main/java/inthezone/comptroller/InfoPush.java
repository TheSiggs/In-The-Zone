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
		complete.complete(LineOfSight.getDiamond(subject.getPos(), 1).stream()
			.filter(p -> battle.battleState.getTargetableAt(p).stream()
				.anyMatch(t -> t.isPushable()))
			.collect(Collectors.toList()));
	}
}


