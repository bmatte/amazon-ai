package amazon.agent.neural;

public interface NeuralNetwork {

	/**
	 * Calculate the output of this neural net given an input.
	 * 
	 * @param input
	 *            Input floating point array.
	 * @return The output floating point array.
	 */
	float[] calc(float[] input);

	/**
	 * Train this neural net using back propagation, given an input and expected
	 * output.
	 * 
	 * @param input
	 *            Input floating point array.
	 * @param trueOutput
	 *            Expected output floating point array.
	 */
	double train(float[] input, float[] trueOutput, double learningRate);

}