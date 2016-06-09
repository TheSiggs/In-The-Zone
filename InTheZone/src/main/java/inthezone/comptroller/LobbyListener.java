package inthezone.comptroller;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import java.util.Collection;
import java.util.Optional;

public interface LobbyListener {
	public void connectedToServer(Collection<String> players);
	public Optional<String> tryDifferentPlayerName(String name);
	public void errorConnectingToServer(Exception e);
	public void serverError(Exception e);
	public void connectionDropped();
	public void playerHasLoggedIn(String player);
	public void playerHasLoggedOff(String player);
	public void playerHasEnteredBattle(String player);
	public void playerRefusesChallenge(String player);
	public void challengeFrom(String player, StartBattleCommandRequest cmd);
	public void startBattle(BattleInProgress battle, String player);
}

