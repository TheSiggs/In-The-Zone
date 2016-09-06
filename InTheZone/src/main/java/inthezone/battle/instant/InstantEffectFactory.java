package inthezone.battle.instant;

import inthezone.battle.BattleState;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.Collection;
import java.util.List;
import org.json.simple.JSONObject;

/**
 * Construct instant effects
 * */
public class InstantEffectFactory {
	public static InstantEffect getEffect(
		BattleState battleState,
		InstantEffectInfo info,
		MapPoint castFrom,
		Collection<MapPoint> attackArea,
		List<MapPoint> targets
	) {
		switch (info.type) {
			case CLEANSE: return Cleanse.getEffect(targets);
			case PUSH: return Push.getEffect(battleState, info, castFrom, targets);
			case PULL: return Pull.getEffect(battleState, info, castFrom, targets);
			case TELEPORT: return Teleport.getEffect(battleState, info, targets);
			case OBSTACLES: return Obstacles.getEffect(battleState, info, attackArea);
			default: throw new RuntimeException("Unimplemented effect " + info.type);
		}
	}

	public static InstantEffect fromJSON(JSONObject o) throws ProtocolException {
		Object okind = o.get("kind");
		if (okind == null) throw new ProtocolException("Missing effect kind");
		try {
			InstantEffectType kind = InstantEffectType.fromString((String) okind);
			switch (kind) {
				case CLEANSE: return Cleanse.fromJSON(o);
				case PUSH: return Push.fromJSON(o);
				case PULL: return Pull.fromJSON(o);
				case TELEPORT: return Teleport.fromJSON(o);
				case OBSTACLES: return Obstacles.fromJSON(o);
				default: throw new ProtocolException("Unimplemented effect " + kind);
			}
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing effect", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing effect", e);
		}


	}
}

