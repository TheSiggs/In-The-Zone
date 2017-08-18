package inthezone.comptroller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import isogame.engine.MapPoint;

import inthezone.battle.Battle;
import inthezone.battle.CharacterFrozen;
import inthezone.battle.LineOfSight;

/**
 * Get the locations the character can move to.
 * */
public class InfoMoveRange extends InfoRequest<Collection<MapPoint>> {
	private final CharacterFrozen subject;
	private final int range;

	public InfoMoveRange(CharacterFrozen subject) {
		this.subject = subject;
		this.range = subject.getMP();
	}

	public InfoMoveRange(CharacterFrozen subject, int range) {
		this.subject = subject;
		this.range = range;
	}

	@Override public void completeAction(Battle battle) {
		if (subject.isImprisoned()) {
			complete.complete(new ArrayList<>());
		} else {
			complete.complete(LineOfSight.getDiamond(subject.getPos(), range).stream()
				.filter(p -> battle.battleState.canMoveRange(range,
					battle.battleState.findPath(subject.getPos(), p, subject.getPlayer())))
				.collect(Collectors.toList()));
		}
	}
}

