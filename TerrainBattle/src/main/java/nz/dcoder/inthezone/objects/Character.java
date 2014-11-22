/**
 * @author jedcalkin
 */
public class Character {
    public int AP;
    public int HP;
    public int Lvl;
    public int Str;
    public int Vit;
    public int Int;
    public Skill[] skills;
    public Weapon weapon;
    public Armour armour;
    
    public Character(){
        this.weapon = null;
        this.armour = null;
        this.skills = new Skill[20];
        this.Lvl = 1;
        this.Str = 1;
        this.Vit = 1;
        this.Int = 1;
        this.AP = 10;
        this.HP = 7;
        
        
        /* // basic caracter
          Character zan = new Character();
          zan.weapon = new Weapon(1); // damage 1
          zan.skills[0] = new Skill("Physical",1,1); // type(Physical,magic), damage, range
          zan.armour = new Armour(0.1,0); // Physical 10%, magic 0%
          zan.max_hp(); // set HP
        */
    
    }
    
    public void max_hp(){
      int f = 1;
      int g = 1;
      HP = Vit * HPMod(Lvl) * f + g;
    }
    
    public int HPMod(int Lvl){
      // TODO Get from table
      return Lvl;
    }
    
    public double Physical_Defense(){
      return armour.Physical;
    }
    
    public double Magical_Defense(){
      double def = 0;
      // sum of all items
      def+= armour.AntiMagic;
      return 0;
    }
}
