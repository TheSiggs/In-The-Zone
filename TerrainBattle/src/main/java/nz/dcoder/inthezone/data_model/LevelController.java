package nz.dcoder.inthezone.data_model;

import java.util.Collection;

/**
 * Controls levelling and things that vary by level
 * */
public class LevelController {
	public int exp = 0;
	public int level = 0;
	public int maxHP = 0;
	public Collection<Ability> abilities;

	// For these arrays, the indices represent levels, so hpMod[1] is the hpMod
	// for level 1.  Since levels start at 1, but array indices start at 0, the
	// element at index 0 is ignored.
	private final int[] hpMod;  // hpMod by level

	// For each level, the abilities that become available at that level
	private final Collection<Ability>[] abilitiesByLevel;

	public LevelController(int[] hpMod, Collection<Ability>[] abilitiesByLevel) {
		this.hpMod = hpMod;
		this.abilitiesByLevel = abilitiesByLevel;
	}

	/**
	 * This method adds experience points, and recomputes the level, maxHP,
	 * abilities, etc.
	 *
	 * @return The number of hp to add due to level up.  When the player levels
	 * up, maxHP increases, but the relative HP should stay the same, so we'll
	 * have to top up the player's hp a bit
	 * */
	public int addExp(int points) {
		// TODO: implement this method
		return 0;
	}
}

