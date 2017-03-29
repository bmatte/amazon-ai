package amazon.agent;

/**
 * Evaluation function interface for determining best move given before and
 * after states.
 */
public interface EvaluationFunction {

	/**
	 * @param moveTurn
	 *            Whether black player's turn before move.
	 * @param initialState
	 *            Board state before move to evaluate.
	 * @param initialChambers
	 *            Chamber state before move to evaluate.
	 * @param finalState
	 *            Board state after move to evaluate.
	 * @param finalChambers
	 *            Chamber state after move to evaluate.
	 * @return Evaluation of move from 1 to -1, black to white favor.
	 */
	public double eF(boolean moveTurn, byte[][] initialState, byte[][][] initialChambers, byte[][] finalState,
			byte[][][] finalChambers);
}
