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
	// Black or white turn.
	private boolean blackTurn;
	// Chambers representation. Will be null when un-calculated.
	private byte[][][] chambers;
	// Time of current turn start.
	private long turnTime;

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
		reinitialize();
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#reinitialize()
	 */
	public void reinitialize() {
		// Create new empty board array.
		board = new byte[10][10];
		// Add initial queens.
		board[6][0] = B;
		board[6][9] = B;
		board[9][3] = B;
		board[9][6] = B;
		board[0][3] = W;
		board[0][6] = W;
		board[3][0] = W;
		board[3][9] = W;

		// // XXX Arrow testing.
		// for (int i = 0; i < 80; i++)
		// board[(int) (Math.random() * 10)][(int) (Math.random() * 10)] =
		// (Math.random() > 0.5 ? AB : AW);

		// White goes first.
		blackTurn = false;
		// Reset turn time.
		turnTime = System.currentTimeMillis();
		// Un-calculated chamber representations.
		chambers = null;
	}

	private BoardArray(byte[][] board, boolean blackTurn) {
		this.board = board;
		this.blackTurn = blackTurn;
		// Reset turn time.
		turnTime = System.currentTimeMillis();
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
		byte[][][] both = getChambers(true, true);
		byte[][][] black = getChambers(true, false);
		byte[][][] white = getChambers(false, true);
		for (int i = 0; i < getRowCount(); i++) {
			for (int j = 0; j < getRowCount(); j++) {
				both[1][i][j] = black[1][i][j];
				both[2][i][j] = white[2][i][j];
			}
		}
		// Save recalculated chambers.
		chambers = both;
		return both;
	}

	/**
	 * @param countBlack
	 *            Count black queens, and treat them as not blocking spaces.
	 * @param countWhite
	 *            Count white queens, and treat them as not blocking spaces.
	 * @return Specific chamber array.
	 */
	private byte[][][] getChambers(boolean countBlack, boolean countWhite) {
		// Initialize chamber labels and count as 0.
		byte[][][] chambers = new byte[3][getRowCount()][getColumnCount()];
		// Initialize chamber queen counts at -1 for non-chambers.
		for (int i = 0; i < getRowCount(); i++)
			for (int j = 0; j < getColumnCount(); j++)
				for (int k = 1; k <= 2; k++)
					chambers[k][i][j] = -1;
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
				checked[i][j] = true;

				// Check location and spread out if empty.
				while (check.size() > 0) {
					// Get location to check.
					int[] l = check.remove(0);
					// Get board value at check location.
					byte value = board[l[0]][l[1]];

					// Check if empty or queen.
					if (value == E || (value == B && countBlack) || (value == W && countWhite)) {
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
								if ((currValue == E || (currValue == B && countBlack) || (currValue == W && countWhite))
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
	private boolean validQueen(int rQI, int cQI, int rQF, int cQF) {
		// Check if initial location isn't current player's queen.
		if (board[rQI][cQI] != (blackTurn ? B : W))
			return false;
		// Check if final location isn't empty.
		if (board[rQF][cQF] != E)
			return false;
		// Check queen move path.
		if (!validMove(rQI, cQI, rQF, cQF, rQI, cQI))
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
	private boolean validArrow(int rQI, int cQI, int rQF, int cQF, int rA, int cA) {
		// Check if arrow location isn't empty, and allow initial queen
		// location.
		if (board[rA][cA] != E && !(rA == rQI && cA == cQI))
			return false;
		// Check arrow move path.
		if (!validMove(rQF, cQF, rA, cA, rQI, cQI))
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
	 * @param rQN
	 *            Row index to ignore (treat as empty).
	 * @param cQN
	 *            Column index to ignore (treat as empty).
	 * @return Validity of piece move.
	 */
	private boolean validMove(int rI, int cI, int rF, int cF, int rN, int cN) {
		// Check if vertical move.
		if (cI == cF) {
			// Up or down.
			int d = rF < rI ? -1 : 1;
			// Check locations between start and destination.
			for (int i = 1; i < Math.abs(rF - rI); i++)
				if (board[rI + d * i][cI] != E && !(rI + d * i == rN && cI == cN))
					return false;
			return true;
			// Check if horizontal move.
		} else if (rI == rF) {
			// Left or right.
			int d = cF < cI ? -1 : 1;
			// Check locations between start and destination.
			for (int i = 1; i < Math.abs(cF - cI); i++)
				if (board[rI][cI + d * i] != E && !(rI == rN && cI + d * i == cN))
					return false;
			return true;
			// Check if upward sloping diagonal move.
		} else if (rF - rI == -(cF - cI)) {
			// Left or right.
			int d = cF < cI ? -1 : 1;
			// Check locations between start and destination.
			for (int i = 1; i < Math.abs(cF - cI); i++)
				if (board[rI - d * i][cI + d * i] != E && !(rI - d * i == rN && cI + d * i == cN))
					return false;
			return true;
			// Check if downward sloping diagonal move.
		} else if (rF - rI == cF - cI) {
			// Left or right.
			int d = cF < cI ? -1 : 1;
			// Check locations between start and destination.
			for (int i = 1; i < Math.abs(cF - cI); i++)
				if (board[rI + d * i][cI + d * i] != E && !(rI + d * i == rN && cI + d * i == cN))
					return false;
			return true;
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
		//// XXX Print error message if queen in owned chamber is moved.
		// if ((getChambers()[1][rQI][cQI] == 0 && getChambers()[2][rQI][cQI] >
		//// 0)
		// || (getChambers()[1][rQI][cQI] > 0 && getChambers()[2][rQI][cQI] ==
		//// 0))
		// System.err.println("Queen within owned chamber being moved!");
		// Check if move is invalid.
		if (!validTurn(rQI, cQI, rQF, cQF, rA, cA))
			return false;
		// Move whatever queen to new location.
		board[rQF][cQF] = board[rQI][cQI];
		// Make initial location empty.
		board[rQI][cQI] = E;
		// Place arrow.
		board[rA][cA] = blackTurn ? AB : AW;
		// Make chamber representation un-calculated after move.
		chambers = null;
		// Change player turn;
		blackTurn = !blackTurn;
		// Reset turn time.
		turnTime = System.currentTimeMillis();
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
		// Location of each queen.
		int[][] queens = getQueens(blackTurn);
		// For each queen.
		for (int q = 0; q < 4; q++) {
			// Initial queen position.
			int rQI = queens[q][0];
			int cQI = queens[q][1];
			// Row direction.
			for (int rD = -1; rD <= 1; rD++) {
				// Column direction.
				for (int cD = -1; cD <= 1; cD++) {
					// Continue if no direction.
					if (rD == 0 && cD == 0)
						continue;
					// Check all possible distances.
					for (int dist = 1; dist < Math.max(getRowCount(), getColumnCount()); dist++) {
						// Calculate final queen position.
						int rQF = rQI + rD * dist;
						int cQF = cQI + cD * dist;
						// Check if move is off board.
						if (rQF < 0 || rQF >= getRowCount() || cQF < 0 || cQF >= getColumnCount())
							break;
						// Check if move is not empty.
						if (board[rQF][cQF] != E)
							break;
						// Arrow row direction.
						for (int rAD = -1; rAD <= 1; rAD++) {
							// Arrow column direction.
							for (int cAD = -1; cAD <= 1; cAD++) {
								// Continue if no direction.
								if (rAD == 0 && cAD == 0)
									continue;

								// Check all possible arrow distances.
								for (int aDist = 1; aDist < Math.max(getRowCount(), getColumnCount()); aDist++) {
									// Calculate arrow position.
									int rA = rQF + rAD * aDist;
									int cA = cQF + cAD * aDist;
									// Check if arrow move is off board.
									if (rA < 0 || rA >= getRowCount() || cA < 0 || cA >= getColumnCount())
										break;
									// Check if arrow move is not empty, or
									// initial position.
									if (board[rA][cA] != E && (rA != rQI || cA != cQI))
										break;
									// Add move to list.
									int[] move = { rQI, cQI, rQF, cQF, rA, cA };
									moves.add(move);
								}
							}
						}

					}
				}
			}
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#getTurn()
	 */
	public boolean getTurn() {
		return blackTurn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.board.BoardModel#getTurn()
	 */
	public long getTime() {
		return System.currentTimeMillis() - turnTime;
	}

	/**
	 * Get the locations of a given players queens.
	 * 
	 * @param white
	 *            Find white or black queens.
	 * @return The location of each queen; first index queen, second index row
	 *         or column.
	 */
	private int[][] getQueens(boolean black) {
		// Location of each queen.
		int[][] queenLoc = new int[4][2];
		// Current queen index.
		int index = 0;
		// Find current player's queen locations.
		for (int i = 0; i < getRowCount(); i++)
			for (int j = 0; j < getColumnCount(); j++)
				if (board[i][j] == (black ? B : W)) {
					queenLoc[index][0] = i;
					queenLoc[index][1] = j;
					index++;
				}
		return queenLoc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public BoardModel clone() {
		return new BoardArray(getState(), blackTurn);
	}

	/** Test initial board layout. */
	public static void main(String[] args) {
		BoardModel model = new BoardArray();
		System.out.println(model.toString());
	}
}
