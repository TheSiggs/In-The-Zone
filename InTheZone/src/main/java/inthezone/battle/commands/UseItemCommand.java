package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.HealthPotion;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

public class UseItemCommand extends Command {
	private final MapPoint agent;
	private final MapPoint target;

	public UseItemCommand(MapPoint agent, MapPoint target) {
		this.agent = agent;
		this.target = target;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("agent", agent.getJSON());
		r.put("target", target.getJSON());
		return r;
	}

	public static UseItemCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object ragent = json.get("agent");
		Object rtarget = json.get("target");

		if (ragent == null) throw new ProtocolException("Missing agent in item command");
		if (rtarget == null) throw new ProtocolException("Missing target in item command");

		try {
			MapPoint agent = MapPoint.fromJSON((JSONObject) ragent);
			MapPoint target = MapPoint.fromJSON((JSONObject) rtarget);
			return new UseItemCommand(agent, target);
		} catch (ClassCastException|CorruptDataException e) {
			throw new ProtocolException("Error parsing item command");
		}
	}

	@Override
	public List<Targetable> doCmd(Battle battle) throws CommandException {
		// there is only one item for now, so just create a health potion
		battle.doUseItem(agent, target, new HealthPotion());

		List<Targetable> r = new ArrayList<>();
		battle.battleState.getCharacterAt(agent).ifPresent(x -> r.add(x));
		battle.battleState.getCharacterAt(target).ifPresent(x -> r.add(x));
		return r;
	}
}

