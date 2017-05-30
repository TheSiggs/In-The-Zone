package inthezone.battle;

import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.UseAbilityCommandRequest;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Contains algorithms to trigger traps and zones
 * */
public class Trigger {
	private final BattleState battle;

	public Trigger(BattleState battle) {
		this.battle = battle;
	}

	/**
	 * Shrink a path to stop at the first trigger point
	 * @param path A non-empty path
	 * @return A path.  The return value may be of length 1, which indicates that
	 * the start point of the original path was a trigger point.  The return
	 * value is never length 0.
	 * */
	public List<MapPoint> shrinkPath(Targetable agent, List<MapPoint> path) {
		return splitPath(agent, path).get(0);
	}

	/**
	 * Split a path into multiple segments, such that each segment is a valid
	 * path, and triggers only occur on the ends of paths.
	 * @param agent the agent walking the path
	 * @param path a non-empty path
	 * @return A list of path segments, each a valid path in its own right.  The
	 * first element in the return list may have length 1, which indicates that
	 * the original path started on a trigger point.  Never returns an empty list.
	 * */
	public List<List<MapPoint>> splitPath(Targetable agent, List<MapPoint> path) {
		final List<List<MapPoint>> r = new ArrayList<>();

		boolean pathAdded = false;

		Optional<Zone> currentZone = null;
		List<MapPoint> currentPath = new ArrayList<>();
		for (MapPoint p : path) {
			currentPath.add(p);
			pathAdded = false;

			Optional<Zone> newZone = battle.getZoneAt(p);
			if (currentZone == null) currentZone = newZone;

			Optional<Trap> t = battle.getTrapAt(p);
			boolean triggerTrap = t.map(tt ->
				tt.ability.canTarget(tt.parent, agent)).orElse(false);
			boolean triggerZone = newZone.isPresent() &&
				!currentZone.equals(newZone) &&
				newZone.map(z ->
					z.ability.canTarget(z.parent, agent)).orElse(false);

			if ((triggerTrap || triggerZone) && battle.isSpaceFree(p)) {
				currentZone = newZone;
				r.add(currentPath);
				pathAdded = true;
				currentPath = new ArrayList<>();
				currentPath.add(p);
			}
		}

		if (!pathAdded) r.add(currentPath);
		return r;
	}

	/**
	 * Get the results of triggering all traps and zones at point p
	 * @param p The location of the trap or zone tor trigger
	 * */
	public List<Command> getAllTriggers(MapPoint p) {
		final List<Command> r = new ArrayList<>();

		battle.getZoneAt(p).ifPresent(zone -> {
			battle.getTargetableAt(p).stream()
				.filter(t -> zone.ability.canTarget(zone.parent, t))
				.forEach(t -> r.addAll(t.triggerZone(battle)));
		});

		battle.getTrapAt(p).ifPresent(trap -> {
			try {
				final List<Casting> targets = new ArrayList<>(); targets.add(new Casting(p, p));

				if (battle.getTargetableAt(p).stream()
					.anyMatch(t -> trap.ability.canTarget(trap.parent, t)))
				{
					r.addAll((new UseAbilityCommandRequest(p, AbilityAgentType.TRAP,
						trap.ability, targets)).makeCommand(battle));
				}
			} catch (CommandException e) {
				throw new RuntimeException("Internal logic error triggering trap", e);
			}
		});

		return r;
	}
}

