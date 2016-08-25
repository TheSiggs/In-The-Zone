package inthezone.battle;

import inthezone.battle.data.Player;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.List;

/**
 * This class processes battle commands.
 * */
public class Battle {
	public BattleState battleState;

	public Battle(BattleState battleState) {
		this.battleState = battleState;
	}

	/**
	 * Perform operations at the start of a player's turn.
	 * */
	public void doTurnStart(Player player) {
		for (Character c : battleState.characters) c.turnReset(player);
	}

	/**
	 * Perform a move operation on a character.  Assumes the path has been fully
	 * validated.
	 * */
	public void doMove(List<MapPoint> path) {
		battleState.getCharacterAt(path.get(0)).ifPresent(c -> {
			c.moveTo(path.get(path.size() - 1));
		});
	}

	public void doAttack(MapPoint agent, Collection<DamageToTarget> targets) {
	}

	public void doAbility(
		MapPoint agent, Ability ability, Collection<DamageToTarget> targets
	) {
		battleState.getCharacterAt(agent).ifPresent(c -> c.useAbility(ability));

		for (DamageToTarget d : targets) {
			Targetable t = battleState.getTargetableAt(d.target)
				.orElseThrow(() -> new RuntimeException(
					"Attempted to attack non-target, command verification code failed"));

			t.dealDamage(d.damage);
		}
	}

	public void doUseItem(MapPoint agent, Item item) {
	}

	public void doPush(MapPoint agent, MapPoint target, boolean effective) {
	}

	public void doResign(Player player) {
		battleState.characters.stream()
			.filter(c -> c.player == player)
			.forEach(c -> c.kill());
	}
}

