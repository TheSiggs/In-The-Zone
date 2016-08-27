package inthezone.battle;

import inthezone.battle.data.Player;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
			MapPoint target = path.get(path.size() - 1);
			c.moveTo(target, battleState.hasMana(target));
		});
	}

	public void doAttack(MapPoint agent, Collection<DamageToTarget> targets) {
	}

	public void doAbility(
		MapPoint agent,
		Ability ability,
		Collection<DamageToTarget> targets
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

	public List<Character> doPush(MapPoint agent, MapPoint target, int amount) {
		if (agent.x == target.x || agent.y == target.y) {
			MapPoint dp = target.subtract(agent).normalise();
			MapPoint x = target;
			for (int i = 0; i < amount; i++) {
				MapPoint z = x.add(dp);
				if (battleState.isSpaceFree(z)) x = z; else break;
			}

			final MapPoint destination = x;
			return battleState.getCharacterAt(target).map(c -> {
				c.teleport(destination, battleState.hasMana(destination));
				return Stream.of(c);
			}).orElse(Stream.empty()).collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	public List<Character> doCleanse(MapPoint target) {
		return battleState.getCharacterAt(target).map(c -> {
			c.cleanse();
			return Stream.of(c);
		}).orElse(Stream.empty()).collect(Collectors.toList());
	}

	public void doResign(Player player) {
		battleState.characters.stream()
			.filter(c -> c.player == player)
			.forEach(c -> c.kill());
	}
}

