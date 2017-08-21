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

	private int lastSequenceNumber = 0;

	public NetworkReader(
		final BufferedReader in,
		final LobbyListener lobbyListener,
		final BlockingQueue<Command> recQueue,
		final GameDataFactory gameData,
		final Thread parent
	) {
		this.in = in;
		this.lobbyListener = lobbyListener;
		this.recQueue = recQueue;
		this.gameData = gameData;
		this.parent = parent;
	}

	public int getLastSequenceNumber() { return lastSequenceNumber; }

	@Override
	public void run() {
		try {

			for (
				String inline = in.readLine();
				inline != null; inline = in.readLine()
			) {
				final Message msg = Message.fromString(inline);
				final int sequenceNumber = msg.getSequenceNumber();

				switch (msg.kind) {
					case PLAYERS_JOIN:
						for (final String p : msg.parseJoinedLobby())
							lobbyListener.playerHasLoggedIn(p);
						for (final String p : msg.parseLeftLobby())
							lobbyListener.playerHasLoggedOff(p);
						for (final String p : msg.parseJoinedGame())
							lobbyListener.playerHasEnteredBattle(p);
						break;

					case CHALLENGE_PLAYER:
						try {
							lobbyListener.challengeFrom(msg.parseName(),
								StartBattleCommandRequest.fromJSON(
									msg.payload.getJSONObject("cmd"), gameData));
						} catch (final JSONException e) {
							throw new ProtocolException(
								"NetworkReader 10: Malformed command", e);
						}
						break;

					case REJECT_CHALLENGE:
						lobbyListener.playerRefusesChallenge(msg.parseOtherPlayer());
						break;

					case START_BATTLE:
						try {
							lobbyListener.startBattle(
								StartBattleCommand.fromJSON(msg.parseCommand(), gameData),
								msg.parsePlayer(), msg.payload.getString("otherPlayer"));
						} catch (final JSONException e) {
							throw new ProtocolException(
								"NetworkReader 20: Invalid start battle command", e);
						}
						break;

					case COMMAND:
						try {
							recQueue.put(Command.fromJSON(msg.parseCommand()));
							lastSequenceNumber = sequenceNumber;
						} catch (final InterruptedException e) {
							/* ignore */
						}
						break;

					case LOGOFF:
						lobbyListener.otherClientDisconnects(true);
						break;

					case RECONNECT:
						lobbyListener.otherClientReconnects();
						break;

					case WAIT_FOR_RECONNECT:
						lobbyListener.otherClientDisconnects(false);
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

		} catch (final IOException e) {
			// TODO: try to recover the situation
		} catch (final ProtocolException e) {
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

