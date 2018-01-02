package inthezone.battle.data;

/**
 * A description of a status effect.
 * */
public class StatusEffectDescription {
	private final StatusEffectInfo info;

	public StatusEffectDescription(final StatusEffectInfo info) {
		this.info = info;
	}

	@Override public String toString() {
		final StringBuilder out = new StringBuilder();
		final String title = info.toNiceString();
		out.append(title.substring(0,1).toUpperCase()).append(title.substring(1));
		out.append(" (").append(info.kind.toString()).append(")\n");

		switch (info.type) {
			case ACCELERATED: out.append("This character gets plus 1 movement point."); break;
			case DAZED: out.append("This character gets minus 1 action point."); break;
			case DEBILITATED: out.append("This character cannot use skills."); break;
			case ENERGIZED: out.append("This character gets plus 1 action point."); break;
			case FEARED: out.append("This character will run away from an enemy character at the start of each turn, wasting movement points."); break;
			case IMPRISONED: out.append("This character cannot be moved in any way (including by teleporting, pushing, or pulling)."); break;
			case PRECISE: out.append("This character has a plus 30% chance to inflict status effects."); break;
			case RESISTANT: out.append("This character takes 20% less damage."); break;
			case VULNERABLE: out.append("This character takes 20% more damage."); break;
			case ONGOING: out.append("This character takes damage equal to 1/2 of the initial damage that inflicted this status effect, at the start of each turn."); break;
			case PANICKED: out.append("This character will move randomly at the start of your turn, using all his/her movement points."); break;
			case REGENERATION: out.append("This character gains health equal to 1/2 of the initial healing or damage that gave this status effect, at the start of each turn."); break;
			case SILENCED: out.append("This character cannot use spells"); break;
			case SLOWED: out.append("This character gets minus 1 movement point."); break;
			case STRENGTHENED: out.append("This character deals 20% more damage."); break;
			case STUNNED: out.append("This character cannot spend action points."); break;
			case VAMPIRISM: out.append("When this character deals damage, he/she gains health proportional to the damage dealt."); break;
			case WEAKENED: out.append("This character deals 20% less damage"); break;
			case COVER: out.append("This character takes no damage for one turn."); break;
			default: out.append("<UNKNOWN STATUS EFFECT>");
		}

		return out.toString();
	}
}

