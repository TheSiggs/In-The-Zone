package nz.dcoder.inthezone.objects;

import nz.dcoder.inthezone.objects.CharacterState;

/**
 * @author jedcalkin
 */
public class Attack {

	public static void attack(CharacterState attacker, CharacterState defender, Skill skill) {
		int a = 1;
		int b = 1;
		int c = 1;
		Double attack;
		double damage = 0.0;
		double stats;

		double rnd = Math.random();
		double dieroll = 0.9 + (0.2 * rnd);
		if (skill.type.equals("magic")) {
			//damage = attacker.weapon.damage - defender.Physical_Defense();
			stats = attacker.getLevel() / b + attacker.getStrength() / a;
		} else {
			damage = (skill.damage * attacker.getLevel() / c) - defender.magicalDefence();
			//stats = attacker.Int / a;
		}
		//attack = dieroll * damage * stats;

		//TODO check range

		//retrun attack;
		// OR
		//defender.setHP(defender.getHP() - attack.intValue()); // apply damage to defender
	}
}
