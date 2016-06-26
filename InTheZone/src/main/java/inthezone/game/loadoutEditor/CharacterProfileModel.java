package inthezone.game.loadoutEditor;

import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.CharacterProfile;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CharacterProfileModel {
	public final ObservableList<AbilityInfo> abilities = FXCollections.observableArrayList();
	public final ObjectProperty<AbilityInfo> basicAbility = new SimpleObjectProperty<>();

	private final ReadOnlyIntegerWrapper cost = new ReadOnlyIntegerWrapper(0);

	private CharacterInfo rootCharacter = null;

	public CharacterProfileModel(CharacterProfile c) {
		init(c);
		basicAbility.addListener((x, x0, x1) -> cost.setValue(computeCost()));
		abilities.addListener((Change<? extends AbilityInfo> change) ->
			cost.setValue(computeCost()));
	}

	public void init(CharacterProfile c) {
		rootCharacter = c.rootCharacter;
		abilities.clear();
		for (AbilityInfo a : c.abilities) abilities.add(a);
	}

	public ReadOnlyIntegerProperty costProperty() {
		return cost.getReadOnlyProperty();
	}

	public CharacterProfile encodeProfile() {
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

