package amazon;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import amazon.board.BoardArray;
import amazon.board.BoardModel;
import amazon.client.ServerClient;
import amazon.client.SmartFoxClient;
import amazon.view.BoardASCII;
import amazon.view.BoardView;
import amazon.view.BoardWindow;

/**
 * Game of Amazons class, managing board model, optional online client, and
 * optional board view.
 */
public class Game {
	// Amazon board model.
	BoardModel board;
	// Client for online play.
	ServerClient client;
	// Board view.
	BoardView view;

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
		final BoardModel boardModel = new BoardArray(); 

		// Check if view should be shown.
		if (viewOption == 1) {
			// Create board view.
			view = new BoardASCII(boardModel);
			view.repaint();
		} else if (viewOption == 2) {
			// Create new window event.
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						// Create board view.
						view = new BoardWindow(boardModel, true, false, true); //error when BoardModel is not final
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		if (user.length() > 0)
			// TODO point to lobby object here.
			client = new SmartFoxClient(user, pass, this, null);

		// XXX Random move testing.
		boolean simulate = true;
		breakLabel: while (simulate) {
			for (int i = 0; i < 1024; i++) {
				ArrayList<int[]> possibleMoves = boardModel.possibleMoves();
				if (possibleMoves.size() > 0) {
					int[] m = possibleMoves.get((int) (possibleMoves.size() * Math.random()));

					// if (!boardModel.getTurn())
					// // Move on to next queen if in its own chamber.
					// if ((boardModel.getChambers()[1][m[0]][m[1]] == 0
					// && boardModel.getChambers()[2][m[0]][m[1]] > 0)
					// || (boardModel.getChambers()[1][m[0]][m[1]] > 0
					// && boardModel.getChambers()[2][m[0]][m[1]] == 0))
					// continue;

					boolean move = boardModel.move(m[0], m[1], m[2], m[3], m[4], m[5]);
					// System.out.println("(" + possibleMoves.size() + ")" +
					// move);
					if (!move) {
						System.out.println(m[0] + " " + m[1] + " " + m[2] + " " + m[3] + " " + m[4] + " " + m[5]);
						break breakLabel;
					}
					if (view != null)
						view.repaint();
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
					}
					if (boardModel.checkFinished())
						break;
				}
			}

			System.out.println(boardModel.getPoints()[0][0] + "," + boardModel.getPoints()[1][0] + ","
					+ boardModel.getPoints()[0][1] + "," + boardModel.getPoints()[1][1]);

			try {
				TimeUnit.MILLISECONDS.sleep(5000);
			} catch (InterruptedException e) {
			}
			boardModel.reinitialize();
			if (view != null)
				view.repaint();
		}
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
		Game g = new Game(2, "group5", "group5");
	}
}
