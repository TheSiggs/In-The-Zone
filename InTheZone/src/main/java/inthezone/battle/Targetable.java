package inthezone.battle;

import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.UseAbilityCommandRequest;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.Stats;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.status.StatusEffect;

public abstract class Targetable implements Obstacle {
	public abstract Stats getStats();
	public abstract MapPoint getPos();
	public abstract double getAttackBuff();
	public abstract double getDefenceBuff();
	public abstract void dealDamage(final int damage);
	public abstract void defuse();
	public abstract void cleanse();
	public abstract void purge();
	public abstract void revive();
	public abstract void applyStatus(
		final Battle battle, final StatusEffect status);
	public abstract boolean isPushable();
	public abstract boolean isEnemyOf(final Character character);
	public abstract boolean isDead();

	public abstract boolean hasMana();
	public abstract double getChanceBuff();

	public abstract TargetableFrozen freeze();

	// get the sprite that represents this object
	public abstract SpriteInfo getSprite();

	// return true if this targetable should be removed from the board
	public abstract boolean reap();

	public Optional<Zone> currentZone = Optional.empty();

	public boolean isAffectedBy(final StatusEffectInfo status) {
		return false;
	}

	public abstract boolean isAffectedBy(final InstantEffectInfo instant);

	/**
	 * Generate a zone effect, if the triggering conditions are met.
	 * */
	public List<Command> triggerZone(final BattleState battle) {
		final MapPoint pos = getPos();
		final List<Command> r = new ArrayList<>();

		final Optional<Zone> newZone = battle.getZoneAt(pos);
		if (!newZone.equals(currentZone)) {
			currentZone = newZone;
			newZone.ifPresent(zone -> {
				try {
					final List<Casting> castings = new ArrayList<>(); castings.add(new Casting(pos, pos));

					r.addAll((new UseAbilityCommandRequest(pos, AbilityAgentType.ZONE,
						zone.ability, castings)).makeCommand(battle));
					System.err.println("Constructed zone effect");
				} catch (CommandException e) {
					throw new RuntimeException("Internal logic error triggering zone", e);
				}
			});
		}

		return r;
	}
}

