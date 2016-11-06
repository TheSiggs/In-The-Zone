package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.Targetable;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InstantEffect implements HasJSONRepresentation {
	public final MapPoint agent;

	protected InstantEffect(MapPoint agent) {
		this.agent = agent;
	}

	/**
	 * Apply this effect assuming traps and zones have been triggered.
	 * */
	public abstract List<Targetable> apply(Battle battle) throws CommandException;

	/**
	 * Apply this effect, triggering traps and zones.
	 * @param battle The current battle
	 * @param cmd A constructor to make Commands from InstantEffects
	 * @param affected [out] A list that will contain the affected characters
	 * @return The actual commands that were executed, including triggers
	 * */
	public List<Command> applyComputingTriggers(
		Battle battle, Function<InstantEffect, Command> cmd, List<Targetable> affected
	) throws CommandException
	{
		affected.clear();
		List<Command> r = new ArrayList<>();
		affected.addAll(apply(battle));
		r.add(cmd.apply(this));
		return r;
	}

	/**
	 * Update the locations of targets to this effect.
	 * */
	public abstract InstantEffect retarget(
		BattleState battle, Map<MapPoint, MapPoint> retarget);

	/**
	 * Get information pertaining to targets that were moved by this effect.
	 * */
	public Map<MapPoint, MapPoint> getRetargeting() {return new HashMap<>();}

	/**
	 * Determine if this instant effect is ready to run
	 * */
	public boolean isComplete() {return true;}

	/**
	 * Attempt to complete this instant effect.
	 * @return true on success, false on fail.
	 * */
	public boolean complete(List<MapPoint> p) {return true;}
}

