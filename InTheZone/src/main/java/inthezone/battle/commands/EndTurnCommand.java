package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.data.Player;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class EndTurnCommand extends Command {
	public final Player player;

	public EndTurnCommand(Player player) {
		this.player = player;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("kind", CommandKind.ENDTURN.toString());
		r.put("player", player.toString());
		return r;
	}

	public static EndTurnCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		try {
			final CommandKind kind = CommandKind.fromString(json.getString("kind"));
			final String player = json.getString("player");

			if (kind != CommandKind.ENDTURN)
				throw new ProtocolException("Expected end turn command");

			return new EndTurnCommand(Player.fromString(player));
		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing end turn command", e);
		}
	}

	@Override
	public List<? extends Targetable> doCmd(Battle turn) {
		return turn.battleState.cloneCharacters();
	}
}

