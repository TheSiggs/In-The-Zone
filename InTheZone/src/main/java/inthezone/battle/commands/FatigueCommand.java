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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FatigueCommand extends Command {
	private Collection<DamageToTarget> targets;

	public FatigueCommand(Collection<DamageToTarget> targets) {
		this.targets = targets;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		final JSONArray a = new JSONArray();
		r.put("kind", CommandKind.FATIGUE.toString());
		for (DamageToTarget d : targets) a.put(d.getJSON());
		r.put("targets", a);
		return r;
	}

	public static FatigueCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		try {
			final CommandKind kind = CommandKind.fromString(json.getString("kind"));
			final JSONArray rawTargets = json.getJSONArray("targets");

			if (kind != CommandKind.FATIGUE)
				throw new ProtocolException("Expected ability command");

			final Collection<DamageToTarget> targets = new ArrayList<>();
			for (int i = 0; i < rawTargets.length(); i++) {
				targets.add(DamageToTarget.fromJSON((JSONObject) rawTargets.get(i)));
			}
			return new FatigueCommand(targets);

		} catch (ClassCastException|JSONException e) {
			throw new ProtocolException("Error parsing fatigue command", e);
		}
	}

	@Override
	public List<Targetable> doCmd(Battle battle) throws CommandException {
		List<Targetable> r = new ArrayList<>();
		for (DamageToTarget d : targets) {
			battle.battleState.getTargetableAt(d.target.target).forEach(t -> r.add(t));
		}

		battle.doFatigue(targets);

		return r;
	}
}

