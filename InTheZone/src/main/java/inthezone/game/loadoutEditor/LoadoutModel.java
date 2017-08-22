package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import isogame.engine.CorruptDataException;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LoadoutModel {
	public final StringProperty name = new SimpleStringProperty();

	public final List<CharacterProfileModel> usedProfiles = new ArrayList<>();
	public final Deque<CharacterProfileModel> otherProfiles = new LinkedList<>();

	public LoadoutModel(final GameDataFactory gameData)
		throws CorruptDataException
	{
		name.set("New loadout");

		for (final CharacterInfo c : gameData.getCharacters()) {
			if (c.playable) {
				otherProfiles.add(new CharacterProfileModel(new CharacterProfile(c)));
			}
		}

		while (usedProfiles.size() < 4) {
			usedProfiles.add(otherProfiles.poll());
		}

		rebindTotalCost();
	}

	public final IntegerProperty totalCost = new SimpleIntegerProperty(0);

	public void rebindTotalCost() {
		totalCost.bind(usedProfiles.stream()
			.map(pr -> (NumberExpression) pr.costProperty())
			.reduce(new SimpleIntegerProperty(0), (x, y) -> x.add(y)));
	}

	/**
	 * Substitute with the previous character in the queue.
	 * */
	public CharacterProfileModel substituteLeft(final CharacterProfileModel old) {
		final int i = usedProfiles.indexOf(old);
		final CharacterProfileModel r = otherProfiles.pollLast();
		otherProfiles.offerFirst(usedProfiles.set(i, r));
		rebindTotalCost();
		return r;
	}

	/**
	 * Substitute with the next character in the queue.
	 * */
	public CharacterProfileModel substituteRight(final CharacterProfileModel old) {
		final int i = usedProfiles.indexOf(old);
		final CharacterProfileModel r = otherProfiles.pollFirst();
		otherProfiles.offerLast(usedProfiles.set(i, r));
		rebindTotalCost();
		return r;
	}
	
	public LoadoutModel(final GameDataFactory gameData, final Loadout loadout)
		throws CorruptDataException
	{
		name.set(loadout.name);

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

		for (final CharacterInfo c : gameData.getCharacters()) {
			if (c.playable && !seenCharacters.contains(c.name)) {
				otherProfiles.add(new CharacterProfileModel(new CharacterProfile(c)));
			}
		}

		while (usedProfiles.size() < 4) {
			usedProfiles.add(otherProfiles.poll());
		}

		rebindTotalCost();
	}

	public Loadout encodeLoadout() {
		if (name.get().equals("")) name.set("<no name>");

		final List<CharacterProfile> characters = usedProfiles.stream()
			.map(p -> p.profileProperty().get())
			.collect(Collectors.toList());

		final List<CharacterProfile> others = otherProfiles.stream()
			.map(p -> p.profileProperty().get())
			.collect(Collectors.toList());

		return new Loadout(name.getValue(), characters, others);
	}
}

