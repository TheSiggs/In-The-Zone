package inthezone.protocol;

/**
 * A client can be in one of several states.  When a client connects to the
 * server, it starts in the HANDSHAKE state.  After negotiating with the server
 * it enters the NAMING state, in which the client can request a player name on
 * the server.  When the server accepts the client's player name, it enters the
 * LOBBY state.  When a client enters the game queue, it moves to the QUEUE
 * state.  When a client enters a battle, it moves to the GAME state.  If the
 * connection to a client closes at any time then the client moves to the
 * DISCONNECTED state.
 * */
public enum ClientState {
	HANDSHAKE, NAMING, LOBBY, QUEUE, GAME, DISCONNECTED;
}

