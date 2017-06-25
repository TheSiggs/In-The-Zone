package inthezone.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;

import org.json.JSONObject;

import inthezone.Log;

public class Game {
	public final Client clientA;
	public final Client clientB;
	private final File outFile;
	private PrintWriter out = null;

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

	public Client getOther(final Client me) {
		return clientA == me? clientB : clientA;
	}

	public void nextCommand(final JSONObject cmd, final Client player) {
		if (out != null)
			out.println(player.getClientName() + "/" + cmd.toString());
	}

	public void close() {
		out.close();
	}
}

