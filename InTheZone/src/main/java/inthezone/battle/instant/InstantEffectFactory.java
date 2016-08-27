package inthezone.battle.instant;

import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.Collection;
import org.json.simple.JSONObject;

/**
 * Construct instant effects
 * */
public class InstantEffectFactory {
	public static InstantEffect getEffect(
		InstantEffectInfo info,
		MapPoint castFrom,
		Collection<MapPoint> attackArea,
		Collection<MapPoint> targets
	) {
		switch (info.type) {
			case CLEANSE: return Cleanse.getEffect(targets);
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
				default: throw new ProtocolException("Unimplemented effect " + kind);
			}
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing effect", e);
		} catch (CorruptDataException e) {
			throw new ProtocolException("Error parsing effect", e);
		}


	}
}

