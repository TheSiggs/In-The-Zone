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

public class FatigueCommand extends Command {
	private Collection<DamageToTarget> targets;

	public FatigueCommand(Collection<DamageToTarget> targets) {
		this.targets = targets;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		JSONArray a = new JSONArray();
		r.put("kind", CommandKind.FATIGUE.toString());
		for (DamageToTarget d : targets) a.add(d.getJSON());
		r.put("targets", a);
		return r;
	}

	public static FatigueCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object otargets = json.get("targets");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (otargets == null) throw new ProtocolException("Missing fatigue targets");

		if (CommandKind.fromString((String) okind) != CommandKind.FATIGUE)
			throw new ProtocolException("Expected ability command");

		try {
			JSONArray rawTargets = (JSONArray) otargets;
			Collection<DamageToTarget> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.size(); i++) {
				targets.add(DamageToTarget.fromJSON((JSONObject) rawTargets.get(i)));
			}
			return new FatigueCommand(targets);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing fatigue command", e);
		}
	}

	@Override
	public List<Targetable> doCmd(Battle battle) throws CommandException {
		List<Targetable> r = new ArrayList<>();
		for (DamageToTarget d : targets) {
			Optional<Character> oc = battle.battleState.getCharacterAt(d.target);
			if (oc.isPresent()) {
				oc.ifPresent(c -> r.add(c.clone()));
			} else {
				battle.battleState.getTargetableAt(d.target).forEach(t -> r.add(t));
			}
		}

		battle.doFatigue(targets);

		return r;
	}
}

