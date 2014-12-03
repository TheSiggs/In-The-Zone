package nz.dcoder.inthezone.objects;

/**
 * @author jedcalkin
 */
public class CharacterState {
    private int AP;
    private int HP;
    private int Lvl;
    private int Str;
    private int Vit;
    private int Int;
    private Skill[] skills;
    private Weapon weapon;
    private Armour armour;
    
    public CharacterState(){
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
          CharacterState zan = new CharacterState();
          zan.weapon = new Weapon(1); // damage 1
          zan.skills[0] = new Skill("Physical",1,1); // type(Physical,magic), damage, range
          zan.armour = new Armour(0.1,0); // Physical 10%, magic 0%
          zan.max_hp(); // set HP
        */
    
    }
    
    public void max_hp(){
      int f = 1;
      int g = 1;
      	setHP(getVit() * HPMod(getLvl()) * f + g);
    }
    
    public int HPMod(int Lvl){
      // TODO Get from table
      return Lvl;
    }
    
    public double Physical_Defense(){
      return armour.getPhysical();
    }
    
    public double Magical_Defense(){
      double def = 0;
      // sum of all items
      def+= armour.getAntiMagic();
      return 0;
    }

	/**
	 * @return the AP
	 */
	public int getAP() {
		return AP;
	}

	/**
	 * @param AP the AP to set
	 */
	public void setAP(int AP) {
		this.AP = AP;
	}

	/**
	 * @return the HP
	 */
	public int getHP() {
		return HP;
	}

	/**
	 * @param HP the HP to set
	 */
	public void setHP(int HP) {
		this.HP = HP;
	}

	/**
	 * @return the Lvl
	 */
	public int getLvl() {
		return Lvl;
	}

	/**
	 * @param Lvl the Lvl to set
	 */
	public void setLvl(int Lvl) {
		this.Lvl = Lvl;
	}

	/**
	 * @return the Str
	 */
	public int getStr() {
		return Str;
	}

	/**
	 * @param Str the Str to set
	 */
	public void setStr(int Str) {
		this.Str = Str;
	}

	/**
	 * @return the Vit
	 */
	public int getVit() {
		return Vit;
	}

	/**
	 * @param Vit the Vit to set
	 */
	public void setVit(int Vit) {
		this.Vit = Vit;
	}

	/**
	 * @return the Int
	 */
	public int getInt() {
		return Int;
	}

	/**
	 * @param Int the Int to set
	 */
	public void setInt(int Int) {
		this.Int = Int;
	}

	/**
	 * @return the skills
	 */
	public Skill[] getSkills() {
		return skills;
	}

	/**
	 * @param skills the skills to set
	 */
	public void setSkills(Skill[] skills) {
		this.skills = skills;
	}
}
