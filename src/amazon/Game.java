package amazon;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
		// Create new board model.
		board = new BoardArray();

		// Load neural network.
		NeuralNetwork nn = null;
		String nnFilename = "amazon5.nn";
		int hiddenSize = 5;
		int hiddenCount = 1;
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

		long iteration = 0;
		// Neural train mode.
		boolean train = false;
		// Probability of being random instead of determined.
		double randomProb = 0;
		// Null client implies simulation mode.
		boolean simulate = client == null || train;
		// Time to delay turns and next game for simulations.
		int simTurnWait = 5000;
		int simGameEndWait = 1000;

		// Play one game, and repeat if set to simulate.
		do {
			// List of move states for current game.
			ArrayList<byte[][]> gameStates = new ArrayList<>();
			// Max possible number of moves is 92.
			for (int i = 0; i < 4; i++) {
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
					// Store best move index.
					int bestIndex = -1;
					double bestRank = -1;
					// Find best move.
					if (Math.random() > randomProb) {
						for (int j = 0; j < possibleMoves.size(); j++) {
							// Get move parameters.
							int[] m = possibleMoves.get(j);

							// Check is move is a stupid move, i.e. queen is
							// within own chamber.
							if ((board.getChambers()[1][m[0]][m[1]] == 0 && board.getChambers()[2][m[0]][m[1]] > 0)
									|| (board.getChambers()[1][m[0]][m[1]] > 0
											&& board.getChambers()[2][m[0]][m[1]] == 0))
								continue;

							// Get the initial state before the simulated move.
							byte[][] initialState = board.getState();
							// Clone board, simulate move, and get resulting
							// state.
							BoardModel simBoard = board.clone();
							simBoard.move(m[0], m[1], m[2], m[3], m[4], m[5]);
							byte[][] finalState = simBoard.getState();
							board.getTurn();
							double rank = evalF.eF(initialState, finalState);
							// Change sign for specific player.
							rank *= board.getTurn() ? 1 : -1;
							if (rank > bestRank || bestIndex == -1) {
								bestIndex = j;
								bestRank = rank;
							}
						}
					}
					if (simulate && !train && !board.getTurn())
						bestIndex = -1;
					// Save state before move.
					gameStates.add(board.getState());
					// Pick random move if not set.
					if (bestIndex == -1)
						bestIndex = (int) (Math.random() * possibleMoves.size());
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
					// Stop if game is finished.
					if (board.checkFinished())
						break;
					// Wait at end of turn, for testing.
					if (simulate && !train)
						try {
							TimeUnit.MILLISECONDS.sleep(simTurnWait);
						} catch (InterruptedException e) {
						}
					try {
						TimeUnit.MILLISECONDS.sleep(simTurnWait);
					} catch (InterruptedException e) {
					}
				}
			}

			if (simulate) {
				// Calculate output as black point ratio from 1 to -1.
				float[] output = {
						((float) board.getPoints()[0][0] / (board.getPoints()[0][0] + board.getPoints()[0][1]) * 2
								- 1) };
				System.out.println(output[0]);
				// Save final state.
				gameStates.add(board.getState());
				// Perform neural network training.
				if (train) {
					double error = -1;
					for (int i = 0; i < (gameStates.size() - 1) * 10; i++) {
						int j = (int) (Math.random() * (gameStates.size() - 1));
						float[] input = NeuralEvaluationFunction.statesToFloat(gameStates.get(j),
								gameStates.get(j + 1));
						error = nn.train(input, output, 0.1);
					}
					if (iteration % 10 == 0) {
						// Decrease random probability.
						// randomProb -= 0.001;
						// System.out.println(error);
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
