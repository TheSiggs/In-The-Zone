package inthezone.comptroller;

import isogame.engine.MapPoint;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import inthezone.battle.Battle;
import inthezone.battle.CharacterFrozen;

/**
 * Get the path the character will follow to a specific point.
 * */
public class InfoPath extends InfoRequest<List<List<MapPoint>>> {
	private final CharacterFrozen subject;
	private final MapPoint target;
	private final int range;

	public InfoPath(final CharacterFrozen subject, final MapPoint target) {
		this.subject = subject;
		this.target = target;
		this.range = subject.getMP();
	}

	public InfoPath(
		final CharacterFrozen subject, final MapPoint target, final int range
	) {
		this.subject = subject;
		this.target = target;
		this.range = range;
	}

	@Override public void completeAction(final Battle battle) {
		if (subject.isImprisoned()) {
			complete.complete(new LinkedList<>());
		} else {
			complete.complete(battle.battleState.findAllValidPaths(
				subject.getPos(), target, subject.getPlayer(), range));
		}
	}
}

