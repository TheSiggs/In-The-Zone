package inthezone.battle.instant;

import inthezone.battle.BattleState;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import ssjsjs.JSONdecodeException;
import ssjsjs.SSJSJS;

/**
 * Construct instant effects
 * */
public class InstantEffectFactory {
	public static InstantEffect getEffect(
		final String abilityName,
		final BattleState battleState,
		final InstantEffectInfo info,
		final MapPoint castFrom,
		final Set<MapPoint> attackArea,
		final Set<MapPoint> targets
	) {
		switch (info.type) {
			case CLEANSE: /* fallthrough */
			case DEFUSE: /* fallthrough */
			case PURGE:
				try {
					return SimpleInstantEffect.getEffect(targets, castFrom, info.type);
				} catch (final ProtocolException e) {
					throw new RuntimeException("Error constructing simple instant effect", e);
				}
			case PUSH: /* fallthrough */
			case PULL: return PullPush.getEffect(battleState, info, castFrom, targets, false);
			case TELEPORT: return Teleport.getEffect(battleState, info, targets, castFrom);
			case OBSTACLES: return Obstacles.getEffect(castFrom, abilityName, new HashSet<>(targets));
			case MOVE: return Move.getEffect(battleState, info, targets, castFrom);
			case REVIVE: return Revive.getEffect(targets, castFrom);
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
				case PURGE: return SSJSJS.decode(o, SimpleInstantEffect.class);
				case PUSH: /* fallthrough */
				case PULL: return SSJSJS.decode(o, PullPush.class);
				case TELEPORT: return SSJSJS.decode(o, Teleport.class);
				case OBSTACLES: return SSJSJS.decode(o, Obstacles.class);
				case MOVE: return SSJSJS.decode(o, Move.class);
				case REVIVE: return SSJSJS.decode(o, Revive.class);
				default: throw new ProtocolException("Unimplemented effect " + kind);
			}
		} catch (final JSONException|JSONdecodeException|CorruptDataException  e) {
			throw new ProtocolException("Error parsing effect", e);
		}
	}
}

