package inthezone.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.Player;

/**
 * An entry on the game queue.
 * */
public class GameQueueElement {
	public final Client client;
	public final Set<String> allowableMaps = new HashSet<>();

	/**
	 * @param client the client waiting on the queue
	 * @param vetoMaps the maps that the client doesn't want to play
	 * @param allMaps
	 * */
	public GameQueueElement(
		final Client client,
		final List<String> vetoMaps,
		final Collection<String> allMaps
	) {
		this.client = client;
		allowableMaps.addAll(allMaps);
		allowableMaps.removeAll(vetoMaps);
	}

	/**
	 * A pair of StartBattleCommandRequests.
	 * */
	public class StartBattlePair {
		public final StartBattleCommandRequest thisone;
		public final StartBattleCommandRequest thatone;

		/**
		 * @param stage the stage to play
		 * @param thisPlayerName this player name
		 * @param thatPlayerName the other player name
		 * */
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
	 * @param other the other client
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

