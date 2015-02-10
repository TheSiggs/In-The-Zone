package nz.dcoder.inthezone.data_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nz.dcoder.inthezone.data_model.pure.BaseStats;
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

	public int hp;
	public Position position;

	private final List<Equipment> equipment;

	public boolean isDead = false;

	private BaseStats effectiveBaseStats;

	public Character(
		CharacterName name,
		String description,
		BaseStats baseStats,
		LevelController level
	) {
		this.name = name;
		this.description = description;
		this.baseStats = baseStats;
		this.effectiveBaseStats = baseStats;
		this.level = level;

		this.hp = level.maxHP;
		this.equipment = new ArrayList<Equipment>();
		this.position = new Position(0, 0);
	}

	private BaseStats computeEffectiveBaseStats() {
		return equipment.stream()
			.map(e -> e.buffs)
			.reduce(baseStats, (b, c) -> b.add(c));
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

	public void equip(Equipment e) {
		equipment.add(e);
		effectiveBaseStats = computeEffectiveBaseStats();
	}

	public void unequip(Equipment e) {
		equipment.remove(e);
		effectiveBaseStats = computeEffectiveBaseStats();
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

	@Override public Collection<Equipment> getWeapons() {
		return equipment.stream()
			.filter(e -> e.eClass == EquipmentClass.WEAPON)
			.collect(Collectors.toList());
	}
}

