package nz.dcoder.inthezone.deprecated;

//import nz.dcoder.inthezone.objects.CharacterState;

/**
 * @author jedcalkin
 */
public class Attack {

	public static void attack(CharacterGraphics attacker, CharacterGraphics defender, Skill skill) {
		int a = 1;
		int b = 1;
		int c = 1;
		Double attack;
		double damage = 1.0;
		double stats = 1.0;                
                CharacterState attackerState = attacker.getState();
                CharacterState defenderState = defender.getState();

		double rnd = Math.random();
		double dieroll = 0.9 + (0.2 * rnd);

		if (skill.type.equals("magic")) {
			//damage = attacker.weapon.damage - defender.Physical_Defense();
			stats = attackerState.getLevel() / b + attackerState.getStrength() / a;
		} else {
			damage = (skill.damage * attackerState.getLevel() / c) - defenderState.magicalDefence();
			//stats = attacker.Int / a;
		}
		attack = dieroll * damage * stats;

		// check range
                int attackerX = attacker.getX();
                int attackerY = attacker.getY();
                int defenderX = defender.getX();
                int defenderY = defender.getY();
                int rangeNeeded = Math.abs(attackerX-defenderX)+Math.abs(attackerY-defenderY);
                
                if(rangeNeeded<skill.getRange()){
                    //retrun attack;
                    // OR
                    defenderState.setHp(defenderState.getHp() - 11.0);//attack.intValue()); // apply damage to defender
                    defender.setState(defenderState);
                }

	}
}
