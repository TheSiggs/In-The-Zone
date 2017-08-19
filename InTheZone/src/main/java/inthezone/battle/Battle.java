package inthezone.battle;

import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.FatigueCommand;
import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.AbilityZoneType;
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
	private final static int zoneTurns = 4;

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
	 * @return A list of affected zones
	 * */
	public List<Zone> doTurnStart(final Player player) {
		battleState.notifyTurn();
		if (flipRound) round += 1;
		flipRound = !flipRound;

		for (final Character c : battleState.characters)
			c.cleanupStatus(this, player);

		return battleState.removeExpiredZones();
	}
	
	/**
	 * Get the commands that happen on turn start
	 * @param player The player who's turn is starting
	 * */
	public List<Command> getTurnStart(final Player player) {
		final List<Command> r = new ArrayList<>();

		// apply fatigue damage first
		if (round > 7) {
			r.add(new FatigueCommand(
				battleState.characters.stream()
					.filter(c -> c.player == player)
					.map(c -> new DamageToTarget(
						new Casting(c.getPos(), c.getPos()), false, false,
						(int) Math.ceil(Ability.damageFormulaStatic(
							(round - 7) * fatigueEff, 0, 0, 0, 0, fatigueStats, c.getStats())),
						Optional.empty(), false, false))
					.collect(Collectors.toList())));
		}

		r.addAll(battleState.characters.stream()
			.flatMap(c -> c.turnReset(this, player).stream())
			.collect(Collectors.toList()));

		return r;
	}

	/**
	 * Turn start has a second phase now in which zones, traps, and obstacles
	 * interact.
	 * */
	public List<Command> getTurnStartPhase2() {
		final List<Command> r = new ArrayList<>();

		// triggers for non-character targetables.
		final Set<MapPoint> misc = battleState.targetable.stream()
			.filter(t -> !(t instanceof Character))
			.map(t -> {
				t.currentZone = Optional.empty();
				return t.getPos();
			})
			.collect(Collectors.toSet());

		final Trigger trigger = new Trigger(battleState);
		r.addAll(misc.stream()
			.flatMap(p -> trigger.getAllTriggers(p).stream())
			.collect(Collectors.toList()));

		return r;
	}

	/**
	 * Perform a move operation on a character.  Assumes the path has been fully
	 * validated.
	 * */
	public void doMove(final List<MapPoint> path, final boolean useMP) {
		battleState.getCharacterAt(path.get(0)).ifPresent(c -> {
			MapPoint target = path.get(path.size() - 1);
			if (useMP) c.moveTo(target,
				battleState.pathCost(path), battleState.hasMana(target));
			else c.teleport(target, battleState.hasMana(target));
		});
		battleState.updateRevengeBonus();
	}

	/**
	 * Perform an ability and update the game state accordingly.  Instant effects
	 * are handled separately.
	 * @param agent The location of the agent.  For traps and zones this is the
	 * actual location of the trap / zone, not the location of the character that
	 * cast the trap.
	 * @param ability The ability being used, from the original agent, not the
	 * trap or zone.
	 * */
	public void doAbility(
		final MapPoint agent,
		final AbilityAgentType agentType,
		final Ability ability,
		final Collection<DamageToTarget> targets
	) throws CommandException {
		if (agentType == AbilityAgentType.TRAP) {
			battleState.getTrapAt(agent).ifPresent(t -> t.defuse());
		} else if (agentType == AbilityAgentType.CHARACTER) {
			battleState.getCharacterAt(agent).ifPresent(c -> c.useAbility(ability));
		}

		for (final DamageToTarget d : targets) {
			final Targetable t;
			if (d.isTargetATrap) {
				Optional<Trap> mt = battleState.getTrapAt(d.target.target);
				if (!mt.isPresent()) continue;
				t = mt.get();

			} else if (d.isTargetAZone) {
				Optional<Zone> mt = battleState.getZoneAt(d.target.target);
				if (!mt.isPresent()) continue;
				t = mt.get();

			} else {
				t = battleState.getTargetableAt(d.target.target).stream()
					.filter(x -> !(x instanceof Trap)).findFirst().orElseThrow(() ->
						new CommandException("Expected targetable at " +
							d.target.toString() + " but there was none"));
			}
			
			t.dealDamage(d.damage);
			if (d.statusEffect.isPresent()) {
				try {
					t.applyStatus(this, d.statusEffect.get().resolve(battleState));
				} catch (ProtocolException e) {
					throw new CommandException("Invalid status effect", e);
				}
			}

			if (t.reap()) battleState.removeObstacle(t);
		}

		battleState.updateRevengeBonus();
	}

	/**
	 * Create traps.
	 * */
	public List<Trap> createTrap(
		final Ability ability,
		final Character agent,
		final Collection<MapPoint> ps
	) {
		agent.useAbility(ability);
		final List<Trap> r = new ArrayList<>();
		for (final MapPoint p : ps) {
			if (battleState.isSpaceFree(p) && !battleState.getTrapAt(p).isPresent())
				r.add(battleState.placeTrap(p, ability, agent, sprites));
		}
		battleState.updateRevengeBonus();
		return r;
	}

	/**
	 * Create a zone.
	 * @param ps Assume there is at least one
	 * @return at most one zone.
	 * */
	public List<Zone> createZone(
		final Ability ability,
		final Character agent,
		final Optional<RoadBlock> bind,
		final Collection<MapPoint> ps
	) {
		final Set<MapPoint> range = new HashSet<>();
		for (final MapPoint p : ps) range.addAll(battleState.getAffectedArea(
			p, AbilityAgentType.CHARACTER, ability, new Casting(p, p)));

		final Optional<Integer> turns = ability.info.zone == AbilityZoneType.BOUND_ZONE?
			Optional.empty() : Optional.of(zoneTurns);

		battleState.updateRevengeBonus();
		return battleState.placeZone(
				ps.iterator().next(), range, ability, turns, bind, agent)
			.map(x -> Stream.of(x))
			.orElse(Stream.empty()).collect(Collectors.toList());
	}

	/**
	 * Handle fatigue damage.
	 * */
	public void doFatigue(final Collection<DamageToTarget> targets) {
		for (final DamageToTarget d : targets) {
			final Character t = battleState.getCharacterAt(d.target.target)
				.orElseThrow(() -> new RuntimeException(
					"Attempted to attack non-target, command verification code failed"));
			t.dealDamage(d.damage);
		}
		battleState.updateRevengeBonus();
	}

	/**
	 * Handle item effects.
	 * */
	public void doUseItem(
		final MapPoint agent, final MapPoint target, final Item item
	) {
		battleState.getCharacterAt(agent).ifPresent(a ->
			battleState.getCharacterAt(target).ifPresent(t -> {
				item.doEffect(t);
				a.useItem(item);
			}));
		battleState.updateRevengeBonus();
	}

	/**
	 * Handle the push and pull effects.
	 * */
	public List<Targetable> doPushPull(
		final List<MapPoint> path, final boolean isFear
	) {
		final List<Targetable> r = new ArrayList<>();
		if (path.size() < 2) return r;

		battleState.getCharacterAt(path.get(0)).ifPresent(c -> {
			final MapPoint t = path.get(path.size() - 1);
			if (isFear) {
				c.moveTo(t, battleState.pathCost(path), battleState.hasMana(t));
			} else {
				c.push(t, battleState.hasMana(t));
			}
			r.add(c);
		});

		battleState.updateRevengeBonus();
		return r;
	}

	/**
	 * Determine if a push/pull is possible
	 * */
	public boolean canPushPull(final List<MapPoint> path) {
		if (path.size() < 2) return false;

		return battleState.getCharacterAt(path.get(0)).map(c ->
			battleState.isSpaceFree(path.get(path.size() - 1))).orElse(false);
	}

	/**
	 * Handle the teleport effect.
	 * */
	public List<Targetable> doTeleport(
		final MapPoint source, final MapPoint destination
	) {
		battleState.updateRevengeBonus();
		return battleState.getCharacterAt(source).map(c -> {
			c.teleport(destination, battleState.hasMana(destination));
			return Stream.of(c);
		}).orElse(Stream.empty()).collect(Collectors.toList());
	}

	/**
	 * Handle the cleanse effect.
	 * */
	public List<Targetable> doCleanse(final Collection<MapPoint> targets) {
		battleState.updateRevengeBonus();
		return targets.stream().flatMap(ot ->
				battleState.getTargetableAt(ot).stream()
					.map(t -> {t.cleanse(); return t;})
			).collect(Collectors.toList());
	}

	/**
	 * Handle the defuse effect.
	 * */
	public List<Targetable> doDefuse(final Collection<MapPoint> targets) {
		final List<Targetable> r = targets.stream().flatMap(ot ->
				battleState.getTargetableAt(ot).stream()
					.map(t -> {t.defuse(); return t;})
			).collect(Collectors.toList());

		r.stream().forEach(t -> {if (t.reap()) battleState.removeObstacle(t);});
		battleState.updateRevengeBonus();
		return r;
	}

	/**
	 * Handle the purge effect.
	 * */
	public List<Targetable> doPurge(final Collection<MapPoint> targets) {
		final List<Targetable> r = targets.stream().flatMap(ot ->
				battleState.getTargetableAt(ot).stream()
					.map(t -> {t.purge(); return t;})
			).collect(Collectors.toList());

		r.stream().forEach(t -> {if (t.reap()) battleState.removeObstacle(t);});
		battleState.updateRevengeBonus();
		return r;
	}

	/**
	 * Handle the obstacles effect
	 * */
	public List<Targetable> doObstacles(
		final Optional<AbilityInfo> a, final Collection<MapPoint> obstacles
	) {
		final List<Targetable> r = new ArrayList<>();
		for (final MapPoint p : obstacles) {
			r.add(battleState.placeObstacle(p, a, sprites));
		}
		battleState.updateRevengeBonus();
		return r;
	}

	/**
	 * Handle the revive effect.
	 * */
	public List<Targetable> doRevive(final Collection<MapPoint> targets) {
		final List<Targetable> r = new ArrayList<>();

		for (final MapPoint t : targets) {
			battleState.getCharacterAt(t).ifPresent(c -> {
				if (c.isDead()) {
					c.revive();
					r.add(c);
				}
			});
		}

		battleState.updateRevengeBonus();
		return r;
	}

	/**
	 * Handle the resign effect
	 * */
	public void doResign(final Player player, final boolean logoff) {
		battleState.resign(player, logoff);
	}
}

