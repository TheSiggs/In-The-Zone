package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Loadout;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class LoadoutModel {
	public final StringProperty name = new SimpleStringProperty();

	public final List<Optional<CharacterProfileModel>> usedProfiles = new ArrayList<>();
	public final Collection<CharacterProfileModel> otherProfiles = new ArrayList<>();
	
	public LoadoutModel(Loadout loadout) {
		name.setValue(loadout.name);

		for (int i = 0; i < 4; i++) usedProfiles.add(Optional.empty());

		for (int i = 0; i < loadout.characters.size(); i++) {
			if (i < 4) {
				usedProfiles.set(i, Optional.of(
					new CharacterProfileModel(loadout.characters.get(i))));
			} else {
				otherProfiles.add(new CharacterProfileModel(loadout.characters.get(i)));
			}
		}
	}

	public Loadout encodeLoadout() {
		final List<CharacterProfile> characters = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			usedProfiles.get(i).ifPresent(p -> characters.add(p.profileProperty().get()));
		}

		return new Loadout(name.getValue(), characters);
	}
}

