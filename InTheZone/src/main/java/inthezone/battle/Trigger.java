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
	public List<MapPoint> shrinkPath(List<MapPoint> path) {
		Optional<Zone> currentZone = null;
		List<MapPoint> r = new ArrayList<>();
		for (MapPoint p : path) {
			r.add(p);

			Optional<Zone> newZone = battle.getZoneAt(p);
			if (currentZone == null) currentZone = newZone;
			if (!currentZone.equals(newZone)) return r;

			Optional<Trap> t = battle.getTrapAt(p);
			if (t.isPresent()) return r;
		}

		return r;
	}

	/**
	 * Split a path into multiple segments, such that each segment is a valid
	 * path, and triggers only occur on the ends of paths.
	 * @param path a non-empty path
	 * @return A list of path segments, each a valid path in its own right.  The
	 * first element in the return list may have length 1, which indicates that
	 * the original path started on a trigger point.
	 * */
	public List<List<MapPoint>> splitPath(List<MapPoint> path) {
		List<List<MapPoint>> r = new ArrayList<>();

		boolean pathAdded = false;

		Optional<Zone> currentZone = null;
		List<MapPoint> currentPath = new ArrayList<>();
		for (MapPoint p : path) {
			currentPath.add(p);
			pathAdded = false;

			Optional<Zone> newZone = battle.getZoneAt(p);
			if (currentZone == null) currentZone = newZone;

			Optional<Trap> t = battle.getTrapAt(p);
			if (t.isPresent() || !currentZone.equals(newZone)) {
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
		List<Command> r = new ArrayList<>();

		battle.getZoneAt(p).ifPresent(zone -> {
			for (Targetable t : battle.getTargetableAt(p))
				r.addAll(t.triggerZone(battle));
		});

		battle.getTrapAt(p).ifPresent(trap -> {
			try {
				List<MapPoint> targets = new ArrayList<>(); targets.add(p);

				r.addAll((new UseAbilityCommandRequest(p, AbilityAgentType.TRAP, p,
					targets, trap.ability)).makeCommand(battle));
			} catch (CommandException e) {
				throw new RuntimeException("Internal logic error triggering trap", e);
			}
		});

		return r;
	}
}

