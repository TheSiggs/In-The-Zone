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
	 * */
	public List<MapPoint> shrinkPath(List<MapPoint> path) {
		List<MapPoint> r = new ArrayList<>();
		Iterator<MapPoint> ps = path.iterator();
		r.add(ps.next());
		for (MapPoint p = ps.next(); ps.hasNext(); p = ps.next()) {
			r.add(p);
			Optional<Trap> t = battle.getTrapAt(p);
			if (t.isPresent()) {
				return r;
			}
		}

		return r;
	}

	/**
	 * Get the results of triggering all traps and zones at point p on character c.
	 * @param c The character that triggers the traps.
	 * @param p The position where the character will be when the traps/zones trigger
	 * */
	public List<Command> doTriggers(Character c, MapPoint p) {
		List<Command> r = new ArrayList<>();
		battle.getTrapAt(p).ifPresent(trap -> {
			// temporarily move the character
			MapPoint p0 = c.getPos();
			c.teleport(p, battle.hasMana(p));

			try {
				List<MapPoint> targets = new ArrayList<>(); targets.add(p);

				r.addAll((new UseAbilityCommandRequest(p, AbilityAgentType.TRAP, p,
					targets, trap.ability)).makeCommand(battle));
			} catch (CommandException e) {
				throw new RuntimeException("Internal logic error triggering trap", e);
			}

			// restore the character to its original place
			c.teleport(p0, battle.hasMana(p0));
		});

		return r;
	}
}

