package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.instant.InstantEffect;
import inthezone.battle.instant.InstantEffectFactory;
import inthezone.protocol.ProtocolException;
import java.util.List;
import org.json.simple.JSONObject;

public class InstantEffectCommand extends Command {
	public final InstantEffect effect;

	public InstantEffectCommand(InstantEffect effect) {
		this.effect = effect;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("kind", CommandKind.INSTANT.toString());
		r.put("effect", effect.getJSON());
		return r;
	}

	public static InstantEffectCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object oeffect = json.get("effect");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (oeffect == null) throw new ProtocolException("Missing effect");

		if (CommandKind.fromString((String) okind) != CommandKind.INSTANT)
			throw new ProtocolException("Expected effect command");

		try {
			return new InstantEffectCommand(
				InstantEffectFactory.fromJSON((JSONObject) oeffect));
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing effect command", e);
		}
	}

	@Override
	public List<Character> doCmd(Battle battle) throws CommandException {
		return effect.apply(battle);
	}
}


