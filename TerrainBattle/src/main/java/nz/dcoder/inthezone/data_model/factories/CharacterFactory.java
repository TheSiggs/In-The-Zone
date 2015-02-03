package nz.dcoder.inthezone.data_model.factories;

import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.Character;

public class CharacterFactory {
	private final LevelControllerFactory levelControllerFactory;

	public CharacterFactory() {
		this.levelControllerFactory = new LevelControllerFactory();
	}

	public Character newCharacter(CharacterName name) {
		return null;
	}
}

