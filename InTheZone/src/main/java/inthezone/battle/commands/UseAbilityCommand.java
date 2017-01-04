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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class UseAbilityCommand extends Command {
	private MapPoint agent;
	public final AbilityAgentType agentType;
	public final String ability;
	private final Collection<MapPoint> targetSquares;
	private Collection<DamageToTarget> targets;
	public final int subsequentLevel;

	private final List<MapPoint> constructed = new ArrayList<>();

	public Collection<DamageToTarget> getTargets() {return targets;}

	public UseAbilityCommand(
		MapPoint agent, AbilityAgentType agentType,
		String ability,
		Collection<MapPoint> targetSquares,
		Collection<DamageToTarget> targets,
		int subsequentLevel
	) {
		this.agent = agent;
		this.agentType = agentType;
		this.ability = ability;
		this.targetSquares = targetSquares;
		this.targets = targets;
		this.subsequentLevel = subsequentLevel;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("kind", CommandKind.ABILITY.toString());
		r.put("agent", agent.getJSON());
		r.put("agentType", agentType.toString());
		r.put("ability", ability);
		r.put("subsequentLevel", subsequentLevel);
		JSONArray ta = new JSONArray();
		for (DamageToTarget d : targets) ta.add(d.getJSON());
		r.put("targets", ta);

		JSONArray tsa = new JSONArray();
		for (MapPoint p : targetSquares) tsa.add(p.getJSON());
		r.put("targetSquares", tsa);
		return r;
	}

	public static UseAbilityCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object oagent = json.get("agent");
		Object oagentType = json.get("agentType");
		Object oability = json.get("ability");
		Object otargets = json.get("targets");
		Object otargetSquares = json.get("targetSquares");
		Object osubsequentLevel = json.get("subsequentLevel");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (oagent == null) throw new ProtocolException("Missing ability agent");
		if (oagentType == null) throw new ProtocolException("Missing ability agent type");
		if (oability == null) throw new ProtocolException("Missing ability");
		if (otargets == null) throw new ProtocolException("Missing ability targets");
		if (otargets == null) throw new ProtocolException("Missing ability targetSqaures");
		if (osubsequentLevel == null) throw new ProtocolException("Missing ability subsequent level");

		if (CommandKind.fromString((String) okind) != CommandKind.ABILITY)
			throw new ProtocolException("Expected ability command");

		try {
			MapPoint agent = MapPoint.fromJSON((JSONObject) oagent);
			AbilityAgentType agentType =
				AbilityAgentType.fromString((String) oagentType);
			String ability = (String) oability;
			Number subsequentLevel = (Number) osubsequentLevel;

			JSONArray rawTargets = (JSONArray) otargets;
			Collection<DamageToTarget> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.size(); i++) {
				targets.add(DamageToTarget.fromJSON((JSONObject) rawTargets.get(i)));
			}

			JSONArray rawTargetSquares = (JSONArray) otargetSquares;
			Collection<MapPoint> targetSquares = new ArrayList<>();
			for (int i = 0; i < rawTargetSquares.size(); i++) {
				targetSquares.add(MapPoint.fromJSON((JSONObject) rawTargetSquares.get(i)));
			}

			return new UseAbilityCommand(
				agent, agentType, ability, targetSquares, targets,
				subsequentLevel.intValue());
		} catch (ClassCastException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing ability command", e);
		}
	}

	@Override
	public List<? extends Targetable> doCmd(Battle battle) throws CommandException {
		final Ability abilityData = battle.battleState.getCharacterAt(agent)
			.flatMap(c -> Stream.concat(Stream.of(c.basicAbility), c.abilities.stream())
				.filter(a -> a.info.name.equals(ability)).findFirst())
			.flatMap(a -> a.getNext(
				battle.battleState.hasMana(agent), subsequentLevel))
			.orElse(null);

		if (abilityData == null && agentType == AbilityAgentType.CHARACTER)
			throw new CommandException("52: No such ability");

		final List<Targetable> r = new ArrayList<>();

		if (agentType == AbilityAgentType.CHARACTER && abilityData.info.trap) {
			battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
			r.addAll(battle.battleState.getCharacterAt(agent)
				.map(c -> battle.createTrap(abilityData, c, targetSquares))
				.orElseThrow(() -> new CommandException("53: Missing ability agent")));

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
				System.err.println("Make bound zone with " + constructed.toString());
				System.err.println("Agent is " + battle.battleState.getCharacterAt(agent).toString()); 

				Optional<RoadBlock> o = constructed.stream()
					.flatMap(p -> battle.battleState.getTargetableAt(p).stream())
					.filter(t -> t instanceof RoadBlock).findFirst().map(t -> (RoadBlock) t);

				if (o.isPresent()) {
					r.addAll(battle.battleState.getCharacterAt(agent)
						.map(c -> battle.createZone(abilityData, c, o, targetSquares))
						.orElseThrow(() -> new CommandException("54: Invalid ability command")));
				}

			// unbound zones
			} else if (
				agentType == AbilityAgentType.CHARACTER &&
				abilityData.info.zone == AbilityZoneType.ZONE
			) {
				r.addAll(battle.battleState.getCharacterAt(agent)
					.map(c -> battle.createZone(
						abilityData, c, Optional.empty(), targetSquares))
					.orElseThrow(() -> new CommandException("55: Invalid ability command")));
			}
		}

		return r;
	}

	/**
	 * Called when the targets are moved by an instant effect.
	 * @param retarget A mapping from old character positions to their new positions.
	 * */
	public void retarget(Map<MapPoint, MapPoint> retarget) {
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
	public void registerConstructedObjects(List<MapPoint> constructed) {
		this.constructed.addAll(constructed);
	}
}

