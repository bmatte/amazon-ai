package amazon.board;

import java.util.ArrayList;

/**
 * "Game of Amazons" board model, modeling board positions and checking for
 * valid moves.
 */
public interface BoardModel {

	// Empty.
	byte E = 0;
	// Black queen.
	byte B = 1;
	// White queen.
	byte W = 2;
	// Black arrow.
	byte AB = 3;
	// White arrow.
	byte AW = 4;

	/**
	 * Validate a given move.
	 * 
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
	 * @return Validity of move positions.
	 */
	boolean validMove(int rQI, int cQI, int rQF, int cQF, int rA, int cA);

	/**
	 * Perform a move, after determining its validity.
	 * 
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
	boolean move(int rQI, int cQI, int rQF, int cQF, int rA, int cA);

	/**
	 * @return List of possible moves.
	 */
	ArrayList<int[]> possibleMoves();

	/**
	 * @param r
	 *            Row of position.
	 * @param c
	 *            Column of position.
	 * @return The value of a given board position.
	 */
	int get(int r, int c);

	/** @return The row count of board. */
	int getRowCount();

	/** @return The column count of board. */
	int getColumnCount();

	/** Get the current state of the board. */
	byte[][] getState();

}