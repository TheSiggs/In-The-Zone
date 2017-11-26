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
import org.json.JSONException;
import org.json.JSONObject;

public class PushCommand extends Command {
	private final MapPoint agent;
	public final PullPush effect;
	private final boolean effective; // determines if the push is effective

	public PushCommand(
		final MapPoint agent, final PullPush effect, final boolean effective
	) {
		this.agent = agent;
		this.effect = effect;
		this.effective = effective;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("kind", CommandKind.PUSH.toString());
		r.put("agent", agent.getJSON());
		r.put("effect", effect.getJSON());
		r.put("effective", effective);
		return r;
	}

	public static PushCommand fromJSON(final JSONObject json)
		throws ProtocolException
	{
		try {
			final CommandKind kind = CommandKind.fromString(json.getString("kind"));
			final MapPoint agent = MapPoint.fromJSON(json.getJSONObject("agent"));
			final PullPush effect = PullPush.fromJSON(json.getJSONObject("effect"));
			final boolean effective = json.getBoolean("effective");

			if (kind != CommandKind.PUSH)
				throw new ProtocolException("Expected push command");

			return new PushCommand(agent, effect, effective);

		} catch (JSONException|CorruptDataException e) {
			throw new ProtocolException("Error parsing push command", e);
		}
	}

	@Override
	public List<Targetable> doCmd(final Battle battle) throws CommandException {
		List<Targetable> r = new ArrayList<>();
		if (effective) {
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

