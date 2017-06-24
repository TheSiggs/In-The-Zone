package inthezone.server;

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

import inthezone.Log;
import inthezone.battle.data.GameDataFactory;

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

	private final int maxClients;

	public Multiplexer(
		String name, int port, int backlog,
		int maxClients, GameDataFactory dataFactory
	) throws IOException {
		this.name = name;
		this.maxClients = maxClients;

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
			try {
				doSelect();
			} catch (Exception e) {
				Log.error("IO error in multiplexer", e);
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
			Log.fatal("Cannot get selector, quitting", e);
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
		selector.select(timeout);

		// do IO operations
		for (
			Iterator<SelectionKey> skeys = selector.selectedKeys().iterator();
			skeys.hasNext();
		) {
			final SelectionKey k = skeys.next();

			if (k == serverKey) {
				if (k.isAcceptable()) {
					final SocketChannel connection = serverSocket.accept();
					if (connection != null) newClient(connection);
					skeys.remove();
				}

			} else {
				final Client c = (Client) k.attachment();
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
				Log.warn("Dropped connection to " +
					c.name.orElse("<UNNAMED CLIENT>"), null);
				disconnectedClients.add(c);
			}
		}

		for (Client c : expiredClients) removeClient(c);
		for (Client c : disconnectedClients) c.closeConnection(false);
	}

	private void removeClient(Client c) {
		Log.info("Removing client " +
			c.name.orElse("<UNNAMED CLIENT>"), null);
		c.closeConnection(true);
	}

	private void newClient(SocketChannel connection) {
		try {
			if (sessions.size() >= maxClients) {
				// if this happens, assume we're under attack and just close the
				// connection.
				Log.warn("Refused connection to " +
					connection.getRemoteAddress().toString() +
					", max clients (" + maxClients + ") reached", null);

				connection.close();
			} else {
				Log.info("Client pending from " +
					connection.getRemoteAddress().toString(), null);
				pendingClients.add(new Client(
					name, connection, selector, namedClients,
					pendingClients, sessions, dataFactory));
			}
		} catch (IOException e) {
			try {
				Log.error("IO error receiving connection from " +
					connection.getRemoteAddress().toString(), e);
			} catch (IOException e2) {
				Log.error("IO error receiving connection from unknown address (" +
					e2.getMessage() + ") ", e);
			}

			try {
				connection.close();
			} catch (IOException e2) {
				/* Doesn't matter */
			}
		}
	}
}

