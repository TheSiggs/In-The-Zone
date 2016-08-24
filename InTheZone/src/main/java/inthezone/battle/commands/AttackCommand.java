package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.DamageToTarget;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class AttackCommand extends Command {
	private final MapPoint agent;
	private final Collection<DamageToTarget> targets;

	public AttackCommand(
		MapPoint agent, Collection<DamageToTarget> targets
	) {
		this.agent = agent;
		this.targets = targets;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		JSONArray a = new JSONArray();
		r.put("kind", CommandKind.ATTACK.toString());
		r.put("agent", agent.getJSON());
		for (DamageToTarget d : targets) a.add(d.getJSON());
		r.put("targets", a);
		return r;
	}

	public static AttackCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object oagent = json.get("agent");
		Object otargets = json.get("targets");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (oagent == null) throw new ProtocolException("Missing attack agent");
		if (otargets == null) throw new ProtocolException("Missing attack targets");

		if (CommandKind.fromString((String) okind) != CommandKind.ATTACK)
			throw new ProtocolException("Expected attack command");


		try {
			MapPoint agent = MapPoint.fromJSON((JSONObject) oagent);
			JSONArray rawTargets = (JSONArray) otargets;
			Collection<DamageToTarget> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.size(); i++) {
				targets.add(DamageToTarget.fromJSON((JSONObject) rawTargets.get(i)));
			}
			return new AttackCommand(agent, targets);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing attack command", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing attack command", e);
		}
	}

	@Override
	public List<Character> doCmd(Battle battle) throws CommandException {
		if (!battle.battleState.canAttack(agent, targets))
			throw new CommandException("Invalid attack");

		List<Character> r = new ArrayList<>();
		battle.battleState.getCharacterAt(agent).ifPresent(c -> r.add(c));
		for (DamageToTarget d : targets)
			battle.battleState.getCharacterAt(d.target).ifPresent(c -> r.add(c));

		battle.doAttack(agent, targets);

		return r.stream().map(c -> c.clone()).collect(Collectors.toList());
	}
}

