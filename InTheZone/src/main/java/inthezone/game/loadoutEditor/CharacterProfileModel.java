package inthezone.game.loadoutEditor;

import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.CharacterProfile;
import isogame.engine.CorruptDataException;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;

public class CharacterProfileModel {
	public final ObservableList<AbilityInfo> abilities = FXCollections.observableArrayList();
	public final ObjectProperty<AbilityInfo> basicAbility = new SimpleObjectProperty<>();

	public final IntegerProperty hpPP = new SimpleIntegerProperty(0);
	public final IntegerProperty attackPP = new SimpleIntegerProperty(0);
	public final IntegerProperty defencePP = new SimpleIntegerProperty(0);

	private final ReadOnlyIntegerWrapper cost = new ReadOnlyIntegerWrapper(0);
	private final ReadOnlyObjectWrapper<CharacterProfile> profile =
		new ReadOnlyObjectWrapper<>(null);

	public CharacterInfo rootCharacter = null;

	public void unbindAll() {
		basicAbility.unbind();
		hpPP.unbind();
		attackPP.unbind();
		defencePP.unbind();
	}

	public CharacterProfileModel(CharacterProfile c) {
		init(c);
		InvalidationListener update = v -> {
			System.err.println("Updating profile: " + abilities.toString());
			CharacterProfile newProfile = encodeProfile();
			cost.setValue(newProfile.computeCost());
			profile.setValue(newProfile);
		};

		basicAbility.addListener(update);
		abilities.addListener(update);
		hpPP.addListener(update);
		attackPP.addListener(update);
		defencePP.addListener(update);
	}

	public void init(CharacterProfile c) {
		rootCharacter = c.rootCharacter;
		basicAbility.setValue(c.basicAbility);
		abilities.clear();
		for (AbilityInfo a : c.abilities) abilities.add(a);
		hpPP.setValue(c.hpPP);
		attackPP.setValue(c.attackPP);
		defencePP.setValue(c.defencePP);

		CharacterProfile newProfile = encodeProfile();
		cost.setValue(newProfile.computeCost());
		profile.setValue(newProfile);
	}

	public ReadOnlyIntegerProperty costProperty() {
		return cost.getReadOnlyProperty();
	}

	public ReadOnlyObjectProperty<CharacterProfile> profileProperty() {
		return profile.getReadOnlyProperty();
	}

	private CharacterProfile encodeProfile() {
		try {
			return new CharacterProfile(
				rootCharacter, new ArrayList<>(abilities),
				basicAbility.getValue(), 
				attackPP.getValue(), defencePP.getValue(),
				hpPP.getValue());
		} catch (CorruptDataException e) {
			throw new RuntimeException("Invalid character profile", e);
		}
	}
}

