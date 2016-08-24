package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import java.util.ArrayList;
import java.util.List;
import inthezone.protocol.ProtocolException;
import org.json.simple.JSONObject;

public class EndTurnCommand extends Command {
	public EndTurnCommand() {
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("kind", CommandKind.ENDTURN.toString());
		return r;
	}

	public static EndTurnCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");

		if (okind == null) throw new ProtocolException("Missing command type");

		if (CommandKind.fromString((String) okind) != CommandKind.ENDTURN)
			throw new ProtocolException("Expected end turn command");

		return new EndTurnCommand();
	}

	@Override
	public List<Character> doCmd(Battle turn) {
		return new ArrayList<>();
	}
}

