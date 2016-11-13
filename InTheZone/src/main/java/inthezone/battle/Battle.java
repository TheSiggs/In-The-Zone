package inthezone.battle;

import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.FatigueCommand;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.Stats;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class processes battle commands.
 * */
public class Battle {
	public final BattleState battleState;
	private final StandardSprites sprites;

	public Battle(BattleState battleState, StandardSprites sprites) {
		this.battleState = battleState;
		this.sprites = sprites;
	}

	private static final double fatigueEff = 0.3;
	private static final Stats fatigueStats =
		new Stats(0, 0, 20 /* power */, 0, 16 /* attack */, 0);

	private int round = -1;
	private boolean flipRound = true;

	/**
	 * Perform operations at the start of a player's turn.
	 * @param player The player who's turn is starting.
	 * */
	public List<Command> doTurnStart(Player player) {
		if (flipRound) round += 1;
		flipRound = !flipRound;

		battleState.removeExpiredZones();

		List<Command> r = battleState.characters.stream()
			.flatMap(c -> c.turnReset(this, player).stream())
			.collect(Collectors.toList());

		if (round > 7) {
			r.add(new FatigueCommand(
				battleState.characters.stream()
					.filter(c -> c.player == player)
					.map(c -> new DamageToTarget(c.getPos(), false,
						(int) Math.ceil(Ability.damageFormulaStatic(
							(round - 7) * fatigueEff, 0, 0, 0, 0, fatigueStats, c.getStats())),
						Optional.empty(), false, false))
					.collect(Collectors.toList())));
		}

		return r;
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
	 * @param agent The location of the agent.  For traps and zones this is the
	 * actual location of the trap / zone, not the location of the character that
	 * cast the trap.
	 * */
	public void doAbility(
		MapPoint agent,
		AbilityAgentType agentType,
		Ability ability,
		Collection<DamageToTarget> targets
	) throws CommandException {
		if (agentType == AbilityAgentType.TRAP) {
			battleState.getTrapAt(agent).ifPresent(t -> t.defuse());
		} else if (agentType == AbilityAgentType.CHARACTER) {
			battleState.getCharacterAt(agent).ifPresent(c -> c.useAbility(ability));
		}

		for (DamageToTarget d : targets) {
			final Targetable t;
			if (d.isTargetATrap) {
				t = battleState.getTrapAt(d.target).orElseThrow(() ->
					new CommandException("Expected trap at " +
						d.target.toString() + " but there was none"));
			} else {
				t = battleState.getTargetableAt(d.target).stream()
					.filter(x -> !(x instanceof Trap)).findFirst().orElseThrow(() ->
						new CommandException("Expected targetable at " +
							d.target.toString() + " but there was none"));
			}
			
			t.dealDamage(d.damage);
			if (d.statusEffect.isPresent()) {
				try {
					t.applyStatus(d.statusEffect.get().resolve(battleState));
				} catch (ProtocolException e) {
					throw new CommandException("Invalid status effect", e);
				}
			}

			if (t.reap()) battleState.removeObstacle(t);
		}
	}

	/**
	 * Create traps
	 * */
	public List<Trap> createTrap(
		Ability ability, Character agent, Collection<MapPoint> ps
	) {
		List<Trap> r = new ArrayList<>();
		for (MapPoint p : ps) {
			r.add(battleState.placeTrap(p, ability, agent, sprites));
		}
		return r;
	}

	/**
	 * Create a zone.
	 * @param ps Assume there is at least one
	 * @return at most one zone.
	 * */
	public List<Zone> createZone(
		Ability ability, Character agent, Collection<MapPoint> ps
	) {
		Set<MapPoint> range = new HashSet<>();
		for (MapPoint p : ps) range.addAll(battleState.getAffectedArea(
			p, AbilityAgentType.ZONE, p, ability, p));

		return battleState.placeZone(
				ps.iterator().next(), range, ability, ability.info.zoneTurns, agent)
			.map(x -> Stream.of(x))
			.orElse(Stream.empty()).collect(Collectors.toList());
	}

	/**
	 * Handle fatigue damage.
	 * */
	public void doFatigue(Collection<DamageToTarget> targets) {
		for (DamageToTarget d : targets) {
			Character t = battleState.getCharacterAt(d.target)
				.orElseThrow(() -> new RuntimeException(
					"Attempted to attack non-target, command verification code failed"));
			t.dealDamage(d.damage);
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

	public List<Targetable> doCleanse(Collection<MapPoint> targets) {
		return targets.stream().flatMap(ot ->
				battleState.getTargetableAt(ot).stream()
					.map(t -> {t.cleanse(); return t;})
			).collect(Collectors.toList());
	}

	public List<Targetable> doDefuse(Collection<MapPoint> targets) {
		List<Targetable> r = targets.stream().flatMap(ot ->
				battleState.getTargetableAt(ot).stream()
					.map(t -> {t.defuse(); return t;})
			).collect(Collectors.toList());

		r.stream().forEach(t -> {if (t.reap()) battleState.removeObstacle(t);});
		return r;
	}

	public List<Targetable> doPurge(Collection<MapPoint> targets) {
		return targets.stream().flatMap(ot ->
				battleState.getTargetableAt(ot).stream()
					.map(t -> {t.purge(); return t;})
			).collect(Collectors.toList());
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

