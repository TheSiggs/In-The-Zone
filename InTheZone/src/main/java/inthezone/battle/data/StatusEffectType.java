package inthezone.battle.data;

import isogame.engine.CorruptDataException;

public enum StatusEffectType {
	ACCELERATED, DAZED, DEBILITATED, ENERGIZED, FEARED,
	IMPRISONED, PRECISE, RESISTANT, VULNERABLE,
	ONGOING, PANICKED, REGENERATION, SILENCED, SLOWED, STRENGTHENED,
	STUNNED, VAMPIRISM, WEAKENED, COVER;

	public String getIconName() {
		switch(this) {
			case ACCELERATED: return "status/accelerated.png";
			case DAZED: return "status/dazed.png";
			case DEBILITATED: return "status/debilitated.png";
			case ENERGIZED: return "status/energized.png";
			case FEARED: return "status/feared.png";
			case IMPRISONED: return "status/imprisoned.png";
			case PRECISE: return "status/precise.png";
			case RESISTANT: return "status/resistant.png";
			case VULNERABLE: return "status/vulnerable.png";
			case ONGOING: return "status/ongoing.png";
			case PANICKED: return "status/panicked.png";
			case REGENERATION: return "status/regeneration.png";
			case SILENCED: return "status/silenced.png";
			case SLOWED: return "status/slowed.png";
			case STRENGTHENED: return "status/strengthened.png";
			case STUNNED: return "status/stunned.png";
			case VAMPIRISM: return "status/vampirism.png";
			case WEAKENED: return "status/weakened.png";
			case COVER: return "status/cover.png";
			default:
				throw new RuntimeException("Invalid status effect type, this cannot happen");
		}
	}

	public StatusEffectKind getEffectKind() {
		switch(this) {
			case ACCELERATED: return StatusEffectKind.BUFF;
			case DAZED: return StatusEffectKind.DEBUFF;
			case DEBILITATED: return StatusEffectKind.DEBUFF;
			case ENERGIZED: return StatusEffectKind.BUFF;
			case FEARED: return StatusEffectKind.DEBUFF;
			case IMPRISONED: return StatusEffectKind.DEBUFF;
			case PRECISE: return StatusEffectKind.BUFF;
			case RESISTANT: return StatusEffectKind.BUFF;
			case VULNERABLE: return StatusEffectKind.DEBUFF;
			case ONGOING: return StatusEffectKind.DEBUFF;
			case PANICKED: return StatusEffectKind.DEBUFF;
			case REGENERATION: return StatusEffectKind.BUFF;
			case SILENCED: return StatusEffectKind.DEBUFF;
			case SLOWED: return StatusEffectKind.DEBUFF;
			case STRENGTHENED: return StatusEffectKind.BUFF;
			case STUNNED: return StatusEffectKind.DEBUFF;
			case VAMPIRISM: return StatusEffectKind.BUFF;
			case WEAKENED: return StatusEffectKind.DEBUFF;
			case COVER: return StatusEffectKind.BUFF;
			default:
				throw new RuntimeException("Invalid status effect type, this cannot happen");
		}
	}

	public static StatusEffectType parse(String s)
		throws CorruptDataException
	{
		switch(s.toLowerCase()) {
			case "accelerated": return ACCELERATED;
			case "dazed": return DAZED;
			case "debilitated": return DEBILITATED;
			case "energized": return ENERGIZED;
			case "feared": return FEARED;
			case "imprisoned": return IMPRISONED;
			case "precise": return PRECISE;
			case "resistant": return RESISTANT;
			case "vulnerable": return VULNERABLE;
			case "ongoing": return ONGOING;
			case "panicked": return PANICKED;
			case "regeneration": return REGENERATION;
			case "silenced": return SILENCED;
			case "slowed": return SLOWED;
			case "strengthened": return STRENGTHENED;
			case "stunned": return STUNNED;
			case "vampirism": return VAMPIRISM;
			case "weakened": return WEAKENED;
			case "cover": return COVER;
			default:
				throw new CorruptDataException("Invalid status effect " + s);
		}
	}

	@Override public String toString() {
		switch (this) {
			case ACCELERATED: return "accelerated";
			case DAZED: return "dazed";
			case DEBILITATED: return "debilitated";
			case ENERGIZED: return "energized";
			case FEARED: return "feared";
			case IMPRISONED: return "imprisoned";
			case PRECISE: return "precise";
			case RESISTANT: return "resistant";
			case VULNERABLE: return "vulnerable";
			case ONGOING: return "ongoing";
			case PANICKED: return "panicked";
			case REGENERATION: return "regeneration";
			case SILENCED: return "silenced";
			case SLOWED: return "slowed";
			case STRENGTHENED: return "strengthened";
			case STUNNED: return "stunned";
			case VAMPIRISM: return "vampirism";
			case WEAKENED: return "weakened";
			case COVER: return "cover";
			default:
				throw new RuntimeException("This cannot happen");
		}
	}
}

