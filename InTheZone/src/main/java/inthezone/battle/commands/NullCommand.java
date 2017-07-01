package inthezone.battle.commands;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import inthezone.battle.Battle;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;

public class NullCommand extends Command {
	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("kind", CommandKind.NULL.toString());
		return r;
	}

	public static NullCommand fromJSON(final JSONObject json)
		throws ProtocolException
	{
		try {
			final CommandKind kind = CommandKind.fromString(json.getString("kind"));
			if (kind != CommandKind.NULL)
				throw new ProtocolException("Expected null command");

			return new NullCommand();

		} catch (JSONException e) {
			throw new ProtocolException("Error parsing null command", e);
		}
	}

	@Override
	public List<Targetable> doCmd(final Battle battle) throws CommandException {
		return new ArrayList<>();
	}
}


