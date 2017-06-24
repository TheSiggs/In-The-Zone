package inthezone.battle.instant;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;

import org.json.JSONException;
import org.json.JSONObject;

import inthezone.battle.BattleState;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.protocol.ProtocolException;

/**
 * Construct instant effects
 * */
public class InstantEffectFactory {
	public static InstantEffect getEffect(
		final String abilityName,
		final BattleState battleState,
		final InstantEffectInfo info,
		final MapPoint castFrom,
		final Collection<MapPoint> attackArea,
		final List<MapPoint> targets
	) {
		switch (info.type) {
			case CLEANSE: /* fallthrough */
			case DEFUSE: /* fallthrough */
			case PURGE: return SimpleInstantEffect.getEffect(targets, castFrom, info.type);
			case PUSH: /* fallthrough */
			case PULL: return PullPush.getEffect(battleState, info, castFrom, targets, false);
			case TELEPORT: return Teleport.getEffect(battleState, info, targets, castFrom);
			case OBSTACLES: return Obstacles.getEffect(castFrom, abilityName, new HashSet<>(targets));
			case MOVE: return Move.getEffect(battleState, info, targets, castFrom);
			default: throw new RuntimeException("Unimplemented effect " + info.type);
		}
	}

	public static InstantEffect fromJSON(final JSONObject o)
		throws ProtocolException
	{
		try {
			final InstantEffectType kind =
				(new InstantEffectInfo(o.getString("kind"))).type;

			switch (kind) {
				case CLEANSE: /* fallthrough */
				case DEFUSE: /* fallthrough */
				case PURGE: return SimpleInstantEffect.fromJSON(o);
				case PUSH: /* fallthrough */
				case PULL: return PullPush.fromJSON(o);
				case TELEPORT: return Teleport.fromJSON(o);
				case OBSTACLES: return Obstacles.fromJSON(o);
				case MOVE: return Move.fromJSON(o);
				default: throw new ProtocolException("Unimplemented effect " + kind);
			}
		} catch (JSONException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing effect", e);
		}
	}
}

