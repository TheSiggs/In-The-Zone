package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import isogame.engine.CorruptDataException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class LoadoutModel {
	public final StringProperty name = new SimpleStringProperty();

	public final List<CharacterProfileModel> usedProfiles = new ArrayList<>();
	public final Queue<CharacterProfileModel> otherProfiles = new LinkedList<>();

	public LoadoutModel(GameDataFactory gameData)
		throws CorruptDataException
	{
		for (CharacterInfo c : gameData.getCharacters()) {
			if (c.playable) {
				otherProfiles.add(new CharacterProfileModel(new CharacterProfile(c)));
			}
		}

		while (usedProfiles.size() < 4) {
			usedProfiles.add(otherProfiles.poll());
		}
	}

	public CharacterProfileModel substituteCharacter(CharacterProfileModel old) {
		int i = usedProfiles.indexOf(old);
		CharacterProfileModel r = otherProfiles.poll();
		otherProfiles.offer(usedProfiles.set(i, r));
		return r;
	}
	
	public LoadoutModel(GameDataFactory gameData, Loadout loadout)
		throws CorruptDataException
	{
		name.setValue(loadout.name);

		final Set<String> seenCharacters = new HashSet<>();

		usedProfiles.addAll(loadout.characters.stream()
			.map(p -> {
				seenCharacters.add(p.rootCharacter.name);
				return new CharacterProfileModel(p);
			})
			.collect(Collectors.toList()));

		otherProfiles.addAll(loadout.otherCharacters.stream()
			.map(p -> {
				seenCharacters.add(p.rootCharacter.name);
				return new CharacterProfileModel(p);
			})
			.collect(Collectors.toList()));

		for (CharacterInfo c : gameData.getCharacters()) {
			if (c.playable && !seenCharacters.contains(c.name)) {
				otherProfiles.add(new CharacterProfileModel(new CharacterProfile(c)));
			}
		}

		while (usedProfiles.size() < 4) {
			usedProfiles.add(otherProfiles.poll());
		}
	}

	public Loadout encodeLoadout() {
		final List<CharacterProfile> characters = usedProfiles.stream()
			.map(p -> p.profileProperty().get())
			.collect(Collectors.toList());

		final List<CharacterProfile> others = otherProfiles.stream()
			.map(p -> p.profileProperty().get())
			.collect(Collectors.toList());

		return new Loadout(name.getValue(), characters, others);
	}
}

