package inthezone.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import inthezone.battle.data.GameDataFactory;
import inthezone.util.Log;

/**
 * The IO multiplexer.
 * */
public class Multiplexer implements Runnable {
	private final static long timeout = 30 * 1000;

	// Clients that are in the process of connecting to the server
	private final Collection<Client> pendingClients = new LinkedList<>();
	// Clients with names on the server
	private final Map<String, Client> namedClients = new HashMap<>();
	// All clients currently connected to the server
	private final Map<UUID, Client> sessions = new HashMap<>();
	// The queue of clients waiting for automatic matchmaking
	private final List<GameQueueElement> gameQueue = new ArrayList<>();

	private final GameDataFactory dataFactory;
	private Selector selector;

	private final ServerSocketChannel serverSocket;
	private final SelectionKey serverKey;

	private final String name;

	private final int maxClients;

	/**
	 * @param name the name of the server
	 * @param port the port to listen on
	 * @param backlog the server socket backlog value
	 * @param maxClients the maximum number of clients that may connect
	 * @param dataFactory the game data
	 * */
	public Multiplexer(
		final String name,
		final int port,
		final int backlog,
		final int maxClients,
		final GameDataFactory dataFactory
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

	/**
	 * Start the multiplexer.
	 * */
	@Override public void run() {
		while (!killThread) {
			try {
				doSelect();
			} catch (final Exception e) {
				Log.error("IO error in multiplexer", e);
				if (!selector.isOpen()) restoreSelector();
			}
		}
	}

	/**
	 * Create a new Selector.  This method is used to recover from an error which
	 * shut down the Selector.
	 * */
	private void restoreSelector() {
		try {
			selector = Selector.open();
			for (final Client c : pendingClients) {
				c.closeConnection(false);
			}
			final Collection<Client> cannotReset = new LinkedList<>();
			for (final Client c : namedClients.values()) {
				try {
					c.resetSelector(selector);
				} catch (final IOException e) {
					cannotReset.add(c);
				}
			}
			for (final Client c : cannotReset) {
				if (!c.isDisconnected()) {
					c.closeConnection(false);
				}
			}
		} catch (final IOException e) {
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
					connection.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
					connection.setOption(StandardSocketOptions.TCP_NODELAY, true);
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
		for (final SelectionKey k : selector.keys()) if (!k.isValid()) k.cancel();

		// clean up any clients which have quietly dropped their connections.
		expiredClients.clear();
		disconnectedClients.clear();
		for (final Client c : sessions.values()) {
			if (c.isDisconnected()) {
				if (c.isDisconnectedTimeout()) expiredClients.add(c);
			} else if (!c.isConnected()) {
				Log.warn("Dropped connection to " +
					c.name.orElse("<UNNAMED CLIENT>"), null);
				disconnectedClients.add(c);
			}
		}

		for (final Client c : expiredClients) removeClient(c);
		for (final Client c : disconnectedClients) c.closeConnection(false);
	}

	/**
	 * Remove a client from set of logged in clients
	 * @param c the client to remove
	 * */
	private void removeClient(final Client c) {
		Log.info("Removing client " +
			c.name.orElse("<UNNAMED CLIENT>"), null);
		c.closeConnection(true);
	}

	/**
	 * Initialise a new client
	 * @param connection the connection the client is connecting on.
	 * */
	private void newClient(final SocketChannel connection) {
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
					pendingClients, sessions, gameQueue, dataFactory));
			}
		} catch (final IOException e) {
			try {
				Log.error("IO error receiving connection from " +
					connection.getRemoteAddress().toString(), e);
			} catch (final IOException e2) {
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

