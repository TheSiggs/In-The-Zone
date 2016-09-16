package inthezone.battle.status;

import inthezone.battle.Character;
import inthezone.battle.data.StatusEffectInfo;

public class StatusEffectFactory {
	public static StatusEffect getEffect(
		StatusEffectInfo info, int initialDamage, Character agent
	) {
		switch (info.type) {
			case RESISTANT: return new BasicStatusEffect(0.0, 0.20, 0.0);
			case VULNERABLE: return new BasicStatusEffect(0.0, 0.20, 0.0);
			case STRENGTHENED: return new BasicStatusEffect(0.20, 0.0, 0.0);
			case WEAKENED: return new BasicStatusEffect(0.20, 0.0, 0.0);
			case PRECISE: return new BasicStatusEffect(0.0, 0.0, 0.30);
			case ACCELERATED: return new PointStatusEffect(0, 1, 0);
			case ENERGIZED: return new PointStatusEffect(1, 0, 0);
			case DAZED: return new PointStatusEffect(-1, 0, 0);
			case SLOWED: return new PointStatusEffect(0, -1, 0);
			case ONGOING: return new PointStatusEffect(0, 0, -(Math.abs(initialDamage) / 2));
			case REGENERATION: return new PointStatusEffect(0, 0, Math.abs(initialDamage) / 2);
			case DEBILITATED: return new Debilitated();
			case SILENCED: return new Silenced();
			case STUNNED: return new Stunned();
			case IMPRISONED: return new Imprisoned();
			case FEARED: return new FearedStatusEffect(agent, info.param);
			case PANICKED: return new PanickedStatusEffect();
			case VAMPIRISM: throw new RuntimeException("Vampirism not implemented yet");
			default: throw new RuntimeException("This cannot happen");
		}
	}
}

