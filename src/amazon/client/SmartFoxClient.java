package amazon.client;

import java.util.ArrayList;
import java.util.Map;

import amazon.Game;
import ygraphs.ai.smart_fox.GameMessage;
import ygraphs.ai.smart_fox.games.AmazonsGameMessage;
import ygraphs.ai.smart_fox.games.GameClient;
import ygraphs.ai.smart_fox.games.GamePlayer;

/**
 * Client for Yong Gao's Smart Fox server implementation.
 */
public class SmartFoxClient extends GamePlayer implements ServerClient {
	// Game client for server.
	private GameClient gameClient;
	// User name being used.
	private String userName;
	// Amazon game.
	private Game game;
	// Whether this player is black.
	private Boolean blackPlayer;
	// Smart Fox lobby room selection.
	private SmartFoxLobby lobby;

	/**
	 * Create a new amazon smart fox game client.
	 * 
	 * @param userName
	 *            Server user name.
	 * @param pass
	 *            Server login.
	 */
	public SmartFoxClient(String userName, String pass, Game game, SmartFoxLobby lobby) {
		this.userName = userName;
		gameClient = new GameClient(userName, pass, this);
		this.game = game;
		this.lobby = lobby;
		lobby.setClient(this);
	}

	@Override
	/**
	 * Invoked by the game client upon successfully login.
	 */
	public void onLogin() {
		if (lobby != null)
			lobby.receiveGameList(gameClient.getRoomList());
	}

	@Override
	/**
	 * Invoked by the game client for receiving game messages, while in a game
	 * room.
	 * 
	 * @param messageType
	 *            The type of the message.
	 * @param msgDetails
	 *            Message info and data about a game action.
	 */
	public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {

		// Properly handle message by type.
		if (messageType.equals(GameMessage.GAME_ACTION_START)) {

			if (msgDetails.get("player-black").equals(this.userName()))
				// This player is black.
				blackPlayer = true;
			else
				// This player is white.
				blackPlayer = false;

		} else if (messageType.equals(GameMessage.GAME_ACTION_MOVE)) {
			handleOpponentMove(msgDetails);
		}
		return true;
	}

	/**
	 * Get this client's user name.
	 */
	@Override
	public String userName() {
		return userName;
	}

	/**
	 * Handle an opponent move received from server.
	 * 
	 * @param msgDetails
	 *            Details of game message for opponent move.
	 */
	private void handleOpponentMove(Map<String, Object> msgDetails) {
		// Get move details.
		Object qI = msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
		Object qF = msgDetails.get(AmazonsGameMessage.Queen_POS_NEXT);
		Object a = msgDetails.get(AmazonsGameMessage.ARROW_POS);
		// Check that each detail is in fact an array list.
		if (qI instanceof ArrayList<?> && qF instanceof ArrayList<?> && a instanceof ArrayList<?>) {
			Object rQI = ((ArrayList<?>) qI).get(0);
			Object cQI = ((ArrayList<?>) qI).get(1);
			Object rQF = ((ArrayList<?>) qF).get(0);
			Object cQF = ((ArrayList<?>) qF).get(1);
			Object rA = ((ArrayList<?>) a).get(0);
			Object cA = ((ArrayList<?>) a).get(1);
			// Check that each object is in fact an integer.
			if (rQI instanceof Integer && cQI instanceof Integer && rQF instanceof Integer && cQF instanceof Integer
					&& rA instanceof Integer && cA instanceof Integer) {
				// Perform move on board model, and throw an exception if it is
				// invalid for the given board.
				receiveMove((Integer) rQI, (Integer) cQI, (Integer) rQF, (Integer) cQF, (Integer) rA, (Integer) cA);
			} else {
				throw new IllegalArgumentException("Server message detail is not an integer!");
			}
		} else {
			throw new IllegalArgumentException("Server message detail is not an array list!");
		}
	}

	/**
	 * Join a game room.
	 * 
	 * @param index
	 *            Index of room to join.
	 */
	public void joinRoom(int index) {
		this.gameClient.joinRoom(gameClient.getRoomList().get(index));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.client.ServerClient#sendMove(int, int, int, int, int, int)
	 */
	public void sendMove(int rQI, int cQI, int rQF, int cQF, int rA, int cA) {
		System.out.println("sendMove " + rQI + " " + cQI + " " + rQF + " " + cQF + " " + rA + " " + cA);
		// Change index convention.
		rQI = rTo(rQI);
		rQF =  rTo(rQF);
		rA =  rTo(rA);
		cQI =  cTo(cQI);
		cQF =  cTo(cQF);
		cA =  cTo(cA);
		System.out.println("sendMove " + rQI + " " + cQI + " " + rQF + " " + cQF + " " + rA + " " + cA);
		this.gameClient.sendMoveMessage(new int[] { rQI, cQI }, new int[] { rQF, cQF }, new int[] { rA, cA });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.client.ServerClient#receiveMove(int, int, int, int, int, int)
	 */
	public void receiveMove(int rQI, int cQI, int rQF, int cQF, int rA, int cA) {
		System.out.println("receiveMove " + rQI + " " + cQI + " " + rQF + " " + cQF + " " + rA + " " + cA);
		// Change index convention.
		rQI = rFrom(rQI);
		rQF =  rFrom(rQF);
		rA =  rFrom(rA);
		cQI =  cFrom(cQI);
		cQF =  cFrom(cQF);
		cA =  cFrom(cA);
		System.out.println("receiveMove " + rQI + " " + cQI + " " + rQF + " " + cQF + " " + rA + " " + cA);
		if (!game.move(false, rQI, cQI, rQF, cQF, rA, cA))
			throw new IllegalArgumentException("Move from server is invalid on board!");
	}

	/** Convert row index to client row index. */
	private int rTo(int y) {
		return y+1;
	}
	
	/** Convert column index to client column index. */
	private int cTo(int x) {
		return 10-x;
	}

	/** Convert client row index to row index. */
	private int rFrom(int y) {
		return y-1;
	}

	/** Convert client column index to column index. */
	private int cFrom(int x) {
		return 10-x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.client.ServerClient#isBlackPlayer()
	 */
	@Override
	public Boolean isBlackPlayer() {
		return blackPlayer;
	}
}