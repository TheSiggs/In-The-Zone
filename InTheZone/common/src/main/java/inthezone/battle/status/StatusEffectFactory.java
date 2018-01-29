package inthezone.battle.status;

import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import ssjsjs.JSONdecodeException;
import ssjsjs.SSJSJS;

public class StatusEffectFactory {
	public static StatusEffect getEffect(
		final StatusEffectInfo info,
		final int startTurn,
		final int initialDamage,
		final Optional<Character> agent
	) {
		switch (info.type) {
			case RESISTANT: return new BasicStatusEffect(info, startTurn, 0.0, 0.20, 0.0);
			case VULNERABLE: return new BasicStatusEffect(info, startTurn, 0.0, 0.20, 0.0);
			case STRENGTHENED: return new BasicStatusEffect(info, startTurn, 0.20, 0.0, 0.0);
			case WEAKENED: return new BasicStatusEffect(info, startTurn, 0.20, 0.0, 0.0);
			case PRECISE: return new BasicStatusEffect(info, startTurn, 0.0, 0.0, 0.30);
			case ACCELERATED: return new PointStatusEffect(info, startTurn, 0, 1);
			case ENERGIZED: return new PointStatusEffect(info, startTurn, 1, 0);
			case DAZED: return new PointStatusEffect(info, startTurn, -1, 0);
			case SLOWED: return new PointStatusEffect(info, startTurn, 0, -1);
			case ONGOING: return new HPStatusEffect(info, -(Math.abs(initialDamage) / 2), startTurn);
			case REGENERATION: return new HPStatusEffect(info, Math.abs(initialDamage) / 2, startTurn);
			case DEBILITATED: return new Debilitated(info, startTurn);
			case SILENCED: return new Silenced(info, startTurn);
			case STUNNED: return new Stunned(info, startTurn);
			case IMPRISONED: return new Imprisoned(info, startTurn);
			case FEARED:
				Character c = agent.orElseThrow(() -> new RuntimeException(
					"Attempted to create feared status without a character agent"));
				return new FearedStatusEffect(info, startTurn, c);
			case PANICKED: return new PanickedStatusEffect(info, startTurn);
			case VAMPIRISM: return new Vampirism(info, startTurn);
			case COVER: return new Cover(info, startTurn);
			default: throw new RuntimeException("This cannot happen");
		}
	}

	public static StatusEffect fromJSON(final JSONObject json)
		throws ProtocolException
	{
		try {
			final StatusEffectInfo info = new StatusEffectInfo(json.getString("info"));

			switch (info.type) {
				case ONGOING:
				case REGENERATION: return SSJSJS.decode(json, HPStatusEffect.class);
				case FEARED: return SSJSJS.decode(json, FearedStatusEffect.class);
				default:
					final int startTurn = json.getInt("startTurn");
					return getEffect(info, startTurn, 0, null);
			}
		} catch (final CorruptDataException|JSONException|JSONdecodeException e) {
			throw new ProtocolException("Error parsing status effect " +
				json.toString());
		}
	}
}

