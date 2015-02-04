package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Combines a character with turns specific information about the character
 * */
public class TurnCharacter {
	public int mp;
	public int ap;
	public int maxMP;
	public int maxAP;
	private final Character character;
	private final int turnNumber;
	private final Battle battle;

	public TurnCharacter(
		Character character,
		int turnNumber,
		Battle battle
	) {
		this.character = character;
		this.turnNumber = turnNumber;
		this.battle = battle;
		this.maxMP = character.getBaseStats().baseMP;
		this.maxAP = character.getBaseStats().baseAP;
		mp = maxMP;
		ap = maxAP;
	}

	void doMotion(Position destination) {
		// TODO: implement this method
		return;
	}

	void doAbility(AbilityName name) {
		// TODO: implement this method
		return;
	}

	void useItem(Item item, Position target) {
		// TODO: implement this method
		return;
	}

	CharacterName getName() {
		return character.name;
	}

	Position getPos() {
		return character.position;
	}

	int getHP() {
		return character.hp;
	}

	int getMaxHP() {
		return character.getMaxHP();
	}

	Collection<Equipment> getVisibleEquipment() {
		// TODO: implement this method
		return null;
	}

	Collection<AbilityInfo> getAbilities() {
		// TODO: implement this method
		return null;
	}

	boolean isOnManaZone() {
		return battle.terrain.isManaZone(character.position);
	}
}

