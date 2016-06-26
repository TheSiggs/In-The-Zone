package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Loadout;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LoadoutModel {
	public final StringProperty name = new SimpleStringProperty();
	public final ObservableList<CharacterProfile> profiles =
		FXCollections.observableArrayList();
	
	public LoadoutModel(Loadout loadout) {
		name.setValue(loadout.name);
		profiles.clear();
		profiles.addAll(loadout.characters);
	}

	public Loadout encodeLoadout() {
		return new Loadout(name.getValue(), profiles);
	}
}

