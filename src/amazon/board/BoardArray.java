package amazon.board;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Array board model of the "Game of Amazons", modeling board positions and
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#validMove(int, int, int, int, int, int)
	 */
	@Override
	public boolean validMove(int rQI, int cQI, int rQF, int cQF, int rA, int cA) {
		// Check if initial position isn't current player's queen.
		if (board[rQI][cQI] != (whiteTurn ? W : B))
			return false;
		// Check if final position isn't empty.
		if (board[rQF][cQF] != E)
			return false;
		// Check if arrow position isn't empty.
		if (board[rA][cA] != E)
			return false;
		// TODO Check if queen final and arrow locations are valid move.
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#move(int, int, int, int, int, int)
	 */
	@Override
	public boolean move(int rQI, int cQI, int rQF, int cQF, int rA, int cA) {
		// Check if move is invalid.
		if (!validMove(rQI, cQI, rQF, cQF, rA, cA))
			return false;
		// Move whatever queen to new position.
		board[rQF][cQF] = board[rQI][cQI];
		// Make initial position empty.
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
