package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.LineOfSight;
import inthezone.battle.PathFinderNode;
import inthezone.battle.Targetable;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.data.Player;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

/**
 * Push and pull instant effects.
 * */
public class PullPush extends InstantEffect {
	private final InstantEffectType kind;

	private final InstantEffectInfo type;
	private final MapPoint castFrom;
	public final List<List<MapPoint>> paths;
	private final int param;

	private final boolean isFear;

	/**
	 * @param isFear Set to true if this effect was created by the feared status
	 * effect, and the triggers have not been resolved yet.
	 * */
	private PullPush(
		final InstantEffectInfo type,
		final MapPoint castFrom,
		final List<List<MapPoint>> paths,
		final boolean isFear
	) {
		super(castFrom);
		this.kind = type.type;
		this.type = type;
		this.param = type.param;
		this.castFrom = castFrom;
		this.paths = paths;
		this.isFear = isFear;
	}

	@JSON
	private PullPush(
		@Field("kind") final InstantEffectType kind,
		@Field("type") final InstantEffectInfo type,
		@Field("castFrom") final MapPoint castFrom,
		@Field("paths") final List<List<MapPoint>> paths,
		@Field("isFear") final boolean isFear
	) throws ProtocolException {
		this(type, castFrom, paths, isFear);

		if (!(kind == InstantEffectType.PULL || kind == InstantEffectType.PUSH)
			|| kind != type.type) throw new ProtocolException("Expected push or pull effect");
	}

	/**
	 * @param isFear Same as for the constructor
	 * */
	public static PullPush getEffect(
		final BattleState battle,
		final InstantEffectInfo info,
		final MapPoint castFrom,
		final Set<MapPoint> targets,
		final boolean isFear
	) {
		final List<List<MapPoint>> paths = new ArrayList<>();

		final List<MapPoint> sortedTargets = new ArrayList<>(targets);
		sortedTargets.sort(Comparator.comparingInt(x -> castFrom.distance(x)));

		final Set<MapPoint> cleared = new HashSet<>();
		final Set<MapPoint> occupied = new HashSet<>();

		for (MapPoint t : sortedTargets) {
			final MapPoint to = getTo(info.type, t, castFrom, info.param);
			final List<MapPoint> path1 =
				getPullPath(battle, t, to, occupied, cleared, info.param,
					to.equals(castFrom), true);
			final List<MapPoint> path2 =
				getPullPath(battle, t, to, occupied, cleared, info.param,
					to.equals(castFrom), false);

			System.err.println("Pull paths: " + path1 + " and " + path2);
			final List<MapPoint> path = path1.size() > path2.size()?  path1 : path2; 
			if (path.size() > 0) {
				paths.add(path);
				cleared.add(path.get(0));
				occupied.add(path.get(path.size() - 1));
			}
		}

		return new PullPush(info, castFrom, paths, isFear);
	}

	private static MapPoint getTo(
		final InstantEffectType type,
		final MapPoint from,
		final MapPoint castFrom,
		final int range
	) {
		if (type == InstantEffectType.PULL) {
			return castFrom;
		} else if (type == InstantEffectType.PUSH) {
			return from.addScale(from.subtract(castFrom), range);
		} else {
			throw new RuntimeException("Invalid effect type, expected PUSH or PULL");
		}
	}

