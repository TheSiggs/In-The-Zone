package inthezone.battle.status;

import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

public class StatusEffectFactory {
	public static StatusEffect getEffect(
		StatusEffectInfo info, int initialDamage, Optional<Character> agent
	) {
		switch (info.type) {
			case RESISTANT: return new BasicStatusEffect(info, 0.0, 0.20, 0.0);
			case VULNERABLE: return new BasicStatusEffect(info, 0.0, 0.20, 0.0);
			case STRENGTHENED: return new BasicStatusEffect(info, 0.20, 0.0, 0.0);
			case WEAKENED: return new BasicStatusEffect(info, 0.20, 0.0, 0.0);
			case PRECISE: return new BasicStatusEffect(info, 0.0, 0.0, 0.30);
			case ACCELERATED: return new PointStatusEffect(info, 0, 1);
			case ENERGIZED: return new PointStatusEffect(info, 1, 0);
			case DAZED: return new PointStatusEffect(info, -1, 0);
			case SLOWED: return new PointStatusEffect(info, 0, -1);
			case ONGOING: return new HPStatusEffect(info, -(Math.abs(initialDamage) / 2));
			case REGENERATION: return new HPStatusEffect(info, Math.abs(initialDamage) / 2);
			case DEBILITATED: return new Debilitated(info);
			case SILENCED: return new Silenced(info);
			case STUNNED: return new Stunned(info);
			case IMPRISONED: return new Imprisoned(info);
			case FEARED:
				Character c = agent.orElseThrow(() -> new RuntimeException(
					"Attempted to create feared status without a character agent"));
				return new FearedStatusEffect(info, c);
			case PANICKED: return new PanickedStatusEffect(info);
			case VAMPIRISM: return new Vampirism(info);
			case COVER: return new Cover(info);
			default: throw new RuntimeException("This cannot happen");
		}
	}

	public static StatusEffect fromJSON(JSONObject json)
		throws ProtocolException
	{
		try {
			final StatusEffectInfo info = new StatusEffectInfo(json.getString("info"));

			switch (info.type) {
				case ONGOING:
				case REGENERATION: return HPStatusEffect.fromJSON(json);
				case FEARED: return FearedStatusEffect.fromJSON(json);
				default: return getEffect(info, 0, null);
			}
		} catch (CorruptDataException|JSONException e) {
			throw new ProtocolException("Error parsing status effect");
		}
	}
}

