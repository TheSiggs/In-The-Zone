package nz.dcoder.inthezone.objects;

import java.util.ArrayList;
import java.util.List;
import nz.dcoder.inthezone.Character;
import nz.dcoder.inthezone.Main;
import nz.dcoder.inthezone.concurrent.RunnableWithController;
import nz.dcoder.inthezone.jfx.MainHUDController;

/**
 * @author jedcalkin
 */
public class CharacterState {

	private int ap;
	private double hp;
	private double totalHp;
	private int level;
	private int strength;
	private int vitality;
	private int intelligence;
	private List<Skill> skills;
	private Weapon weapon;
	private Armour armour;
	private Character character;

	public CharacterState() {
		this.weapon = null;
		this.armour = null;
		this.skills = new ArrayList();
		this.level = 1;
		this.strength = 1;
		this.vitality = 1;
		this.intelligence = 1;
		this.ap = 10;
		this.hp = 10;
		this.totalHp = hp;

		/* // basic caracter
		 CharacterState zan = new CharacterState();
		 zan.weapon = new Weapon(1); // damage 1
		 zan.skills[0] = new Skill("Physical",1,1); // type(Physical,magic), damage, range
		 zan.armour = new Armour(0.1,0); // Physical 10%, magic 0%
		 zan.maxHp(); // set hp
		 */
	}

	public void maxHp() {
		int f = 1;
		int g = 1;
		setHp(getVitality() * hpMod(getLevel()) * f + g);
	}

	public int hpMod(int Lvl) {
		// TODO Get from table
		return Lvl;
	}

	public double Physical_Defense() {
		return armour.getPhysical();
	}

	public double magicalDefence() {
		double def = 0;
		// sum of all items
		def += armour.getAntiMagic();
		return 0;
	}

	/**
	 * @return the ap
	 */
	public int getAp() {
		return ap;
	}

	/**
	 * @param ap the ap to set
	 */
	public void setAp(int ap) {
		this.ap = ap;
	}

	/**
	 * @return the hp
	 */
	public double getHp() {
		return hp;
	}

	/**
	 * @param hp the hp to set
	 */
	public void setHp(int hp) {
		this.hp = hp;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the strength
	 */
	public int getStrength() {
		return strength;
	}

	/**
	 * @param Strength the strength to set
	 */
	public void setStrength(int Strength) {
		this.strength = Strength;
	}

	/**
	 * @return the vitality
	 */
	public int getVitality() {
		return vitality;
	}

	/**
	 * @param vitality the vitality to set
	 */
	public void setVitality(int vitality) {
		this.vitality = vitality;
	}

	/**
	 * @return the intelligence
	 */
	public int getIntelligence() {
		return intelligence;
	}

	/**
	 * @param intelligence the intelligence to set
	 */
	public void setIntelligence(int intelligence) {
		this.intelligence = intelligence;
	}

	/**
	 * @return the skills
	 */
	public List<Skill> getSkills() {
		return skills;
	}

	/**
	 * @param skills the skills to set
	 */
	public void setSkills(List<Skill> skills) {
		this.skills = skills;
	}

	public double getDamage() {
		return 1.0;
	}

	public void decreaseHp(double damage) {
		this.hp -= damage;

		RunnableWithController gui = Main.instance.getGuiThread();
		MainHUDController controller = gui.getController();
		if (hp <= 0.0) {
			System.out.println("Health of character at " + character.getX() + "," + character.getY() + " is " + 0);
			controller.setHealth(0.0);
			die();
		} else {
			System.out.println("Health of character at " + character.getX() + "," + character.getY() + " is " + hp);
			controller.setHealth(hp / totalHp);
		}
	}

	private void die() {
		System.out.println("Character at " + character.getX() + "," + character.getY() + " died");
		character.die();
	}

	/**
	 * @return the character
	 */
	public Character getCharacter() {
		return character;
	}

	/**
	 * @param character the character to set
	 */
	public void setCharacter(Character character) {
		this.character = character;
	}
}
