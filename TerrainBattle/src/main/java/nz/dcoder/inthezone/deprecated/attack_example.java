package nz.dcoder.inthezone.deprecated;

/**
 * @author jedcalkin
 */
public class attack_example{
    public static void main(String[] args) {
        Attack attackObj = new Attack();
    
        // make zan
        CharacterState zan = new CharacterState();
        zan.setWeapon(new Weapon(1));
		/*
        zan.weapon = new Weapon(1); // damage 1
        zan.skills[0] = new Skill("Physical",1,1); // type(Physical,magic), damage, range
        zan.armour = new Armour(0.1,0.0); // Physical 10%, magic 0%
        zan.maxHp(); // set HP

        // make enemy
        Character enemy = new Character();
        enemy.armour = new Armour(0.1,0.0);
        enemy.maxHp();

        // attack
        attackObj.attack(zan, enemy, zan.skills[0]); // attacker, defender, attacker.skill
		*/
    }
}
