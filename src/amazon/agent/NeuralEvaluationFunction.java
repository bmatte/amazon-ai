package amazon.agent;

import java.util.ArrayList;

import amazon.agent.neural.NeuralNetwork;
import amazon.board.BoardModel;

/**
 * Evaluation function utilizing a given neural network.
 */
public class NeuralEvaluationFunction implements EvaluationFunction {
	/** Neural network to use. */
	private NeuralNetwork nn;

	public NeuralEvaluationFunction(NeuralNetwork nn) {
		this.nn = nn;
	}

	@Override
	public double eF(byte[][] initialState, byte[][] finalState) {
		// Return random if no neural network.
		if (nn == null)
			return Math.random() * 2 - 1;
		float[] floats = statesToFloat(initialState, finalState);
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
		float[] output = new float[3];
		output[0] = b == BoardModel.B ? 1f : -1f;
		output[1] = b == BoardModel.W ? 1f : -1f;
		output[2] = (b == BoardModel.AB || b == BoardModel.AW) ? 1f : -1f;
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
	public static float[] statesToFloat(byte[][] initialState, byte[][] finalState) {
		// Create list of floats for input.
		ArrayList<Float> values = new ArrayList<>();
		for (int i = 0; i < initialState.length; i++) {
			for (int j = 0; j < initialState[i].length; j++) {
				float[] vI = positionToStates(initialState[i][j]);
				float[] vF = positionToStates(finalState[i][j]);
				for (int k = 0; k < 3; k++) {
					values.add(vI[k]);
					values.add(vF[k]);
				}
			}
		}
		float[] floats = new float[values.size()];
		for (int i = 0; i < values.size(); i++)
			floats[i] = values.get(i);
		return floats;
	}
}
