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
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class UseAbilityCommand extends Command {
	private final MapPoint agent;
	private final String ability;
	private final Collection<DamageToTarget> targets;

	public UseAbilityCommand(
		MapPoint agent, String ability, Collection<DamageToTarget> targets
	) {
		this.agent = agent;
		this.ability = ability;
		this.targets = targets;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		JSONArray a = new JSONArray();
		r.put("kind", CommandKind.ABILITY);
		r.put("agent", agent);
		r.put("ability", ability);
		for (DamageToTarget d : targets) a.add(d.getJSON());
		r.put("targets", a);
		return r;
	}

	public static UseAbilityCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object oagent = json.get("agent");
		Object oability = json.get("ability");
		Object otargets = json.get("targets");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (oagent == null) throw new ProtocolException("Missing ability agent");
		if (oability == null) throw new ProtocolException("Missing ability");
		if (otargets == null) throw new ProtocolException("Missing ability targets");

		if (CommandKind.fromString((String) okind) != CommandKind.ABILITY)
			throw new ProtocolException("Expected ability command");

		try {
			MapPoint agent = MapPoint.fromJSON((JSONObject) oagent);
			String ability = (String) oability;
			JSONArray rawTargets = (JSONArray) otargets;
			Collection<DamageToTarget> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.size(); i++) {
				targets.add(DamageToTarget.fromJSON((JSONObject) rawTargets.get(i)));
			}
			return new UseAbilityCommand(agent, ability, targets);
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
				.orElseThrow(() -> new CommandException("Invalid ability command"));

		if (!battle.battleState.canDoAbility(agent, abilityData, targets))
			throw new CommandException("Invalid ability command");

		Collection<Character> r = new ArrayList<>();
		battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
		for (DamageToTarget d : targets)
			battle.battleState.getCharacterAt(d.target).ifPresent(c -> r.add(c));

		battle.doAbility(agent, abilityData, targets);

		return r.stream().map(c -> c.clone()).collect(Collectors.toList());
	}
}

