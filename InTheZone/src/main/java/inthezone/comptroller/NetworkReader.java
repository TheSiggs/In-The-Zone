package inthezone.comptroller;

import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.GameDataFactory;
import inthezone.protocol.Message;
import inthezone.protocol.ProtocolException;
import java.io.BufferedReader;
import java.io.IOException;
import org.json.simple.JSONObject;

public class NetworkReader implements Runnable {
	private final BufferedReader in;
	private final LobbyListener lobbyListener;
	private final Thread parent;
	private final GameDataFactory gameData;

	public NetworkReader(
		BufferedReader in,
		LobbyListener lobbyListener,
		GameDataFactory gameData,
		Thread parent
	) {
		this.in = in;
		this.lobbyListener = lobbyListener;
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

					case ACCEPT_CHALLENGE:
						// TODO: implement this
						break;

					case REJECT_CHALLENGE:
						lobbyListener.playerRefusesChallenge(msg.parseName());
						break;

					case COMMAND:
						// TODO: implement this
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

					default:
						throw new ProtocolException("Server error: unexpected message");
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

