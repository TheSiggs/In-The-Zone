package inthezone.game;

import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.util.DefaultServerConfig;
import isogame.engine.KeyBindingTable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ssjsjs.annotations.As;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.Implicit;
import ssjsjs.annotations.JSONConstructor;
import ssjsjs.JSONable;

/**
 * The configuration data loaded from file.
 * */
public class ClientConfigData implements JSONable {
	private final GameDataFactory gameData;
	private final KeyBindingTable defaultKeyBindingTable;

	public final Optional<String> defaultPlayerName;
	public final String server;
	public final int port;
	public final List<Loadout> loadouts = new ArrayList<>();
	public final KeyBindingTable keyBindings = new KeyBindingTable();

	public ClientConfigData(
		final GameDataFactory gameData,
		final KeyBindingTable defaultKeyBindingTable
	) {
		this(gameData, defaultKeyBindingTable, Optional.empty(), null, null, new ArrayList<>(), null);
	}

	@JSONConstructor
	public ClientConfigData(
		@Implicit("gameData") final GameDataFactory gameData,
		@Implicit("defaultKeyBindingTable") final KeyBindingTable defaultKeyBindingTable,
		@Field("defaultPlayerName")@As("name") final Optional<String> defaultPlayerName,
		@Field("server") final String server,
		@Field("port") final Integer port,
		@Field("loadouts") final List<Loadout> loadouts,
		@Field("keyBindings")@As("keys") final KeyBindingTable keyBindings
	) {
		this.gameData = gameData;
		this.defaultKeyBindingTable = defaultKeyBindingTable;
		this.defaultPlayerName = defaultPlayerName;
		this.server = server == null? DefaultServerConfig.DEFAULT_SERVER : server;
		this.port = port == null? DefaultServerConfig.DEFAULT_PORT : port;
		this.loadouts.addAll(loadouts);
		this.keyBindings.loadBindings(keyBindings == null? defaultKeyBindingTable : keyBindings);
	}

	/**
	 * Set the server
	 * @param server the new server
	 * @return the new configuration data
	 * */
	public ClientConfigData setServer(final String server) {
		return new ClientConfigData(gameData, defaultKeyBindingTable, defaultPlayerName, server, port, loadouts, keyBindings);
	}

	/**
	 * Set the server port
	 * @param port the new server port
	 * @return the new configuration data
	 * */
	public ClientConfigData setPort(final int port) {
		return new ClientConfigData(gameData, defaultKeyBindingTable, defaultPlayerName, server, port, loadouts, keyBindings);
	}
}

