package inthezone.battle;

import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
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
	private StandardSprites sprites;

	public Battle(BattleState battleState, StandardSprites sprites) {
		this.battleState = battleState;
		this.sprites = sprites;
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

	/**
	 * Perform an ability and update the game state accordingly.  Instant effects
	 * are handled separately.
	 * */
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
			if (t.reap()) battleState.removeObstacle(t);
		}
	}

	public void doUseItem(MapPoint agent, Item item) {
	}

	public List<Targetable> doPushPull(List<MapPoint> path) {
		List<Targetable> r = new ArrayList<>();
		if (path.size() < 2) return r;

		battleState.getCharacterAt(path.get(0)).ifPresent(c -> {
			MapPoint t = path.get(path.size() - 1);
			c.teleport(t, battleState.hasMana(t));
			r.add(c);
		});

		return r;
	}

	public List<Targetable> doTeleport(MapPoint source, MapPoint destination) {
		return battleState.getCharacterAt(source).map(c -> {
			c.teleport(destination, battleState.hasMana(destination));
			return Stream.of(c);
		}).orElse(Stream.empty()).collect(Collectors.toList());
	}

	public List<Targetable> doCleanse(MapPoint target) {
		return battleState.getCharacterAt(target).map(c -> {
			c.cleanse();
			return Stream.of(c);
		}).orElse(Stream.empty()).collect(Collectors.toList());
	}

	public List<Targetable> doObstacles(Collection<MapPoint> obstacles) {
		List<Targetable> r = new ArrayList<>();
		for (MapPoint p : obstacles) {
			r.add(battleState.placeObstacle(p, sprites));
		}
		return r;
	}

	public void doResign(Player player) {
		battleState.characters.stream()
			.filter(c -> c.player == player)
			.forEach(c -> c.kill());
	}
}

