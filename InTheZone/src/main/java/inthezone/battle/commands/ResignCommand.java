package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.data.Player;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import java.util.List;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;

public class ResignCommand extends Command {
	public final Player player;

	public ResignCommand(Player player) {
		this.player = player;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("kind", CommandKind.RESIGN.toString());
		r.put("player", player.toString());
		return r;
	}

	public static ResignCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object oplayer = json.get("player");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (oplayer == null) throw new ProtocolException("Missing resigning player");

		if (CommandKind.fromString((String) okind) != CommandKind.RESIGN)
			throw new ProtocolException("Expected resign command");

		try {
			return new ResignCommand(Player.fromString((String) oplayer));
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing resign command", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing resign command", e);
		}
	}

	@Override
	public List<Character> doCmd(Battle turn) {
		turn.doResign(player);
		return turn.battleState.characters.stream()
			.filter(c -> c.player == player).collect(Collectors.toList());
	}
}


