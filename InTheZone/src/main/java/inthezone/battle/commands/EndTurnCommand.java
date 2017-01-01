package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.data.Player;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

public class EndTurnCommand extends Command {
	public final Player player;

	public EndTurnCommand(Player player) {
		this.player = player;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("kind", CommandKind.ENDTURN.toString());
		r.put("player", player.toString());
		return r;
	}

	public static EndTurnCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object oplayer = json.get("player");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (oplayer == null) throw new ProtocolException("Missing resigning player");

		if (CommandKind.fromString((String) okind) != CommandKind.ENDTURN)
			throw new ProtocolException("Expected end turn command");

		try {
			return new EndTurnCommand(Player.fromString((String) oplayer));
		} catch (ClassCastException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing end turn command", e);
		}
	}

	@Override
	public List<? extends Targetable> doCmd(Battle turn) {
		return turn.battleState.cloneCharacters();
	}
}

