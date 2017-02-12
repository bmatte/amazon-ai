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
	// Chambers representation. Will be null when un-calculated.
	private byte[][][] chambers;

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
		for (int i = 0; i < 80; i++)
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
	 * @see amazon.board.BoardModel#findChambers()
	 */
	public byte[][][] getChambers() {
		// Don't recalculate if not needed.
		if (chambers != null)
			return chambers;
		// Initialize chamber labels and count as 0.
		byte[][][] chambers = new byte[3][getRowCount()][getColumnCount()];
		// TODO Possibly make chamber queen counts initialized at -1 for
		// non-chambers.
		// Checked board locations.
		boolean[][] checked = new boolean[getRowCount()][getColumnCount()];
		// Chamber label, reserving 0 for non-chambers.
		byte label = 1;
		// Scan through all locations.
		for (int i = 0; i < getRowCount(); i++) {
			for (int j = 0; j < getColumnCount(); j++) {
				// Don't re-check locations, or arrow occupied locations.
				if (checked[i][j] || board[i][j] == AB || board[i][j] == AW)
					continue;
				// List of locations to check.
				ArrayList<int[]> check = new ArrayList<>();
				// List of found chamber locations.
				ArrayList<int[]> found = new ArrayList<>();
				// Number of queens found in current chamber.
				byte bCount = 0;
				byte wCount = 0;
				// Start at current scan location.
				check.add(new int[] { i, j });

				// Check location and spread out if empty.
				while (check.size() > 0) {
					// Get location to check.
					int[] l = check.remove(0);
					// Get board value at check location.
					byte value = board[l[0]][l[1]];

					// Check if empty or queen.
					if (value == E || value == B || value == W) {
						// Add location to found list.
						found.add(new int[] { l[0], l[1] });
						// Increase count if queen is found.
						if (value == B)
							bCount++;
						else if (value == W)
							wCount++;

						// Add neighbor locations if empty and unchecked.
						for (int rN = -1; rN <= 1; rN++) {
							// Check for border.
							if (l[0] + rN < 0 || l[0] + rN >= getRowCount())
								continue;
							for (int cN = -1; cN <= 1; cN++) {
								// Check for border.
								if (l[1] + cN < 0 || l[1] + cN >= getColumnCount())
									continue;
								// Get board value at current check location.
								byte currValue = board[l[0] + rN][l[1] + cN];
								// Add current location to check list if empty
								// or queen, and unchecked.
								if ((currValue == E || currValue == B || currValue == W)
										&& !checked[l[0] + rN][l[1] + cN]) {
									check.add(new int[] { l[0] + rN, l[1] + cN });
									// Mark location as checked.
									checked[l[0] + rN][l[1] + cN] = true;
								}
							}
						}
					}
				}

				// Add chamber info to array.
				for (int f = 0; f < found.size(); f++) {
					int r = found.get(f)[0];
					int c = found.get(f)[1];
					// Chamber labels.
					chambers[0][r][c] = label;
					// Black queen count.
					chambers[1][r][c] = bCount;
					// White queen count.
					chambers[2][r][c] = wCount;
				}
				label++;
			}
		}
		// Save recalculated chambers.
		this.chambers = chambers;
		// Return board chamber representation array.
		return chambers;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#getPoints()
	 */
	public int[][] getPoints() {
		// Calculate chambers if needed.
		byte[][][] chambers = getChambers();
		// Shared and unshared points, for black and white.
		int[][] points = new int[2][2];
		for (int i = 0; i < getRowCount(); i++)
			for (int j = 0; j < getColumnCount(); j++) {
				if (chambers[1][i][j] > 0) {
					points[0][0]++;
					if (chambers[2][i][j] <= 0)
						points[1][0]++;
				}
				if (chambers[2][i][j] > 0) {
					points[0][1]++;
					if (chambers[1][i][j] <= 0)
						points[1][1]++;
				}
			}
		return points;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#checkFinished()
	 */
	public boolean checkFinished() {
		// Calculate chambers if needed.
		byte[][][] chambers = getChambers();
		// Check if any chamber is occupied by both players.
		for (int i = 0; i < getRowCount(); i++)
			for (int j = 0; j < getColumnCount(); j++)
				if (chambers[1][i][j] > 0 && chambers[2][i][j] > 0)
					return false;
		return true;
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
		// Make chamber representation un-calculated after move.
		chambers = null;
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
