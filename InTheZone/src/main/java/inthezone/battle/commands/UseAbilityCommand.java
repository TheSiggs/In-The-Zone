package inthezone.battle.commands;

import java.util.Collection;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
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
	private AbilityAgentType agentType;
	private final MapPoint castFrom;
	public final String ability;
	private final Collection<MapPoint> targetSquares;
	private Collection<DamageToTarget> targets;
	public final int subsequentLevel;
	public final int recursionLevel;

	public Collection<DamageToTarget> getTargets() {return targets;}

	public UseAbilityCommand(
		MapPoint agent, AbilityAgentType agentType,
		MapPoint castFrom, String ability,
		Collection<MapPoint> targetSquares,
		Collection<DamageToTarget> targets,
		int subsequentLevel, int recursionLevel
	) {
		this.agent = agent;
		this.agentType = agentType;
		this.castFrom = castFrom;
		this.ability = ability;
		this.targetSquares = targetSquares;
		this.targets = targets;
		this.subsequentLevel = subsequentLevel;
		this.recursionLevel = recursionLevel;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("kind", CommandKind.ABILITY.toString());
		r.put("agent", agent.getJSON());
		r.put("agentType", agentType.toString());
		r.put("castFrom", castFrom.getJSON());
		r.put("ability", ability);
		r.put("subsequentLevel", subsequentLevel);
		r.put("recursionLevel", recursionLevel);
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
		Object ocastFrom = json.get("castFrom");
		Object oability = json.get("ability");
		Object otargets = json.get("targets");
		Object otargetSquares = json.get("targetSquares");
		Object osubsequentLevel = json.get("subsequentLevel");
		Object orecursionLevel = json.get("recursionLevel");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (oagent == null) throw new ProtocolException("Missing ability agent");
		if (oagentType == null) throw new ProtocolException("Missing ability agent type");
		if (ocastFrom == null) throw new ProtocolException("Missing tile to cast ability from");
		if (oability == null) throw new ProtocolException("Missing ability");
		if (otargets == null) throw new ProtocolException("Missing ability targets");
		if (otargets == null) throw new ProtocolException("Missing ability targetSqaures");
		if (osubsequentLevel == null) throw new ProtocolException("Missing ability subsequent level");
		if (orecursionLevel == null) throw new ProtocolException("Missing ability recursion level");

		if (CommandKind.fromString((String) okind) != CommandKind.ABILITY)
			throw new ProtocolException("Expected ability command");

		try {
			MapPoint agent = MapPoint.fromJSON((JSONObject) oagent);
			AbilityAgentType agentType =
				AbilityAgentType.fromString((String) oagentType);
			MapPoint castFrom = MapPoint.fromJSON((JSONObject) ocastFrom);
			String ability = (String) oability;
			Number subsequentLevel = (Number) osubsequentLevel;
			Number recursionLevel = (Number) orecursionLevel;

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
				agent, agentType, castFrom, ability, targetSquares, targets,
				subsequentLevel.intValue(), recursionLevel.intValue());
		} catch (ClassCastException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing ability command", e);
		}
	}

	@Override
	public List<? extends Targetable> doCmd(Battle battle) throws CommandException {
		Ability abilityData;

		if (agentType == AbilityAgentType.TRAP) {
			abilityData = battle.battleState.getTrapAt(castFrom)
				.map(t -> t.ability).orElseThrow(() ->
					new CommandException("Invalid ability command"));

		} else if (agentType == AbilityAgentType.ZONE) {
			abilityData = battle.battleState.getZoneAt(castFrom)
				.map(z -> z.ability).orElseThrow(() ->
					new CommandException("Invalid ability command"));

		} else {
			abilityData = battle.battleState.getCharacterAt(agent)
				.flatMap(c -> Stream.concat(Stream.of(c.basicAbility), c.abilities.stream())
					.filter(a -> a.info.name.equals(ability)).findFirst())
				.flatMap(a -> a.getNext(
					battle.battleState.hasMana(agent), subsequentLevel, recursionLevel))
				.orElseThrow(() -> new CommandException("Invalid ability command"));
		}

		List<Targetable> r = new ArrayList<>();

		if (abilityData.info.trap && agentType == AbilityAgentType.CHARACTER) {
			return battle.battleState.getCharacterAt(agent)
				.map(c -> battle.createTrap(abilityData, c, targetSquares))
				.orElseThrow(() -> new CommandException("Invalid ability command"));

		} else if (abilityData.info.zoneTurns > 0 && agentType == AbilityAgentType.CHARACTER) {
			return battle.battleState.getCharacterAt(agent)
				.map(c -> battle.createZone(abilityData, c, targetSquares))
				.orElseThrow(() -> new CommandException("Invalid ability command"));

		} else {
			battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
			battle.battleState.getTrapAt(agent).ifPresent(t -> r.add(t));
			for (DamageToTarget d : targets) {
				battle.battleState.getTargetableAt(d.target).forEach(t -> r.add(t));
			}

			battle.doAbility(agent, agentType, abilityData, targets);
		}

		return r;
	}

	/**
	 * Called when the targets are moved by an instant effect
	 * */
	public void retarget(Map<MapPoint, MapPoint> retarget) {
		Collection<DamageToTarget> newDTT = new ArrayList<>();
		for (MapPoint x : retarget.keySet()) {
			if (agent.equals(x)) {
				this.agent = retarget.get(x);
			} else {
				this.targets.stream()
					.filter(t -> t.target.equals(x)).findFirst()
					.ifPresent(t -> newDTT.add(t.retarget(retarget.get(x))));
			}
		}
		this.targets = newDTT;
	}
}

