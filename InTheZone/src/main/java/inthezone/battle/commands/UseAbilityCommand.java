package inthezone.battle.commands;

import java.util.Collection;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Casting;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
import inthezone.battle.data.AbilityZoneType;
import inthezone.battle.RoadBlock;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UseAbilityCommand extends Command {
	private MapPoint agent;
	public final AbilityAgentType agentType;
	public final String ability;
	public final String friendlyAbilityName;
	private final Collection<MapPoint> targetSquares;
	private Collection<DamageToTarget> targets;
	public final int subsequentLevel;

	private final List<MapPoint> constructed = new ArrayList<>();

	public Collection<DamageToTarget> getTargets() {return targets;}

	// These are set when the command is executed so they can be available to the
	// GUI layer.
	public boolean placedTraps = false;
	public boolean placedZones = false;

	public UseAbilityCommand(
		final MapPoint agent, final AbilityAgentType agentType,
		final String ability,
		final String friendlyAbilityName,
		final Collection<MapPoint> targetSquares,
		final Collection<DamageToTarget> targets,
		final int subsequentLevel
	) {
		this(agent, agentType, ability, friendlyAbilityName,
			targetSquares, targets, new ArrayList<>(), subsequentLevel);
	}

	public UseAbilityCommand(
		final MapPoint agent, final AbilityAgentType agentType,
		final String ability,
		final String friendlyAbilityName,
		final Collection<MapPoint> targetSquares,
		final Collection<DamageToTarget> targets,
		final Collection<MapPoint> constructed,
		final int subsequentLevel
	) {
		this.agent = agent;
		this.agentType = agentType;
		this.friendlyAbilityName = friendlyAbilityName;
		this.ability = ability;
		this.targetSquares = targetSquares;
		this.targets = targets;
		this.constructed.addAll(constructed);
		this.subsequentLevel = subsequentLevel;
		canCancel = subsequentLevel == 0;
	}

	/**
	 * A hack to mark that this command has a pre-effect, and therefore cannot be
	 * cancelled after all.
	 * */
	void notifyHasPreEffect() {
		canCancel = false;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("kind", CommandKind.ABILITY.toString());
		r.put("agent", agent.getJSON());
		r.put("agentType", agentType.toString());
		r.put("ability", ability);
		r.put("fability", friendlyAbilityName);
		r.put("subsequentLevel", subsequentLevel);
		final JSONArray ta = new JSONArray();
		for (DamageToTarget d : targets) ta.put(d.getJSON());
		r.put("targets", ta);

		final JSONArray tsa = new JSONArray();
		for (MapPoint p : targetSquares) tsa.put(p.getJSON());
		r.put("targetSquares", tsa);

		final JSONArray cs = new JSONArray();
		for (MapPoint p : constructed) cs.put(p.getJSON());
		r.put("constructed", cs);
		return r;
	}

	public static UseAbilityCommand fromJSON(final JSONObject json)
		throws ProtocolException
	{
		try {
			final CommandKind kind = CommandKind.fromString(json.getString("kind"));
			final MapPoint agent = MapPoint.fromJSON(json.getJSONObject("agent"));
			final AbilityAgentType agentType = AbilityAgentType.fromString(json.getString("agentType"));
			final String ability = json.getString("ability");
			final String friendlyAbilityName = json.getString("fability");
			final JSONArray rawTargets = json.getJSONArray("targets");
			final JSONArray rawTargetSquares = json.getJSONArray("targetSquares");
			final JSONArray rawConstructed = json.getJSONArray("constructed");
			final int subsequentLevel = json.getInt("subsequentLevel");

			if (kind != CommandKind.ABILITY)
				throw new ProtocolException("Expected ability command");

			final Collection<DamageToTarget> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.length(); i++) {
				targets.add(DamageToTarget.fromJSON(rawTargets.getJSONObject(i)));
			}

			final Collection<MapPoint> targetSquares = new ArrayList<>();
			for (int i = 0; i < rawTargetSquares.length(); i++) {
				targetSquares.add(MapPoint.fromJSON(rawTargetSquares.getJSONObject(i)));
			}

			final Collection<MapPoint> constructed = new ArrayList<>();
			for (int i = 0; i < rawConstructed.length(); i++) {
				constructed.add(MapPoint.fromJSON(rawConstructed.getJSONObject(i)));
			}

			return new UseAbilityCommand(
				agent, agentType, ability, friendlyAbilityName,
				targetSquares, targets, constructed, subsequentLevel);

		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing ability command, " + e.getMessage(), e);
		}
	}

	@Override
	public List<? extends Targetable> doCmd(final Battle battle) throws CommandException {
		final Ability abilityData = battle.battleState.getCharacterAt(agent)
			.flatMap(c -> Stream.concat(Stream.of(c.basicAbility), c.abilities.stream())
				.filter(a -> a.rootName.equals(ability)).findFirst())
			.flatMap(a -> a.getNext(
				battle.battleState.hasMana(agent), subsequentLevel))
			.orElse(null);

		if (abilityData == null && agentType == AbilityAgentType.CHARACTER)
			throw new CommandException("52: No such ability " + ability);

		final List<Targetable> r = new ArrayList<>();

		if (agentType == AbilityAgentType.CHARACTER && abilityData.info.trap) {
			placedTraps = true;

			// don't place traps on defusing zones
			final Collection<MapPoint> realTargets = targetSquares.stream()
				.filter(p -> !battle.battleState.hasDefusingZone(p))
				.collect(Collectors.toList());

			battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
			r.addAll(battle.battleState.getCharacterAt(agent)
				.map(c -> battle.createTrap(abilityData, c, realTargets))
				.orElseThrow(() -> new CommandException("53: Missing ability agent for " + ability)));

		} else {
			battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
			battle.battleState.getTrapAt(agent).ifPresent(t -> r.add(t));
			for (DamageToTarget d : targets) {
				battle.battleState.getTargetableAt(d.target.target).forEach(t -> r.add(t));
			}

			// do the ability now
			battle.doAbility(agent, agentType, abilityData, targets);
			r.addAll(battle.battleState.removeExpiredZones());

			// If it's a zone ability, also create the zone
			// bound zones
			if (
				agentType == AbilityAgentType.CHARACTER &&
				abilityData.info.zone == AbilityZoneType.BOUND_ZONE
			) {
				Optional<RoadBlock> o = constructed.stream()
					.flatMap(p -> battle.battleState.getTargetableAt(p).stream())
					.filter(t -> t instanceof RoadBlock).findFirst().map(t -> (RoadBlock) t);

				if (o.isPresent()) {
					placedZones = true;
					r.addAll(battle.battleState.getCharacterAt(agent)
						.map(c -> battle.createZone(abilityData, c, o, targetSquares))
						.orElseThrow(() -> new CommandException("54: Invalid ability command")));
				}

			// unbound zones
			} else if (
				agentType == AbilityAgentType.CHARACTER &&
				abilityData.info.zone == AbilityZoneType.ZONE
			) {
				placedZones = true;
				r.addAll(battle.battleState.getCharacterAt(agent)
					.map(c -> battle.createZone(
						abilityData, c, Optional.empty(), targetSquares))
					.orElseThrow(() -> new CommandException("55: Invalid ability command")));
			}
		}

		r.addAll(battle.battleState.characters);
		return r;
	}

	/**
	 * Called when the targets are moved by an instant effect.
	 * @param retarget A mapping from old character positions to their new positions.
	 * */
	public void retarget(final Map<MapPoint, MapPoint> retarget) {
		this.agent = retarget.getOrDefault(agent, agent);
		this.targets = targets.stream()
			.map(t -> t.retarget(new Casting(
				retarget.getOrDefault(t.target.castFrom, t.target.castFrom),
				retarget.getOrDefault(t.target.target, t.target.target))))
			.collect(Collectors.toList());
	}

	/**
	 * Called when new objects are created by instant effects.
	 * */
	public void registerConstructedObjects(final List<MapPoint> constructed) {
		this.constructed.addAll(constructed);
	}
}