	private static List<MapPoint> getPullPath(
		final BattleState battle, final MapPoint from, final MapPoint to,
		final Set<MapPoint> occupied, final Set<MapPoint> cleared,
		final int limit, final boolean isPull, final boolean bias
	) {
		final List<MapPoint> los;
		if (isPull) {
			los = LineOfSight.getLOS(to, from, bias);
			Collections.reverse(los);
		} else {
			los = LineOfSight.getLOS(from, to, bias);
		}
		
		final List<MapPoint> path = new ArrayList<>();

		// make sure there is a character to pull and a path to pull along
		if (los == null || los.isEmpty()) return path;
		final Player player = battle.getCharacterAt(from).map(c -> c.player).orElse(null);
		if (player == null) return path;

		// Stop the path short at the first impassable object.
		MapPoint last = los.remove(0);
		path.add(last);
		while (
			battle.pathCost(path) < limit &&
			los.size() > 0 &&
			!occupied.contains(los.get(0)) &&
			(battle.canMoveThrough(los.get(0), player) || cleared.contains(los.get(0))) &&
			PathFinderNode.canTraverseBoundary(
				last, los.get(0), battle.terrain.terrain)
		) {
			last = los.remove(0);
			path.add(last);
		}

		// Trim invalid destinations off the end of the path.
		while (
			path.size() > 0 &&
				(occupied.contains(path.get(path.size() - 1)) ||
					!(battle.isSpaceFree(path.get(path.size() - 1)) ||
						cleared.contains(path.get(path.size() - 1))))
		) {
			path.remove(path.size() - 1);
		}

		return path.size() >= 2? path : new ArrayList<>();
	}

	@Override public List<Targetable> apply(final Battle battle) {
		return paths.stream()
			.flatMap(path -> battle.doPushPull(path, isFear).stream())
			.collect(Collectors.toList());
	}

	@Override public List<ExecutedCommand> applyComputingTriggers(
		final Battle battle, final Function<InstantEffect, Command> cmd
	) throws CommandException
	{
		final List<ExecutedCommand> r = new ArrayList<>();

		List<List<List<MapPoint>>> splitPaths = paths.stream()
			.map(path -> {
				if (path.size() == 0) return new ArrayList<List<MapPoint>>(); else {
					final Character agent =
						battle.battleState.getCharacterAt(path.get(0)).orElse(null);
					if (agent == null || isFear && !agent.isFeared()) {
						return new ArrayList<List<MapPoint>>();
					}	else {
						return battle.battleState.trigger.splitPath(agent, path);
					}
				}
			}).collect(Collectors.toList());

		while (!splitPaths.isEmpty()) {
			final List<List<MapPoint>> pathSections = new ArrayList<>();
			for (List<List<MapPoint>> sections : splitPaths) {
				if (!sections.isEmpty()) pathSections.add(sections.remove(0));
			}
			splitPaths = splitPaths.stream()
				.filter(x -> !x.isEmpty()).collect(Collectors.toList());

			final List<List<MapPoint>> validPathSections = pathSections.stream()
				.filter(x -> battle.canPushPull(x))
				.collect(Collectors.toList());

			// do the push/pull
			if (!validPathSections.isEmpty()) {
				final InstantEffect eff = new PullPush(
					this.type, this.castFrom, validPathSections, isFear);
				r.add(new ExecutedCommand(cmd.apply(eff), eff.apply(battle)));
			}

			// do the triggers
			for (List<MapPoint> path : pathSections) {
				final MapPoint loc = path.get(path.size() - 1);
				final List<Command> triggers = battle.battleState.trigger.getAllTriggers(loc);
				for (Command c : triggers) r.addAll(c.doCmdComputingTriggers(battle));

				final Optional<Character> oc = battle.battleState.getCharacterAt(loc);

				// if the character is no longer feared, stop the effect
				if (isFear && oc.map(c -> !c.isFeared()).orElse(false)) {
					return r;
				}
			}
		}

		return r;
	}

	@Override public Map<MapPoint, MapPoint> getRetargeting() {
		final Map<MapPoint, MapPoint> r = new HashMap<>();

		for (List<MapPoint> path : paths) {
			r.put(path.get(0), path.get(path.size() - 1));
		}
		return r;
	}

	@Override public InstantEffect retarget(
		final BattleState battle, final Map<MapPoint, MapPoint> retarget
	) {
		final Set<MapPoint> targets =
			paths.stream().map(p -> retarget.getOrDefault(p.get(0), p.get(0)))
			.collect(Collectors.toSet());

		return getEffect(battle, type,
			retarget.getOrDefault(castFrom, castFrom), targets, false);
	}
}

