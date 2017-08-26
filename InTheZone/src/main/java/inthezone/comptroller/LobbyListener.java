package inthezone.comptroller;

import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import java.util.Collection;
import java.util.Optional;

/**
 * These methods are invoked in the network thread.
 * */
public interface LobbyListener {
	public void connectedToServer(
		final String playerName, final Collection<String> players);
	public void errorConnectingToServer(final Exception e);
	public void serverError(final Exception e);

	/**
	 * A message from the server that should be reported to the player.
	 * */
	public void serverNotification(final String e);

	public void connectionDropped();
	public void loggedOff();
	public void playerHasLoggedIn(final String player);
	public void playerHasLoggedOff(final String player);
	public void playerHasEnteredBattle(final String player);

	public void playerRefusesChallenge(
		final String player, final boolean notReady);

	public void challengeFrom(
		final String player,
		final boolean fromQueue,
		final StartBattleCommandRequest cmd);

	/**
	 * Our challenge has been issued to the named player.
	 * */
	public void challengeIssued(final String player);

	/**
	 * An incoming challenge was cancelled.
	 * @param player The player that cancelled the challenge
	 * */
	public void challengeCancelled(final String player);

	/**
	 * We got thrown off the queue.
	 * */
	public void queueCancelled();

	/**
	 * The other client logged off.
	 * @param logoff The other client is not coming back
	 * */
	public void otherClientDisconnects(final boolean logoff);

	/**
	 * The other client reconnected
	 * */
	public void otherClientReconnects();

	/**
	 * Start a battle.
	 * @param player The player we are playing
	 * @param otherPlayer The name of the other player
	 * */
	public void startBattle(
		final StartBattleCommand battle,
		final Player player,
		final String otherPlayer,
		final boolean isFromQueue);
}

