package inthezone.comptroller;

import inthezone.battle.commands.Command;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.protocol.Message;
import inthezone.protocol.ProtocolException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import org.json.simple.JSONObject;

public class NetworkReader implements Runnable {
	private final BufferedReader in;
	private final LobbyListener lobbyListener;
	private final Thread parent;
	private final GameDataFactory gameData;

	private final BlockingQueue<Command> recQueue;

	public NetworkReader(
		BufferedReader in,
		LobbyListener lobbyListener,
		BlockingQueue<Command> recQueue,
		GameDataFactory gameData,
		Thread parent
	) {
		this.in = in;
		this.lobbyListener = lobbyListener;
		this.recQueue = recQueue;
		this.gameData = gameData;
		this.parent = parent;
	}

	@Override
	public void run() {
		try {
			for (
				String inline = in.readLine();
				inline != null; inline = in.readLine()
			) {
				Message msg = Message.fromString(inline);
				switch (msg.kind) {
					case PLAYERS_JOIN:
						for (String p : msg.parseJoinedLobby()) lobbyListener.playerHasLoggedIn(p);
						for (String p : msg.parseLeftLobby()) lobbyListener.playerHasLoggedOff(p);
						for (String p : msg.parseJoinedGame()) lobbyListener.playerHasEnteredBattle(p);
						break;

					case CHALLENGE_PLAYER:
						try {
							lobbyListener.challengeFrom(msg.parseName(),
								StartBattleCommandRequest.fromJSON(
									(JSONObject) msg.payload.get("cmd"), gameData));
						} catch (ClassCastException e) {
							throw new ProtocolException("Malformed command", e);
						}
						break;

					case REJECT_CHALLENGE:
						lobbyListener.playerRefusesChallenge(msg.parseName());
						break;

					case START_BATTLE:
						Object ootherPlayer = msg.payload.get("otherPlayer");
						if (ootherPlayer == null)
							throw new ProtocolException("Invalid start battle command");
						try {
							lobbyListener.startBattle(
								StartBattleCommand.fromJSON(msg.parseCommand(), gameData),
								msg.parsePlayer(), (String) ootherPlayer);
						} catch (ClassCastException e) {
							throw new ProtocolException("Invalid start battle command");
						}
						break;

					case COMMAND:
						try {
							recQueue.put(Command.fromJSON(msg.parseCommand()));
						} catch (InterruptedException e) {
							/* ignore */
						}
						break;

					case GAME_OVER:
						// TODO: implement this
						break;

					case LOGOFF:
						// TODO: implement this
						break;

					case RECONNECT:
						// TODO: implement this
						break;

					case WAIT_FOR_RECONNECT:
						// TODO: implement this
						break;

					case ISSUE_CHALLENGE:
						lobbyListener.challengeIssued(msg.parseName());
						break;

					case NOK:
						lobbyListener.serverNotification(msg.parseMessage());
						break;

					default:
						throw new ProtocolException(
							"Server error: unexpected message of kind " + msg.kind);
				}
			}
		} catch (IOException e) {
			// TODO: try to recover the situation
		} catch (ProtocolException e) {
			lobbyListener.serverError(e);
			try {
				in.close();
				parent.interrupt();
			} catch (IOException e2) {
				/* Doesn't matter */
			}
		}
	}
}

