package inthezone.comptroller;

import inthezone.battle.commands.Command;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.protocol.Message;
import inthezone.protocol.ProtocolException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import org.json.JSONException;
import org.json.JSONObject;

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
									msg.payload.getJSONObject("cmd"), gameData));
						} catch (JSONException e) {
							throw new ProtocolException("NetworkReader 10: Malformed command", e);
						}
						break;

					case REJECT_CHALLENGE:
						lobbyListener.playerRefusesChallenge(msg.parseName());
						break;

					case START_BATTLE:
						try {
							lobbyListener.startBattle(
								StartBattleCommand.fromJSON(msg.parseCommand(), gameData),
								msg.parsePlayer(), msg.payload.getString("otherPlayer"));
						} catch (JSONException e) {
							throw new ProtocolException(
								"NetworkReader 20: Invalid start battle command", e);
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
						// This can be safely ignored for now
						break;

					case LOGOFF:
						lobbyListener.otherClientDisconnects(true);
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

			// if we get to here, then the connection was closed.
			try {
				// Make sure the connection is fully closed (not just half-closed)
				in.close();
				parent.interrupt();
			} catch (IOException e) {
				/* Doesn't matter */
			}

		} catch (IOException e) {
			// TODO: try to recover the situation
		} catch (ProtocolException e) {
			try {
				in.close();
				parent.interrupt();
			} catch (IOException e2) {
				/* Doesn't matter */
			}

			lobbyListener.serverError(e);
		}
	}
}

