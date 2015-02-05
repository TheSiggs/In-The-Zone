package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nz.dcoder.inthezone.data_model.pure.BaseStats;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Represents a character.  This information persists between battles.
 * */
public class Character implements CanDoAbility {
	final public CharacterName name;
	final public String description;
	final private BaseStats baseStats;
	final private LevelController level;

	public int hp;
	public Position position;

	public List<Equipment> equipment;

	public boolean isDead = false;

	public Character(
		CharacterName name,
		String description,
		BaseStats baseStats,
		LevelController level
	) {
		this.name = name;
		this.description = description;
		this.baseStats = baseStats;
		this.level = level;

		this.hp = level.maxHP;
		this.equipment = new ArrayList<Equipment>();
		this.position = new Position(0, 0);
	}

	/**
	 * @return A BattleObject representing the corpse of this character
	 * */
	public BattleObject die() {
		// note:  remember that you use BattleObjectFactory to make a battle
		// object.  For each character, the factory will know how to make that
		// character's corpse
		isDead = true;
		// TODO: implement this method
		return null;
	}

	public int getMaxHP() {
		return level.maxHP;
	}

	public Collection<Ability> getAbilities() {
		return level.abilities;
	}

	public Collection<Equipment> getArmour() {
		// TODO: implement this method
		return null;
	}

	/**
	 * This may get expanded to return information about whether or not the
	 * character leveled up, and what new abilities he/she learned.
	 * */
	public void addExp(int points) {
		// TODO: implement this method
		return;
	}

	@Override public BaseStats getBaseStats() {
		// TODO: implement this method
		return null;
	}

	@Override public int getLevel() {
		// TODO: implement this method
		return level.level;
	}

	@Override public Collection<Equipment> getWeapons() {
		// TODO: implement this method
		return null;
	}
}

