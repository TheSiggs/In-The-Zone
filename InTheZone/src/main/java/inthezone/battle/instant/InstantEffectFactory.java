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
			case CLEANSE: /* fallthrough */
			case DEFUSE: /* fallthrough */
			case PURGE: return SimpleInstantEffect.getEffect(targets, info.type);
			case PUSH: /* fallthrough */
			case PULL: return PullPush.getEffect(battleState, info, castFrom, targets);
			case TELEPORT: return Teleport.getEffect(battleState, info, targets);
			case OBSTACLES: return Obstacles.getEffect(battleState, info, targets);
			default: throw new RuntimeException("Unimplemented effect " + info.type);
		}
	}

	public static InstantEffect fromJSON(JSONObject o) throws ProtocolException {
		Object okind = o.get("kind");
		if (okind == null) throw new ProtocolException("Missing effect kind");

		try {
			InstantEffectType kind = InstantEffectType.fromString((String) okind);
			switch (kind) {
				case CLEANSE: /* fallthrough */
				case DEFUSE: /* fallthrough */
				case PURGE: return SimpleInstantEffect.fromJSON(o);
				case PUSH: /* fallthrough */
				case PULL: return PullPush.fromJSON(o);
				case TELEPORT: return Teleport.fromJSON(o);
				case OBSTACLES: return Obstacles.fromJSON(o);
				default: throw new ProtocolException("Unimplemented effect " + kind);
			}
		} catch (ClassCastException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing effect", e);
		}
	}
}

