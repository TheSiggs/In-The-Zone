package inthezone.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;

import org.json.JSONObject;

import inthezone.util.Log;

/**
 * An active game on the server.
 * */
public class Game {
	public final Client clientA;
	public final Client clientB;
	private final File outFile;
	private PrintWriter out = null;

	/**
	 * @param clientA player A
	 * @param clientB player B
	 * */
	public Game(
		final Client clientA,
		final Client clientB
	) {
		this.clientA = clientA;
		this.clientB = clientB;
		this.outFile = new File(Server.GAMES_DIR,
			"" + (new Date()).getTime() + "_" +
				clientA.getClientName() + "_vs_" + clientB.getClientName() + ".game");

		try {
			this.out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8")), true);
		} catch (IOException e) {
			Log.error("Cannot create recorded game file \"" + outFile + "\"", null);
		}
	}

	/**
	 * Get the other player.
	 * @param me this player
	 * */
	public Client getOther(final Client me) {
		return clientA == me? clientB : clientA;
	}

	/**
	 * Handle the next command.
	 * @param cmd the command
	 * @param player the player that issued the command
	 * */
	public void nextCommand(final JSONObject cmd, final Client player) {
		if (out != null)
			out.println(player.getClientName() + "/" + cmd.toString());
	}

	/**
	 * End the game.
	 * */
	public void close() {
		out.close();
	}
}

