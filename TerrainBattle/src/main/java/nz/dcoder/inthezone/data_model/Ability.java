package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nz.dcoder.inthezone.data_model.pure.EffectName;
import nz.dcoder.inthezone.data_model.pure.LineOfSight;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Pairs an AbilityInfo with a means to apply the ability's effect.
 * */
abstract public class Ability {
	public final EffectName name;
	public final AbilityInfo info;

	public Ability(EffectName name, AbilityInfo info) {
		this.name = name;
		this.info = info;
	}

	public abstract void applyEffect(
		CanDoAbility agent, Position target, Battle battle);
	public abstract boolean canApplyEffect(
		CanDoAbility agent, Position target, Battle battle);

	/**
	 * Get the squares that are affected by an ability
	 * @param agentPosition The position of the agent doing the ability
	 * @param target The square the agent is targeting
	 * @param battle The current battle
	 * @return A collection containing all the affected squares
	 * */
	public Collection<Position> getAffectedArea(
		Position agentPosition, Position target, Battle battle
	) {
		final List<Position> affected = new ArrayList<Position>();
		affected.add(target);

		if (info.isPiercing) {
			affected.addAll(LineOfSight.getLOS(agentPosition, target, true));
		}

		if (info.areaOfEffect > 0) {
			List<Position> diamond =
				LineOfSight.getDiamond(target, info.areaOfEffect);

			if (!info.hasAOEShading) {
				affected.addAll(diamond);
			} else {
				// only add the squares that have unobstructed line of sight from the
				// target position
				diamond.stream().filter(p -> {
						List<Position> los1 = LineOfSight.getLOS(target, p, true);
						List<Position> los2 = LineOfSight.getLOS(target, p, false);
						los1.remove(los1.size() - 1); // we only need LOS up to the square
						los2.remove(los2.size() - 1); // the square itself may be blocked
						
						return los1.stream()
								.map(l -> battle.getObjectAt(l))
								.noneMatch(o -> o != null && o.blocksPath) ||
							los2.stream()
								.map(l -> battle.getObjectAt(l))
								.noneMatch(o -> o != null && o.blocksPath);
					}).forEach(a -> affected.add(a));
			}
		}

		return affected;
	}

	/**
	 * Determine if an agent has line of sight to a target square for this ability
	 * @param agentPosition The position of the agent doing the ability
	 * @param target The square the agent is targeting
	 * @param battle The current battle
	 * @return true if the agent has line of sight, otherwise false
	 * */
	protected boolean hasLineOfSight(
		Position agentPosition, Position target, Battle battle
	) {
			List<Position> los1 = LineOfSight.getLOS(agentPosition, target, true);
			List<Position> los2 = LineOfSight.getLOS(agentPosition, target, false);

			boolean allClear;
			if (info.isPiercing) {
				allClear =
					los1.stream().map(p -> battle.getObjectAt(p))
						.noneMatch(o -> o != null && o.blocksPath && !o.isAttackable) ||
					los2.stream().map(p -> battle.getObjectAt(p))
						.noneMatch(o -> o != null && o.blocksPath && !o.isAttackable);

			} else {
				allClear =
					los1.stream().map(p -> battle.getObjectAt(p))
						.noneMatch(o -> o != null && o.blocksPath) ||
					los2.stream().map(p -> battle.getObjectAt(p))
						.noneMatch(o -> o != null && o.blocksPath);

				// there may be a character at the target position, but not anywhere
				// along the LOS, hence we now ignore the last element of the LOS.
				los1.remove(los1.size() - 1);
				los2.remove(los2.size() - 1);
				allClear &=
					los1.stream().map(p -> battle.getCharacterAt(p))
						.noneMatch(c -> c != null) ||
					los2.stream().map(p -> battle.getCharacterAt(p))
						.noneMatch(c -> c != null);
			}
			return allClear;
	}
}

