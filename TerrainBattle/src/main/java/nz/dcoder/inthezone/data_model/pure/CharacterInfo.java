package nz.dcoder.inthezone.data_model.pure;

import java.util.Collection;

/**
 * Stores a copy of the essential information about a character
 * */
public class CharacterInfo {
	public final CharacterName name;
	public final String description;
	public final int level;
	public final Collection<AbilityInfo> abilities;

	public Points mp = null;
	public Points ap = null;
	public Points hp = null;

	public CharacterInfo(
		CharacterName name,
		String description,
		int level,
		Points hp,
		Collection<AbilityInfo> abilities
	) {
		this.name = name;
		this.description = description;
		this.level = level;
		this.abilities = abilities;
		this.hp = hp;
	}
}

