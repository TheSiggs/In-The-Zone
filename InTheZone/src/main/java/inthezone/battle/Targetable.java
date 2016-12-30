package inthezone.battle;

import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.UseAbilityCommandRequest;
import inthezone.battle.data.Player;
import inthezone.battle.data.Stats;
import inthezone.battle.status.StatusEffect;
import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Targetable implements Obstacle {
	public abstract Stats getStats();
	public abstract MapPoint getPos();
	public abstract double getAttackBuff();
	public abstract double getDefenceBuff();
	public abstract void dealDamage(int damage);
	public abstract void defuse();
	public abstract void cleanse();
	public abstract void purge();
	public abstract void applyStatus(StatusEffect status);
	public abstract boolean isPushable();
	public abstract boolean isEnemyOf(Character character);
	public abstract boolean isDead();

	public abstract boolean hasMana();
	public abstract double getChanceBuff();

	public abstract Targetable clone();

	// get the sprite that represents this object
	public abstract SpriteInfo getSprite();

	// return true if this targetable should be removed from the board
	public abstract boolean reap();

	public Optional<Zone> currentZone = Optional.empty();

	/**
	 * Generate a zone effect, if the triggering conditions are met.
	 * */
	public List<Command> triggerZone(BattleState battle) {
		final MapPoint pos = getPos();
		List<Command> r = new ArrayList<>();

		Optional<Zone> newZone = battle.getZoneAt(pos);
		if (!newZone.equals(currentZone)) {
			currentZone = newZone;
			newZone.ifPresent(zone -> {
				try {
					List<MapPoint> targets = new ArrayList<>(); targets.add(pos);

					r.addAll((new UseAbilityCommandRequest(pos, AbilityAgentType.ZONE, pos,
						targets, zone.ability)).makeCommand(battle));
					System.err.println("Constructed zone effect");
				} catch (CommandException e) {
					throw new RuntimeException("Internal logic error triggering zone", e);
				}
			});
		}

		return r;
	}

}

