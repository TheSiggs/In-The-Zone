package inthezone.server;

import inthezone.battle.data.GameDataFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class Multiplexer implements Runnable {
	// Clients that are in the process of connecting to the server
	private final Collection<Client> pendingClients = new LinkedList<>();
	// Clients with names on the server
	private final Map<String, Client> namedClients = new HashMap<>();
	// All clients currently connected to the server
	private final Map<UUID, Client> sessions = new HashMap<>();

	private final GameDataFactory dataFactory;
	private Selector selector;

	private final ServerSocketChannel serverSocket;
	private final SelectionKey serverKey;

	public Multiplexer(int port, int backlog, GameDataFactory dataFactory)
		throws IOException
	{
		this.dataFactory = dataFactory;
		this.selector = Selector.open();

		this.serverSocket = ServerSocketChannel.open();
		serverSocket.bind(new InetSocketAddress(port), backlog);
		serverSocket.configureBlocking(false);
		serverKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
	}

	private boolean killThread = false;

	@Override
	public void run() {
		while (!killThread) {
			System.out.println("In multiplexer");
			try {
				doSelect();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				if (!selector.isOpen()) restoreSelector();
			}
		}
	}

	private void restoreSelector() {
		try {
			selector = Selector.open();
			for (Client c : pendingClients) {
				c.closeConnection(false);
			}
			Collection<Client> cannotReset = new LinkedList<>();
			for (Client c : namedClients.values()) {
				try {
					c.resetSelector(selector);
				} catch (IOException e) {
					cannotReset.add(c);
				}
			}
			for (Client c : cannotReset) {
				if (!c.isDisconnected()) {
					c.closeConnection(false);
				}
			}
		} catch (IOException e) {
			System.err.println("Cannot get selector, quitting");
			killThread = true;
		}
	}

	private void doSelect() throws IOException {
		System.out.println("blocking");
		selector.select();
		System.out.println("unblocked");

		for (SelectionKey k : selector.keys()) {
			if (k == serverKey) {
				SocketChannel connection = serverSocket.accept();
				if (connection != null) newClient(connection);
				break;
			}

			Client c = (Client) k.attachment();
			if (k.isValid()) {
				if (k.isReadable()) c.receive();
				if (k.isWritable()) c.send();
			} else {
				// clean up this client if necessary.
				k.cancel();
				if (c.isDisconnected()) {
					if (c.isDisconnectedTimeout()) removeClient(c);
				} else {
					c.closeConnection(false);
				}
			}
		}

		// clean up any clients which have quietly dropped their connections.
		for (Client c : sessions.values()) {
			if (!c.isConnected()) c.closeConnection(false);
			if (c.isDisconnectedTimeout()) removeClient(c);
		}
	}

	private void removeClient(Client c) {
		sessions.remove(c);
		namedClients.remove(c);
		pendingClients.remove(c);
	}

	private void newClient(SocketChannel connection) {
		try {
			pendingClients.add(new Client(
				connection, selector, namedClients,
				pendingClients, sessions, dataFactory));
		} catch (IOException e) {
			try {
				connection.close();
			} catch (IOException e2) {
				/* Doesn't matter */
			}
		}
	}
}

