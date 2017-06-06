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
import org.json.JSONException;
import org.json.JSONObject;

public class UseItemCommand extends Command {
	private final MapPoint agent;
	private final MapPoint target;

	public UseItemCommand(MapPoint agent, MapPoint target) {
		this.agent = agent;
		this.target = target;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("kind", CommandKind.ITEM.toString());
		r.put("agent", agent.getJSON());
		r.put("target", target.getJSON());
		return r;
	}

	public static UseItemCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		try {
			final CommandKind kind = CommandKind.fromString(json.getString("kind"));

			if (kind != CommandKind.ITEM)
				throw new ProtocolException("Expected move command");

			final MapPoint agent = MapPoint.fromJSON(json.getJSONObject("agent"));
			final MapPoint target = MapPoint.fromJSON(json.getJSONObject("target"));

			return new UseItemCommand(agent, target);
		} catch (JSONException|CorruptDataException e) {
			throw new ProtocolException("Error parsing item command");
		}
	}

	@Override
	public List<Targetable> doCmd(Battle battle) throws CommandException {
		// there is only one item for now, so just create a health potion
		battle.doUseItem(agent, target, new HealthPotion());

		final List<Targetable> r = new ArrayList<>();
		battle.battleState.getCharacterAt(agent).ifPresent(x -> r.add(x));
		battle.battleState.getCharacterAt(target).ifPresent(x -> r.add(x));
		r.addAll(battle.battleState.characters);
		return r;
	}
}

