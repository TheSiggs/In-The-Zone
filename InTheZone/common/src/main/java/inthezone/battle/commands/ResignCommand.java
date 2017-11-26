package inthezone.battle.commands;

import isogame.engine.CorruptDataException;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import inthezone.battle.Battle;
import inthezone.battle.Targetable;
import inthezone.battle.data.Player;
import inthezone.protocol.ProtocolException;

public class ResignCommand extends Command {
	public final Player player;
	public final ResignReason reason;

	public ResignCommand(final Player player) {
		this.player = player;
		this.reason = ResignReason.RESIGNED;
	}

	public ResignCommand(final Player player, final ResignReason reason) {
		this.player = player;
		this.reason = reason;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("kind", CommandKind.RESIGN.toString());
		r.put("player", player.toString());
		r.put("reason", reason.toString());
		return r;
	}

	public static ResignCommand fromJSON(final JSONObject json)
		throws ProtocolException
	{
		try {
			final CommandKind kind = CommandKind.fromString(json.getString("kind"));
			final Player player = Player.fromString(json.getString("player"));
			final ResignReason reason = ResignReason.valueOf(json.getString("reason"));

			if (kind != CommandKind.RESIGN)
				throw new ProtocolException("Expected resign command");

			return new ResignCommand(player, reason);

		} catch (final JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing resign command", e);
		}
	}

	@Override
	public List<Targetable> doCmd(final Battle turn) {
		turn.doResign(player, reason);
		return new ArrayList<>();
	}
}

