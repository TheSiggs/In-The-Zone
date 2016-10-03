package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.InstantEffectCommand;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.instant.Push;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.json.simple.JSONObject;

public class FearedStatusEffect extends StatusEffect {
	private final Character agent;
	private final int g;

	public FearedStatusEffect(StatusEffectInfo info, Character agent) {
		super(info);
		this.agent = agent;
		g = info.param;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("info", info.toString());
		r.put("agent", agent.getPos().getJSON());
		return r;
	}

	public static FearedStatusEffect fromJSON(JSONObject o, BattleState battle)
		throws ProtocolException
	{
		Object oagent = o.get("agent");
		Object oinfo = o.get("info");

		if (oinfo == null) throw new ProtocolException("Missing status effect type");
		if (oagent == null) throw new ProtocolException("Missing agent");

		try {
			StatusEffectInfo info = new StatusEffectInfo((String) oinfo);
	
			Character agent =
				battle.getCharacterAt(
					MapPoint.fromJSON((JSONObject) oagent))
				.orElseThrow(() ->
					new ProtocolException("Cannot find feared agent"));

			return new FearedStatusEffect(info, agent);
		} catch (ClassCastException|CorruptDataException e) {
			throw new ProtocolException("Error parsing feared status effect", e);
		}
	}

	public List<Command> doBeforeTurn(Battle battle, Character c) {
		Collection<MapPoint> targets = new ArrayList<>();
		targets.add(c.getPos());

		Push p = Push.getEffect(
			battle.battleState,
			new InstantEffectInfo(InstantEffectType.PUSH, g),
			agent.getPos(), targets);

		List<Command> r = new ArrayList<>();
		r.add(new InstantEffectCommand(p, Optional.empty(), Optional.empty()));
		return r;
	}
}

