package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.instant.PullPush;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;

public class PushCommand extends Command {
	private final MapPoint agent;
	public final PullPush effect;
	private final boolean effective; // determines if the push is effective

	public PushCommand(MapPoint agent, PullPush effect, boolean effective) {
		this.agent = agent;
		this.effect = effect;
		this.effective = effective;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("kind", CommandKind.PUSH.toString());
		r.put("agent", agent.getJSON());
		r.put("effect", effect.getJSON());
		r.put("effective", effective);
		return r;
	}

	public static PushCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object oagent = json.get("agent");
		Object oeffect = json.get("effect");
		Object oeffective = json.get("effective");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (oagent == null) throw new ProtocolException("Missing push agent");
		if (oeffect == null) throw new ProtocolException("Missing push effect");
		if (oeffective == null) throw new ProtocolException("Missing push effective");

		if (CommandKind.fromString((String) okind) != CommandKind.PUSH)
			throw new ProtocolException("Expected push command");

		try {
			MapPoint agent = MapPoint.fromJSON((JSONObject) oagent);
			PullPush effect = PullPush.fromJSON((JSONObject) oeffect);
			boolean effective = (Boolean) oeffective;
			return new PushCommand(agent, effect, effective);
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing push command", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing push command", e);
		}
	}

	@Override
	public List<Targetable> doCmd(Battle battle) throws CommandException {
		List<Targetable> r = new ArrayList<>();
		if (effective) {
			System.err.println("Do push command effective");
			Character user = battle.battleState.getCharacterAt(agent)
				.orElseThrow(() -> new CommandException("40: Cannot find push agent"));

			user.usePush();
			// by convention, we always put the agent first in the affected characters list.
			r.add(user);
			r.addAll(effect.apply(battle));
			return r;
		} else {
			return r;
		}
	}
}

