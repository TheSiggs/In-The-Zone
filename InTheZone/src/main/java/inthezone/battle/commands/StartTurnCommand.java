package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.data.Player;
import inthezone.battle.Targetable;
import java.util.List;
import org.json.JSONObject;

/**
 * A dummy command not intended to be relayed between players.
 * */
public class StartTurnCommand extends Command {
	public final Player player;

	private final List<Targetable> affected;

	public StartTurnCommand( Player player, List<Targetable> affected) {
		this.affected = affected;
		this.player = player;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("kind", CommandKind.STARTTURN.toString());
		return r;
	}

	@Override
	public List<Targetable> doCmd(Battle turn) {
		return affected;
	}
}

