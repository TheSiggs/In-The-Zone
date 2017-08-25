package inthezone.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.Player;

public class GameQueueElement {
	public final Client client;
	public final Set<String> allowableMaps = new HashSet<>();

	public GameQueueElement(
		final Client client,
		final List<String> vetoMaps,
		final Collection<String> allMaps
	) {
		this.client = client;
		allowableMaps.addAll(allMaps);
		allowableMaps.removeAll(vetoMaps);
	}

	public class StartBattlePair {
		public final StartBattleCommandRequest thisone;
		public final StartBattleCommandRequest thatone;

		private StartBattlePair(
			final String stage,
			final String thisPlayerName,
			final String thatPlayerName
		) {
			final Player thisPlayer = Player.randomPlayer();
			final Player thatPlayer = thisPlayer.otherPlayer();

			thisone = new StartBattleCommandRequest(
				stage, thisPlayer, thisPlayerName,
				Optional.empty(), Optional.empty());
			thatone = new StartBattleCommandRequest(
				stage, thatPlayer, thatPlayerName,
				Optional.empty(), Optional.empty());
		}
	}

	/**
	 * Attempt to match this client with another client
	 * @return if the clients are compatible, then a pair of
	 * StartBattleCommandRequests
	 * */
	public Optional<StartBattlePair> match(
		final GameQueueElement other)
	{
		final List<String> intersection =
			other.allowableMaps.stream()
				.filter(s -> allowableMaps.contains(s))
				.collect(Collectors.toList());

		if (intersection.isEmpty()) {
			return Optional.empty();
		} else {
			final int i =
				(int) Math.floor(Math.random() * ((double) intersection.size()));

			final String stage = intersection.get(i >= intersection.size()? 0 : i);
			return Optional.of(new StartBattlePair(
				stage, client.getClientName(), other.client.getClientName()));
		}
	}
}

