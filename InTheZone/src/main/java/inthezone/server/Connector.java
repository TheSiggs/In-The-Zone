package inthezone.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector implements Runnable {
	private final ServerSocket socket;
	private final Multiplexer multiplexer;

	public Connector(
		int port, int backlog, Multiplexer multiplexer
	) throws IOException {
		this.socket = new ServerSocket(port, backlog);
		this.multiplexer = multiplexer;
	}

	public void run() {
		while (true) {
			Socket s = null;
			try {
				s = socket.accept();
				multiplexer.newClient(s);
			} catch (IOException e) {
				System.err.println(
					"IOException listening on socket: " + e.getMessage());
				try {
					if (s != null) s.close();
				} catch (IOException e2) {
					System.err.println(
						"IOException closing socket: " + e2.getMessage());
				}
			}
		}
	}
}

