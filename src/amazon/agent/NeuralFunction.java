package amazon.agent;

import java.util.ArrayList;

import amazon.agent.neural.NeuralNetwork;
import amazon.board.BoardModel;

/**
 * Evaluation function utilizing a given neural network.
 */
public class NeuralFunction implements EvaluationFunction {
	/** Neural network to use. */
	private NeuralNetwork nn;

	public NeuralFunction(NeuralNetwork nn) {
		this.nn = nn;
	}

	@Override
	public double eF(boolean moveTurn, byte[][] initialState, byte[][][] initialChambers, byte[][] finalState,
			byte[][][] finalChambers) {
		// Return random if no neural network.
		if (nn == null) {
			System.err.println("Warning: Evaluation Function not using neural network!");
			return Math.random() * 2 - 1;
		}
		float[] floats = statesToFloat(moveTurn, initialState, initialChambers, finalState, finalChambers);
		return nn.calc(floats)[0];
	}

	/**
	 * Convert a single board position value into a float component
	 * representation.
	 * 
	 * @param b
	 *            Position value.
	 * @return Array of float components representing board position value.
	 */
	private static float[] positionToStates(byte b) {
		float[] output = new float[9];
		// If empty.
		output[0] = b == BoardModel.E ? 1f : -1f;
		// If black queen.
		output[1] = b == BoardModel.B ? 1f : -1f;
		// If white queen.
		output[2] = b == BoardModel.W ? 1f : -1f;
		// If black arrow.
		output[3] = b == BoardModel.AB ? 1f : -1f;
		// If white arrow.
		output[4] = b == BoardModel.AW ? 1f : -1f;
		// If an arrow.
		output[5] = (b == BoardModel.AB || b == BoardModel.AW) ? 1f : -1f;
		// If a queen.
		output[6] = (b == BoardModel.B || b == BoardModel.W) ? 1f : -1f;
		// If a black queen or arrow.
		output[7] = (b == BoardModel.B || b == BoardModel.AB) ? 1f : -1f;
		// If a white queen or arrow.
		output[8] = (b == BoardModel.W || b == BoardModel.AW) ? 1f : -1f;
		return output;
	}

	/**
	 * Convert a single chamber value into a float component representation.
	 * 
	 * @param b
	 *            Chamber value.
	 * @param allChambers
	 *            For board chambers, unspecific to player.
	 * @return Array of float components representing chamber value.
	 */
	private static float[] chamberToStates(byte b, boolean allChambers) {
		float[] output;
		if (allChambers) {
			output = new float[4];
			// If -1.
			output[0] = b == -1 ? 1f : -1f;
			// If 0.
			output[1] = b == 0 ? 1f : -1f;
			// If 1.
			output[2] = b == 1 ? 1f : -1f;
			// If >1.
			output[3] = b > 1 ? 1f : -1f;
		} else {
			output = new float[1];
			// If 0.
			output[0] = b == 0 ? 1f : -1f;
		}
		return output;
	}

	/**
	 * Convert the states before and after a move into a neural input float
	 * array.
	 * 
	 * @param initialState
	 *            Board state before move to evaluate.
	 * @param finalState
	 *            Board state after move to evaluate.
	 * @return Neural input float array representation.
	 */
	public static float[] statesToFloat(boolean moveTurn, byte[][] initialState, byte[][][] initialChambers,
			byte[][] finalState, byte[][][] finalChambers) {
		// Create list of floats for input.
		ArrayList<Float> values = new ArrayList<>();
		// Add move turn value.
		values.add(moveTurn ? 1f : -1f);
		// For each spot on the board.
		for (int i = 0; i < initialState.length; i++) {
			for (int j = 0; j < initialState[i].length; j++) {
				float[] vI = positionToStates(initialState[i][j]);
				float[] vF = positionToStates(finalState[i][j]);
				float[] cI = chamberToStates(initialChambers[0][i][j], true);
				float[] cBI = chamberToStates(initialChambers[1][i][j], false);
				float[] cWI = chamberToStates(initialChambers[2][i][j], false);
				float[] cF = chamberToStates(finalChambers[0][i][j], true);
				float[] cBF = chamberToStates(finalChambers[1][i][j], false);
				float[] cWF = chamberToStates(finalChambers[2][i][j], false);
				for (int k = 0; k < vI.length; k++) {
					values.add(vI[k]);
					values.add(vF[k]);
				}
				for (int k = 0; k < cI.length; k++) {
					values.add(cI[k]);
					values.add(cF[k]);
				}
				for (int k = 0; k < cBI.length; k++) {
					values.add(cBI[k]);
					values.add(cWI[k]);
					values.add(cBF[k]);
					values.add(cWF[k]);
				}
			}
		}
		float[] floats = new float[values.size()];
		for (int i = 0; i < values.size(); i++)
			floats[i] = values.get(i);
		return floats;
	}
}
