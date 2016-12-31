package inthezone.comptroller;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.LineOfSight;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Get the locations the character can move to.
 * */
public class InfoMoveRange extends InfoRequest<Collection<MapPoint>> {
	private final Character subject;
	private final int range;

	public InfoMoveRange(Character subject) {
		this.subject = subject;
		this.range = subject.getMP();
	}

	public InfoMoveRange(Character subject, int range) {
		this.subject = subject;
		this.range = range;
	}

	@Override public void completeAction(Battle battle) {
		complete.complete(LineOfSight.getDiamond(subject.getPos(), range).stream()
			.filter(p -> battle.battleState.canMoveRange(range,
				battle.battleState.findPath(subject.getPos(), p, subject.player)))
			.collect(Collectors.toList()));
	}
}

