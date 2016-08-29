package inthezone.battle.commands;

import java.util.Collection;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class UseAbilityCommand extends Command {
	private MapPoint agent;
	private final MapPoint castFrom;
	private final String ability;
	private Collection<DamageToTarget> targets;
	private final int subsequentLevel;
	private final int recursionLevel;

	public Collection<DamageToTarget> getTargets() {return targets;}

	public UseAbilityCommand(
		MapPoint agent, MapPoint castFrom, String ability,
		Collection<DamageToTarget> targets,
		int subsequentLevel, int recursionLevel
	) {
		this.agent = agent;
		this.castFrom = castFrom;
		this.ability = ability;
		this.targets = targets;
		this.subsequentLevel = subsequentLevel;
		this.recursionLevel = recursionLevel;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		JSONArray a = new JSONArray();
		r.put("kind", CommandKind.ABILITY.toString());
		r.put("agent", agent.getJSON());
		r.put("castFrom", castFrom.getJSON());
		r.put("ability", ability);
		r.put("subsequentLevel", subsequentLevel);
		r.put("recursionLevel", recursionLevel);
		for (DamageToTarget d : targets) a.add(d.getJSON());
		r.put("targets", a);
		return r;
	}

	public static UseAbilityCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object oagent = json.get("agent");
		Object ocastFrom = json.get("castFrom");
		Object oability = json.get("ability");
		Object otargets = json.get("targets");
		Object osubsequentLevel = json.get("subsequentLevel");
		Object orecursionLevel = json.get("recursionLevel");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (oagent == null) throw new ProtocolException("Missing ability agent");
		if (ocastFrom == null) throw new ProtocolException("Missing tile to cast ability from");
		if (oability == null) throw new ProtocolException("Missing ability");
		if (otargets == null) throw new ProtocolException("Missing ability targets");
		if (osubsequentLevel == null) throw new ProtocolException("Missing ability subsequent level");
		if (orecursionLevel == null) throw new ProtocolException("Missing ability recursion level");

		if (CommandKind.fromString((String) okind) != CommandKind.ABILITY)
			throw new ProtocolException("Expected ability command");

		try {
			MapPoint agent = MapPoint.fromJSON((JSONObject) oagent);
			MapPoint castFrom = MapPoint.fromJSON((JSONObject) ocastFrom);
			String ability = (String) oability;
			Number subsequentLevel = (Number) osubsequentLevel;
			Number recursionLevel = (Number) orecursionLevel;
			JSONArray rawTargets = (JSONArray) otargets;
			Collection<DamageToTarget> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.size(); i++) {
				targets.add(DamageToTarget.fromJSON((JSONObject) rawTargets.get(i)));
			}
			return new UseAbilityCommand(
				agent, castFrom, ability, targets,
				subsequentLevel.intValue(), recursionLevel.intValue());
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing ability command", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing ability command", e);
		}
	}

	@Override
	public List<Character> doCmd(Battle battle) throws CommandException {
		Ability abilityData = battle.battleState.getCharacterAt(agent)
			.flatMap(c -> c.abilities.stream()
				.filter(a -> a.info.name.equals(ability)).findFirst())
			.flatMap(a -> a.getNext(
				battle.battleState.hasMana(agent), subsequentLevel, recursionLevel))
			.orElseThrow(() -> new CommandException("Invalid ability command"));

		Collection<Character> r = new ArrayList<>();
		battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
		for (DamageToTarget d : targets)
			battle.battleState.getCharacterAt(d.target).ifPresent(c -> r.add(c));

		battle.doAbility(agent, abilityData, targets);

		return r.stream().map(c -> c.clone()).collect(Collectors.toList());
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

