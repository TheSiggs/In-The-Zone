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

public class ResignCommand extends Command {
	public final Player player;
	public final boolean logoff;

	public ResignCommand(Player player) {
		this.player = player;
		this.logoff = false;
	}

	public ResignCommand(Player player, boolean logoff) {
		this.player = player;
		this.logoff = logoff;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("kind", CommandKind.RESIGN.toString());
		r.put("player", player.toString());
		r.put("logoff", logoff);
		return r;
	}

	public static ResignCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		try {
			final CommandKind kind = CommandKind.fromString(json.getString("kind"));
			final Player player = Player.fromString(json.getString("player"));
			final boolean logoff = json.getBoolean("logoff");

			if (kind != CommandKind.RESIGN)
				throw new ProtocolException("Expected resign command");

			return new ResignCommand(player, logoff);

		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing resign command", e);
		}
	}

	@Override
	public List<Targetable> doCmd(Battle turn) {
		turn.doResign(player, logoff);
		return new ArrayList<>();
	}
}

