package amazon.board;

import java.util.ArrayList;

/**
 * "Game of Amazons" board model, modeling board locations and checking for
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
	 * Validate a given player turn, i.e. queen move and arrow.
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
	 * @return Validity of move locations.
	 */
	boolean validTurn(int rQI, int cQI, int rQF, int cQF, int rA, int cA);

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
	 *            Row of location.
	 * @param c
	 *            Column of location.
	 * @return The value of a given board location.
	 */
	int get(int r, int c);

	/** @return The row count of board. */
	int getRowCount();

	/** @return The column count of board. */
	int getColumnCount();

	/** Get the current state of the board. */
	byte[][] getState();

	/**
	 * Return three board array representations for chamber ownership. First
	 * array represents all found chambers with unique labels, while second and
	 * third arrays represent the number of black and white queens contained in
	 * each chamber.
	 * 
	 * @return Validity of piece move.
	 */
	public byte[][][] findChambers();

}