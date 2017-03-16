package amazon;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import amazon.board.BoardArray;
import amazon.board.BoardModel;
import amazon.client.ServerClient;
import amazon.client.SmartFoxClient;
import amazon.client.SmartFoxLobby;
import amazon.client.SmartFoxLobbyConsole;
import amazon.view.BoardASCII;
import amazon.view.BoardView;
import amazon.view.BoardWindow;

/**
 * Game of Amazons class, managing board model, optional online client, and
 * optional board view.
 */
public class Game {
	// Amazon board model.
	private BoardModel board;
	// Client for online play.
	private ServerClient client;
	// Board view.
	private BoardView view;

	/**
	 * Create a new game.
	 * 
	 * @param viewOption
	 *            0: no view. 1: ASCII print view. 2: Window view.
	 * @param user
	 *            Server user name.
	 * @param pass
	 *            Server user password.
	 */
	public Game(int viewOption, String user, String pass) {
		// Create new board model.
		board = new BoardArray();

		// Check if view should be shown.
		if (viewOption == 1) {
			// Create board view.
			view = new BoardASCII(board);
			view.repaint();
		} else if (viewOption == 2) {
			// Create new window event.
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						// Create board view.
						view = new BoardWindow(board, true, false, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

		if (user.length() > 0) {
			SmartFoxLobby lobby = new SmartFoxLobbyConsole();
			client = new SmartFoxClient(user, pass, this, lobby);
		}

		// Wait until client assigns a player color.
		if (client != null)
			while (client.isBlackPlayer() == null) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
				}
			}

		// Null client implies simulation mode.
		boolean simulate = client == null;
		// Time to delay turns and next game for simulations.
		int simTurnWait = 100;
		int simGameEndWait = 1000;

		// Play one game, and repeat if set to simulate.
		do {
			// Max possible number of moves is 92.
			for (int i = 0; i < 92; i++) {
				// Wait while it's the other (online) player's turn.
				if (client != null)
					while (board.getTurn() != client.isBlackPlayer()) {
						try {
							TimeUnit.MILLISECONDS.sleep(100);
						} catch (InterruptedException e) {
						}
					}
				// Get list of possible moves.
				ArrayList<int[]> possibleMoves = board.possibleMoves();
				if (possibleMoves.size() > 0) {
					int[] m = possibleMoves.get((int) (possibleMoves.size() * Math.random()));
					boolean moveM = move(true, m[0], m[1], m[2], m[3], m[4], m[5]);
					// Print move information if move was invalid.
					if (!moveM)
						System.err.println(
								"{" + m[0] + "," + m[1] + "," + m[2] + "," + m[3] + "," + m[4] + "," + m[5] + "}");
					// Repaint if a view exists.
					if (view != null)
						view.repaint();
					// Stop if game is finished.
					if (board.checkFinished())
						break;
					// Wait at end of turn, for testing.
					if (simulate)
						try {
							TimeUnit.MILLISECONDS.sleep(simTurnWait);
						} catch (InterruptedException e) {
						}
				}
			}

			if (simulate) {
				// Wait at end of game simulation.
				try {
					TimeUnit.MILLISECONDS.sleep(simGameEndWait);
				} catch (InterruptedException e) {
				}
				board.reinitialize();
				if (view != null)
					view.repaint();
			}
		} while (simulate);
	}

	/**
	 * Create a new offline game.
	 * 
	 * @param viewOption
	 *            0: no view. 1: ASCII print view. 2: Window view.
	 */
	public Game(int show) {
		this(show, "", "");
	}

	/**
	 * Create a new offline game, using ASCII view.
	 */
	public Game() {
		this(1, "", "");
	}

	/**
	 * Perform a move.
	 * 
	 * @param player
	 *            Player or opponent move.
	 * @param rQI
	 *            Initial queen row index.
	 * @param cQI
	 *            Initial queen column index.
	 * @param rQF
	 *            Final queen row index.
	 * @param cQF
	 *            Final queen column index.
	 * @param rA
	 *            Arrow row index.
	 * @param cA
	 *            Arrow column index.
	 * @return Whether move was performed.
	 */
	public boolean move(boolean player, int rQI, int cQI, int rQF, int cQF, int rA, int cA) {
		// Attempt move on board and check its validity.
		if (!board.move(rQI, cQI, rQF, cQF, rA, cA)) {
			// Throw exception if server issues illegal move.
			if (!player && client != null)
				throw new IllegalArgumentException("Server issued illegal move!");
			return false;
		}
		// Send move to server if online, and not opponent turn.
		if (player && client != null)
			client.sendMove(rQI, cQI, rQF, cQF, rA, cA);
		// Update GUI if shown.
		if (view != null)
			view.repaint();
		// Succeeded.
		return true;
	}

	/**
	 * Get the game's board model.
	 * 
	 * @return This games's board model.
	 */
	public BoardModel getBoard() {
		return board;
	}

	/** Create and run the game. */
	public static void main(String[] args) {
		Game g = null;
		if (args.length == 1)
			g = new Game(2, args[0], args[0]);
		else
			g = new Game(2);
	}
}
