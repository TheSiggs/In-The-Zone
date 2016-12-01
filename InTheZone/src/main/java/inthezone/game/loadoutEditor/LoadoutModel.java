package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Loadout;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.stream.Collectors;

public class LoadoutModel {
	public final StringProperty name = new SimpleStringProperty();
	public final ObservableList<CharacterProfileModel> profiles =
		FXCollections.observableArrayList();
	
	public LoadoutModel(Loadout loadout) {
		name.setValue(loadout.name);
		profiles.clear();
		loadout.characters.stream()
			.map(p -> new CharacterProfileModel(p)).forEach(m -> profiles.add(m));
	}

	public Loadout encodeLoadout() {
		return new Loadout(name.getValue(),
			profiles.stream()
				.map(p -> p.profileProperty().getValue())
				.collect(Collectors.toList()));
	}
}

