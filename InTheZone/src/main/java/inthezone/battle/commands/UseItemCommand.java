package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.Item;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.simple.JSONObject;

public class UseItemCommand extends Command {
	private final MapPoint agent;
	private final Item item;

	public UseItemCommand(MapPoint agent, Item item) {
		this.agent = agent;
		this.item = item;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		// TODO: implement this
		return new JSONObject();
	}

	public static UseItemCommand fromJSON(JSONObject json) {
		// TODO: implement this
		return null;
	}

	public static UseItemCommand fromJSON() throws ProtocolException {
		throw new ProtocolException("Item command not implemented yet");
	}

	@Override
	public List<Targetable> doCmd(Battle battle) throws CommandException {
		Optional<Character> oc = battle.battleState.getCharacterAt(agent);

		battle.doUseItem(agent, item);

		List<Targetable> r = new ArrayList<>();
		oc.ifPresent(c -> r.add(c));
		return r;
	}
}

