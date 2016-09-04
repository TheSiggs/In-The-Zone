package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.commands.CommandException;
import inthezone.battle.Targetable;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.List;
import java.util.Map;

public interface InstantEffect extends HasJSONRepresentation {
	public List<Targetable> apply(Battle battle) throws CommandException;

	/**
	 * Get information pertaining to targets that were moved by this effect.
	 * */
	public Map<MapPoint, MapPoint> getRetargeting();

	/**
	 * Update the locations of targets to this effect.
	 * */
	public InstantEffect retarget(BattleState battle, Map<MapPoint, MapPoint> retarget);

	/**
	 * Determine if this instant effect is ready to run
	 * */
	public boolean isComplete();

	/**
	 * Attempt to complete this instant effect.
	 * @return true on success, false on fail.
	 * */
	public boolean complete(List<MapPoint> p);
}

