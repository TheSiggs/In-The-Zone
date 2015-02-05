package nz.dcoder.inthezone.data_model.factories;

import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.Character;

public class CharacterFactory {
	private final LevelControllerFactory levelControllerFactory;

	public CharacterFactory(AbilityFactory abilityFactory)
		throws DatabaseException
	{
		this.levelControllerFactory =
			new LevelControllerFactory(abilityFactory);
	}

	public Character newCharacter(CharacterName name) {
		return null;
	}
}

