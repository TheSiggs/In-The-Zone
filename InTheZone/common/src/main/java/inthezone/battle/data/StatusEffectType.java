package inthezone.battle.data;

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

	public boolean causesMovement() {
		return this == PANICKED || this == FEARED;
	}

	public String toNiceString() {
		if (this == ONGOING) return "ongoing damage"; else return this.toString().toLowerCase();
	}
}

