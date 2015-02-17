package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nz.dcoder.inthezone.data_model.pure.BaseStats;
import nz.dcoder.inthezone.data_model.pure.BattleObjectName;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.EquipmentClass;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Represents a character.  This information persists between battles.
 * */
public class Character implements CanDoAbility {
	public final CharacterName name;
	public final String description;
	private final BaseStats baseStats;
	private final LevelController level;
	private final BiFunction<BattleObjectName, Position, BattleObject> makeBody;
	private final BattleObjectName bodyName;

	public int hp;
	public Position position;

	private Equipment weapon;
	private final List<Equipment> equipment;

	public boolean isDead = false;

	private BaseStats effectiveBaseStats;

	public Character(
		CharacterName name,
		String description,
		BaseStats baseStats,
		LevelController level,
		BiFunction<BattleObjectName, Position, BattleObject> makeBody,
		BattleObjectName bodyName
	) {
		this.name = name;
		this.description = description;
		this.baseStats = baseStats;
		this.effectiveBaseStats = baseStats;
		this.level = level;
		this.makeBody = makeBody;
		this.bodyName = bodyName;

		this.hp = level.maxHP;
		this.equipment = new ArrayList<Equipment>();
		this.position = new Position(0, 0);
	}

	private BaseStats computeEffectiveBaseStats() {
		BaseStats base;
		if (this.weapon == null) {
			base = baseStats;
		} else {
			base = baseStats.add(weapon.buffs);
		}

		return equipment.stream()
			.map(e -> e.buffs)
			.reduce(base, (b, c) -> b.add(c));
	}

	/**
	 * @return A BattleObject representing the corpse of this character
	 * */
	public BattleObject die() {
		isDead = true;
		return makeBody.apply(bodyName, this.position);
	}

	public int getMaxHP() {
		return level.maxHP;
	}

	public Collection<Ability> getAbilities() {
		return Stream.concat(
			equipment.stream().flatMap(e -> e.abilities.stream()),
			level.abilities.stream()
		).collect(Collectors.toList());
	}

	public Collection<Equipment> getArmour() {
		return equipment.stream()
			.filter(e -> e.eClass == EquipmentClass.ARMOUR)
			.collect(Collectors.toList());
	}

	/**
	 * This may get expanded to return information about whether or not the
	 * character leveled up, and what new abilities he/she learned.
	 * */
	public void addExp(int points) {
		hp += level.addExp(points, getBaseStats(), hp);
	}

	// These methods will change when we put restrictions on what equipment
	// characters can use

	/**
	 * Add equipment (such as armour) to this character
	 * */
	public void equip(Equipment e) {
		equipment.add(e);
		effectiveBaseStats = computeEffectiveBaseStats();
	}

	/**
	 * Remove equipment from this character
	 * */
	public void unequip(Equipment e) {
		equipment.remove(e);
		effectiveBaseStats = computeEffectiveBaseStats();
	}

	/**
	 * Equip a weapon.
	 * @param e The weapon, must be of class WEAPON
	 * @return The previous weapon, or null if this character didn't have a
	 * weapon
	 * */
	public Equipment equipWeapon(Equipment e) {
		if (e.eClass != EquipmentClass.WEAPON)
			throw new RuntimeException(e.toString() + " is not a weapon");

		Equipment r = this.weapon;
		this.weapon = e;
		effectiveBaseStats = computeEffectiveBaseStats();
		return r;
	}

	/**
	 * Unequip a weapon
	 * @return The weapon, or null if this character didn't have a weapon
	 * */
	public Equipment unequipWeapon() {
		Equipment r = this.weapon;
		this.weapon = null;
		effectiveBaseStats = computeEffectiveBaseStats();
		return r;
	}

	/**
	 * Get visible equipment for the UI
	 * */
	public Collection<Equipment> getVisibleEquipment() {
		return equipment.stream()
			.filter(e -> !e.isHidden).collect(Collectors.toList());
	}

	@Override public Position getPosition() {
		return position;
	}

	@Override public BaseStats getBaseStats() {
		return effectiveBaseStats;
	}

	@Override public int getLevel() {
		return level.level;
	}

	@Override public Equipment getWeapon() {
		if (this.weapon == null) {
			throw new RuntimeException("Character " + this.name.toString() +
				" doesn't have a weapon.  This should have been check during battle setup");
		} else {
			return this.weapon;
		}
	}
}

