package amazon.board;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Array board model of the "Game of Amazons", modeling board locations and
 * checking for valid moves.
 */
// TODO check for win state.
// TODO check for player turn.
public class BoardArray implements BoardModel {
	// Board representation.
	private byte[][] board;
	// White or black turn.
	private boolean whiteTurn;

	// /**
	// * Representation of Amazon board.
	// *
	// * @param rows
	// * Number of board rows.
	// * @param columns
	// * Number of board columns.
	// */
	// public AmazonBoardModel(int rowCount, int columnCount) {
	// // Create new empty board array.
	// board = new byte[rowCount][columnCount];
	// // Add initiaL queens.
	// board[6][0] = B;
	// board[6][9] = B;
	// board[9][3] = B;
	// board[9][6] = B;
	// board[0][3] = W;
	// board[0][6] = W;
	// board[3][0] = W;
	// board[3][9] = W;
	// }

	/**
	 * Representation of Amazon board.
	 */
	public BoardArray() {
		// Create new empty board array.
		board = new byte[10][10];
		// Add initiaL queens.
		board[6][0] = B;
		board[6][9] = B;
		board[9][3] = B;
		board[9][6] = B;
		board[0][3] = W;
		board[0][6] = W;
		board[3][0] = W;
		board[3][9] = W;

		// Arrow testing.
		for (int i = 0; i < 50; i++)
			board[(int) (Math.random() * 10)][(int) (Math.random() * 10)] = (Math.random() > 0.5 ? AB : AW);

		// White goes first.
		whiteTurn = true;
	};

	private BoardArray(byte[][] board, boolean whiteTurn) {
		this.board = board;
		this.whiteTurn = whiteTurn;
	}

	/**
	 * Validate a given queen move, without checking arrow.
	 * 
	 * @param rQI
	 *            Initial queen row index.
	 * @param cQI
	 *            Initial queen column index.
	 * @param rQF
	 *            Final queen row index.
	 * @param cQF
	 *            Final queen column index.
	 * @return Validity of queen move.
	 */
	public boolean validQueen(int rQI, int cQI, int rQF, int cQF) {
		// Check if initial location isn't current player's queen.
		if (board[rQI][cQI] != (whiteTurn ? W : B))
			return false;
		// Check if final location isn't empty.
		if (board[rQF][cQF] != E)
			return false;
		// Check queen move path.
		if (!validMove(rQI, cQI, cQF, cQF))
			return false;
		return true;
	}

	/**
	 * Validate a given arrow, assuming move is valid.
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
	 * @return Validity of arrow move.
	 */
	public boolean validArrow(int rQI, int cQI, int rQF, int cQF, int rA, int cA) {
		// Check if arrow location isn't empty, and allow initial queen
		// location.
		if (board[rA][cA] != E && !(rA == rQI && cA == cQI))
			return false;
		// Check arrow move path.
		if (!validMove(rQF, cQF, rA, cA))
			return false;
		return true;
	}

	/**
	 * Validate a given piece move path, be it queen or arrow. Assumes
	 * destination spot is empty and will not check it.
	 * 
	 * @param rQI
	 *            Initial row index.
	 * @param cQI
	 *            Initial column index.
	 * @param rQF
	 *            Final row index.
	 * @param cQF
	 *            Final column index.
	 * @return Validity of piece move.
	 */
	public boolean validMove(int rI, int cI, int rF, int cF) {
		// Check if vertical move.
		if (cI == cF) {
			// Up or down.
			int d = rF < rI ? -1 : 1;
			// Check locations between start and destination.
			for (int r = 0; r < Math.abs(rF - rI) - 1; r++)
				if (board[rI + d * r][cI] != E)
					return false;
			// Check if horizontal move.
		} else if (rI == rF) {
			// Left or right.
			int d = cF < cI ? -1 : 1;
			// Check locations between start and destination.
			for (int c = 0; c < Math.abs(cF - cI) - 1; c++)
				if (board[rI][cI + d * c] != E)
					return false;
			// Check if upward sloping diagonal move.
		} else if (rF - rI == -(cF - cI)) {
			// Left or right.
			int d = cF < cI ? -1 : 1;
			// Check locations between start and destination.
			for (int c = 0; c < Math.abs(cF - cI) - 1; c++)
				if (board[rI - d * c][cI + d * c] != E)
					return false;
			// Check if downward sloping diagonal move.
		} else if (rF - rI == cF - cI) {
			// Left or right.
			int d = cF < cI ? -1 : 1;
			// Check locations between start and destination.
			for (int c = 0; c < Math.abs(cF - cI) - 1; c++)
				if (board[rI + d * c][cI + d * c] != E)
					return false;
		}
		// Move cannot be valid.
		return false;
	}

	@Override
	public boolean validTurn(int rQI, int cQI, int rQF, int cQF, int rA, int cA) {
		return validQueen(rQI, cQI, rQF, cQF) && validArrow(rQI, cQI, rQF, cQF, rA, cA);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#move(int, int, int, int, int, int)
	 */
	@Override
	public boolean move(int rQI, int cQI, int rQF, int cQF, int rA, int cA) {
		// Check if move is invalid.
		if (!validTurn(rQI, cQI, rQF, cQF, rA, cA))
			return false;
		// Move whatever queen to new location.
		board[rQF][cQF] = board[rQI][cQI];
		// Make initial location empty.
		board[rQI][cQI] = E;
		// Place arrow.
		board[rA][cA] = E;
		// Succeeded.
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#possibleMoves()
	 */
	@Override
	public ArrayList<int[]> possibleMoves() {
		ArrayList<int[]> moves = new ArrayList<>();

		return moves;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String output = "";
		for (int i = 0; i < board.length; i++)
			for (int j = 0; j < board[i].length; j++)
				output += board[i][j] + (j == board[i].length - 1 ? "\n" : " ");
		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#get(int, int)
	 */
	@Override
	public int get(int r, int c) {
		return board[r][c];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return board.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return board[0].length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#getState()
	 */
	@Override
	public byte[][] getState() {
		byte[][] state = new byte[board.length][];
		for (int i = 0; i < board.length; i++)
			state[i] = board[i].clone();
		return state;
	}

	/** Create a clone of this board. */
	public BoardModel clone() {
		return new BoardArray(getState(), whiteTurn);
	}

	/** Test initial board layout. */
	public static void main(String[] args) {
		BoardModel model = new BoardArray();
		System.out.println(model.toString());
	}
}
