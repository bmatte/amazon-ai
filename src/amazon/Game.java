package amazon;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import amazon.agent.EvaluationFunction;
import amazon.agent.NeuralEvaluationFunction;
import amazon.agent.neural.NeuralNetwork;
import amazon.agent.neural.VanillaNeuralNetwork;
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
	// Evaluation function for AI.
	private EvaluationFunction evalF;

	/**
	 * Create a new game.
	 * 
	 * @param viewOption
	 *            0: no view. 1: ASCII print view. 2: Window view.
	 * @param user
	 *            Server user name.
	 * @param pass
	 *            Server user password.
	 * @throws IOException
	 */
	public Game(int viewOption, String user, String pass) {
		// Load neural network.
		NeuralNetwork nn = null;
		String nnFilename = "amazon100.nn";
		int hiddenSize = 100;
		int hiddenCount = 1;
		// Neural train mode.
		boolean train = false;
		// Probability of being random instead of determined.
		double randomProb = 1;
		// Time to delay turns and next game for simulations.
		int simTurnWait = 00;
		int simGameEndWait = 1000;
		int turnTimeLimit = 3000;

		// Create new board model.
		board = new BoardArray();

		// Current number of games played.
		long iteration = 0;

		// Try loading neural network, or create new one.
		try {
			nn = new VanillaNeuralNetwork(nnFilename, 600, hiddenSize, hiddenCount, 1);
		} catch (IOException e1) {
			nn = new VanillaNeuralNetwork(600, hiddenSize, hiddenCount, 1);
			try {
				((VanillaNeuralNetwork) nn).save(nnFilename);
			} catch (IOException e) {
				System.err.println("Failed to save neural network!");
			}
		}

		// Create evaluation function from neural network.
		evalF = new NeuralEvaluationFunction(nn);

		// Don't create view if training.
		if (!train)
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

		// Null client implies simulation mode.
		boolean simulate = client == null || train;

		// Wait until client assigns a player color.
		if (client != null)
			while (client.isBlackPlayer() == null) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
				}
			}

		// Play one game, and repeat if set to simulate.
		do {
			// Moves in current game.
			int moves = 0;
			// List of move states for current game.
			ArrayList<byte[][]> gameStates = new ArrayList<>();
			// Max possible number of moves is 92.
			for (int i = 0; i < 92; i++) {
				// Wait while it's the other (online) player's turn.
				if (!simulate)
					while (board.getTurn() != client.isBlackPlayer()) {
						try {
							TimeUnit.MILLISECONDS.sleep(100);
						} catch (InterruptedException e) {
						}
					}

				// Get list of possible moves.
				ArrayList<int[]> possibleMoves = board.possibleMoves();

				if (possibleMoves.size() > 0) {
					// List of ranks and indices.
					CopyOnWriteArrayList<int[]> rankedIndices = new CopyOnWriteArrayList<>();
					// Always random for training.
					boolean random = train || (simulate && Math.random() < randomProb && !board.getTurn());
					// Best move index to use, -1 for random.
					int bestIndex = -1;
					// Find best move, if not random mode.
					if (!random) {
						// Create new thread pool.
						ExecutorService pool = Executors.newWorkStealingPool();

						// For each depth of search.
						for (int depth = 0; depth <= possibleMoves.size(); depth++) {
							final int fDepth = depth;
							for (int j = 0; j < possibleMoves.size(); j++) {
								final int fJ = j;
								// Execute current search in new thread.
								pool.execute(new Runnable() {
									@Override
									public void run() {
										// Get move parameters.
										int[] m = possibleMoves.get(fJ);
										// Clone board, simulate move, and get
										// resulting state.
										BoardModel simBoard = board.clone();
										// Get the states before and after the
										// simulated move.
										byte[][] initialState = simBoard.getState();
										simBoard.move(m[0], m[1], m[2], m[3], m[4], m[5]);
										byte[][] finalState = simBoard.getState();
										// Evaluate board state with a given
										// depth.
										double rank;
										if (fDepth == 0)
											rank = evalF.eF(initialState, finalState);
										else
											rank = evalBoard(simBoard, 1, fDepth);
										// Change sign for specific player.
										rank *= board.getTurn() ? 1 : -1;
										// Check if move is a stupid move, i.e.
										// queen is
										// within own chamber.
										if ((board.getChambers()[1][m[0]][m[1]] == 0
												&& board.getChambers()[2][m[0]][m[1]] > 0)
												|| (board.getChambers()[1][m[0]][m[1]] > 0
														&& board.getChambers()[2][m[0]][m[1]] == 0))
											rank -= 1;
										// Add rank and index to master list.
										rankedIndices.add(new int[] { (int) (rank * 1000000), fJ, fDepth });
									}
								});
							}

							// Break if out of time, or max depth.
							if (board.getTime() >= turnTimeLimit) {
								break;
							}
						}

						// Wait for time limit to finish.
						while (board.getTime() < turnTimeLimit) {
							try {
								TimeUnit.MILLISECONDS.sleep(100);
							} catch (InterruptedException e) {
							}
						}

						// Shutdown thread pool.
						pool.shutdownNow();

						System.out.println(
								"Searched " + rankedIndices.size() + " times from " + possibleMoves.size() + " moves.");
						int bestRank = 0;
						int bestDepth = 0;
						for (int j = 0; j < rankedIndices.size(); j++)
							if (rankedIndices.get(j)[0] > bestRank || bestIndex == -1) {
								bestRank = rankedIndices.get(j)[0];
								bestIndex = rankedIndices.get(j)[1];
								bestDepth = rankedIndices.get(j)[2];
							}
						System.out.println("Best move found at " + bestDepth + " depth.");
					}

					if (bestIndex == -1)
						bestIndex = (int) (Math.random() * possibleMoves.size());

					// Save state before move.
					gameStates.add(board.getState());
					// Get best move parameters.
					int[] m = possibleMoves.get(bestIndex);
					boolean moveM = move(true, m[0], m[1], m[2], m[3], m[4], m[5]);
					// Print move information if move was invalid.
					if (!moveM)
						System.err.println(
								"{" + m[0] + "," + m[1] + "," + m[2] + "," + m[3] + "," + m[4] + "," + m[5] + "}");
					// Repaint if a view exists.
					if (view != null)
						view.repaint();
					// // Stop if game is finished.
					// if (board.checkFinished())
					// break;
					// Wait at end of turn, for testing.
					if (simulate && !train)
						try {
							TimeUnit.MILLISECONDS.sleep(simTurnWait);
						} catch (InterruptedException e) {
						}
				}
				// Increment move count after black goes, or if online.
				if (!simulate || board.getTurn())
					moves++;
			}

			if (simulate) {
				// Calculate output as black point ratio from 1 to -1.
				float[] output = {
						((float) board.getPoints()[0][0] / (board.getPoints()[0][0] + board.getPoints()[0][1]) * 2
								- 1) };
				if (!train)
					System.out.println(output[0]);
				// output[0] = output[0]*output[0]*Math.signum(output[0]);
				// Save final state.
				gameStates.add(board.getState());
				// Perform neural network training.
				if (train) {
					double error = 0;
					int cycles = (gameStates.size() - 1) * 100;
					for (int i = 0; i < cycles; i++) {
						int j = (int) (Math.random() * (gameStates.size() - 1));
						float[] input = NeuralEvaluationFunction.statesToFloat(gameStates.get(j),
								gameStates.get(j + 1));
						error += nn.train(input, output, 0.1);
					}
					error /= cycles;
					if (iteration % 1 == 0) {
						// Decrease random probability.
						// randomProb -= 0.001;
						System.out.println(error);
						try {
							// System.out.println("Saving Neural Network");
							((VanillaNeuralNetwork) nn).save(nnFilename);
						} catch (IOException e) {
							System.err.println("Failed to save neural network!");
						}
					}
				} else {
					// Wait at end of game simulation if not training.
					try {
						TimeUnit.MILLISECONDS.sleep(simGameEndWait);
					} catch (InterruptedException e) {
					}
				}
				board.reinitialize();
				if (view != null)
					view.repaint();
			}
			iteration++;
		} while (simulate);

	}

	public double evalBoard(BoardModel boardState, int depth, int maxDepth) {
		// Clone board for simulation.
		BoardModel simBoard = boardState.clone();
		// Evaluation rank.
		double rank = 0;
		// Get list of possible moves.
		ArrayList<int[]> possibleMoves = simBoard.possibleMoves();
		for (int i = 0; i < possibleMoves.size(); i++) {
			// Get move parameters.
			int[] m = possibleMoves.get(i);
			// Get the states before and after the simulated move.
			byte[][] initialState = simBoard.getState();
			simBoard.move(m[0], m[1], m[2], m[3], m[4], m[5]);
			byte[][] finalState = simBoard.getState();
			if (depth >= maxDepth) {
				rank += evalF.eF(initialState, finalState);
			} else {
				rank += evalBoard(simBoard, depth + 1, maxDepth);
			}
		}
		rank /= possibleMoves.size();
		return rank;
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
