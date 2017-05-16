package inthezone.server;

import inthezone.battle.data.GameDataFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class Multiplexer implements Runnable {
	private final static long timeout = 30 * 1000;

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

	private final String name;

	public Multiplexer(
		String name, int port, int backlog, GameDataFactory dataFactory
	) throws IOException {
		this.name = name;

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
				e.printStackTrace(System.err);
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

	int debugCounter = 0;

	final Collection<Client> expiredClients = new ArrayList<>();
	final Collection<Client> disconnectedClients = new ArrayList<>();

	/**
	 * Block until an IO operation is ready to run.
	 * */
	private void doSelect() throws IOException {
		System.out.println("blocking " + (debugCounter++));
		selector.select(timeout);
		System.out.println("unblocked");

		// do IO operations
		for (
			Iterator<SelectionKey> skeys = selector.selectedKeys().iterator();
			skeys.hasNext();
		) {
			SelectionKey k = skeys.next();

			if (k == serverKey) {
				if (k.isAcceptable()) {
					SocketChannel connection = serverSocket.accept();
					if (connection != null) newClient(connection);
					skeys.remove();
				}

			} else {
				Client c = (Client) k.attachment();
				if (k.isValid() && k.isWritable()) c.send();
				if (k.isValid() && k.isReadable()) c.receive();
				skeys.remove();
			}
		}

		// cancel keys for closed connections.
		for (SelectionKey k : selector.keys()) if (!k.isValid()) k.cancel();

		// clean up any clients which have quietly dropped their connections.
		expiredClients.clear();
		disconnectedClients.clear();
		for (Client c : sessions.values()) {
			if (c.isDisconnected()) {
				if (c.isDisconnectedTimeout()) expiredClients.add(c);
			} else if (!c.isConnected()) {
				System.err.println("Dropped connection");
				disconnectedClients.add(c);
			}
		}

		for (Client c : expiredClients) removeClient(c);
		for (Client c : disconnectedClients) c.closeConnection(false);
	}

	private void removeClient(Client c) {
		System.err.println("Removing client " + c);
		c.closeConnection(true);
	}

	private void newClient(SocketChannel connection) {
		try {
			pendingClients.add(new Client(
				name, connection, selector, namedClients,
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

