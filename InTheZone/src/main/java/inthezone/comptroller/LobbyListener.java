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
	public void connectedToServer(String playerName, Collection<String> players);
	public Optional<String> tryDifferentPlayerName(String name);
	public void errorConnectingToServer(Exception e);
	public void serverError(Exception e);

	/**
	 * A message from the server that should be reported to the player.
	 * */
	public void serverNotification(String e);

	public void connectionDropped();
	public void loggedOff();
	public void playerHasLoggedIn(String player);
	public void playerHasLoggedOff(String player);
	public void playerHasEnteredBattle(String player);
	public void playerRefusesChallenge(String player);
	public void challengeFrom(String player, StartBattleCommandRequest cmd);

	/**
	 * Our challenge has been issued to the named player.
	 * */
	public void challengeIssued(String player);


	/**
	 * Start a battle.
	 * @param player The player we are playing
	 * @param otherPlayer The name of the other player
	 * */
	public void startBattle(StartBattleCommand battle, Player player, String otherPlayer);
}

