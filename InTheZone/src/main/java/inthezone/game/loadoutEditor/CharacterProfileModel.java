package inthezone.game.loadoutEditor;

import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.CharacterProfile;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CharacterProfileModel {
	public final ObservableList<AbilityInfo> abilities = FXCollections.observableArrayList();
	public final ObjectProperty<AbilityInfo> basicAbility = new SimpleObjectProperty<>();

	private final ReadOnlyIntegerWrapper cost = new ReadOnlyIntegerWrapper(0);
	private final ReadOnlyObjectWrapper<CharacterProfile> profile =
		new ReadOnlyObjectWrapper<>(null);

	private CharacterInfo rootCharacter = null;

	public CharacterProfileModel(CharacterProfile c) {
		init(c);
		basicAbility.addListener(v -> {
			cost.setValue(computeCost());
			profile.setValue(encodeProfile());
		});
		abilities.addListener((Observable v) -> {
			cost.setValue(computeCost());
			profile.setValue(encodeProfile());
		});
	}

	public void init(CharacterProfile c) {
		rootCharacter = c.rootCharacter;
		basicAbility.setValue(c.basicAbility);
		abilities.clear();
		for (AbilityInfo a : c.abilities) abilities.add(a);
	}

	public ReadOnlyIntegerProperty costProperty() {
		return cost.getReadOnlyProperty();
	}

	public ReadOnlyObjectProperty<CharacterProfile> profileProperty() {
		return profile.getReadOnlyProperty();
	}

	private CharacterProfile encodeProfile() {
		return new CharacterProfile(
			rootCharacter, new ArrayList<>(abilities),
			basicAbility.getValue(), 0, 0, 0);
	}

	private int computeCost() {
		return
			basicAbility.getValue().pp + 
			abilities.stream().map(a -> a.pp).collect(
				Collectors.summingInt(x -> (int) x));
	}
}

