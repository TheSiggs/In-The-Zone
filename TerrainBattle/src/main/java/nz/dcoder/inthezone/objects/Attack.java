/**
 * @author jedcalkin
 */
public class Attack{

    public Attack(){}

    static public void attack(Character attacker, Character defender, Skill skill){
        int a = 1;
        int b = 1;
        int c = 1;
        Double attack;
        double damage;
        double stats;

        double RND = Math.random();
        double dieroll = 0.9+(0.2*RND);
        if(skill.type!="magic"){
          damage = attacker.weapon.damage - defender.Physical_Defense();
          stats  = attacker.Lvl/b + attacker.Str/a;
        }else{
          damage = (skill.damage*attacker.Lvl/c) - defender.Magical_Defense();
          stats  = attacker.Int/a;
        }
        attack = dieroll*damage*stats;

        //TODO check range

        //retrun attack;
        // OR
        defender.HP-=attack.intValue(); // apply damage to defender
    }
}
