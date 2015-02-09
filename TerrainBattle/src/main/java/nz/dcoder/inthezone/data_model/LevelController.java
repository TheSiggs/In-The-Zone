package nz.dcoder.inthezone.data_model;

import java.util.Collection;

/**
 * Controls leveling and things that vary by level
 * */
public class LevelController {
	public int exp = 0;
	public int level = 0;
	public int maxHP = 1;
	public Collection<Ability> abilities;

	// For these arrays, the indices represent levels, so hpMod[1] is the hpMod
	// for level 1.  Since levels start at 1, but array indices start at 0, the
	// element at index 0 is ignored.
	private final int[] hpMod;  // hpMod by level

	// For each level, the abilities that become available at that level
	private final Collection<Ability>[] abilitiesByLevel;

	// For each level, the total number of exp required of the character's
	// lifetime to reach that level.  As usual, data at index 0 is ignored.
	private final int[] totalExpRequired;

	public LevelController(
		int[] hpMod,
		Collection<Ability>[] abilitiesByLevel,
		int[] totalExpRequired
	) {
		this.hpMod = hpMod;
		this.abilitiesByLevel = abilitiesByLevel;
		this.totalExpRequired = totalExpRequired;
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
		// exp
                exp+=points;
                
                // level
                if(totalExpRequired[level+1]<=exp){
                    level+=1;
                }
                // multi leveling
                if(totalExpRequired[level+1]<=exp){
                    int i=level;
                    while(i<totalExpRequired.length && totalExpRequired[i]<exp){i++;}
                    level = i;
                }
                
                // abilitys
                abilities = abilitiesByLevel[level];
                        
                // HP
                int vit = 1; // accsess the character object??
                int hp = 1; // accsess the character object??
                double ratio = hp/maxHP;
                int oldMaxHp = maxHP;
                maxHP = (int)Math.floor((vit*hpMod[level])/75);
		return (int)ratio*(maxHP-oldMaxHp);
                
	}
}

