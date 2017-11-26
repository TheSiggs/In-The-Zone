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
import java.util.function.Consumer;

/**
 * Data model for character profiles.
 * */
public class CharacterProfileModel {
	public final ObservableList<AbilityInfo> abilities = FXCollections.observableArrayList();
	public final ObjectProperty<AbilityInfo> basicAbility = new SimpleObjectProperty<>();

	public final IntegerProperty hpPP = new SimpleIntegerProperty(0);
	public final IntegerProperty attackPP = new SimpleIntegerProperty(0);

	private final ReadOnlyIntegerWrapper cost = new ReadOnlyIntegerWrapper(0);
	private final ReadOnlyObjectWrapper<CharacterProfile> profile =
		new ReadOnlyObjectWrapper<>(null);

	public CharacterInfo rootCharacter = null;

	public void unbindAll() {
		basicAbility.unbind();
		hpPP.unbind();
		attackPP.unbind();
	}

	public CharacterProfileModel(final CharacterProfile c) {
		init(c);
		InvalidationListener update = v -> {
			CharacterProfile newProfile = encodeProfile();
			profile.setValue(newProfile);
			cost.setValue(newProfile.computeCost());
		};

		profileProperty().addListener((v, o, n) -> profileUpdate.accept(n));

		basicAbility.addListener(update);
		abilities.addListener(update);
		hpPP.addListener(update);
		attackPP.addListener(update);
	}

	public void init(final CharacterProfile c) {
		rootCharacter = c.rootCharacter;
		basicAbility.setValue(c.basicAbility);
		abilities.clear();
		for (final AbilityInfo a : c.abilities) abilities.add(a);
		hpPP.setValue(c.hpPP);
		attackPP.setValue(c.attackPP);

		final CharacterProfile newProfile = encodeProfile();
		cost.setValue(newProfile.computeCost());
		profile.setValue(newProfile);
	}

	public ReadOnlyIntegerProperty costProperty() {
		return cost.getReadOnlyProperty();
	}

	public ReadOnlyObjectProperty<CharacterProfile> profileProperty() {
		return profile.getReadOnlyProperty();
	}

	Consumer<CharacterProfile> profileUpdate = x -> {};
	public void setProfileUpdateReceiver(
		final Consumer<CharacterProfile> profileUpdate
	) {
		this.profileUpdate = profileUpdate;
	}

	private CharacterProfile encodeProfile() {
		try {
			return new CharacterProfile(
				rootCharacter, new ArrayList<>(abilities),
				basicAbility.getValue(), 
				attackPP.getValue(), hpPP.getValue());
		} catch (final CorruptDataException e) {
			throw new RuntimeException("Invalid character profile", e);
		}
	}
}

