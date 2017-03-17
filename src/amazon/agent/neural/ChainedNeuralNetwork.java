package amazon.agent.neural;

public class ChainedNeuralNetwork implements NeuralNetwork {

	NeuralNetwork[] nn;

	public ChainedNeuralNetwork(int inputSize, int hiddenSize, int hiddenCount, int outputSize, int count) {
		nn = new NeuralNetwork[count];
		for (int i = 0; i < count; i++) {
			int size = (i == 0) ? inputSize : inputSize + 1;
			nn[i] = new VanillaNeuralNetwork(size, hiddenSize, hiddenCount, outputSize);
		}
	}

	@Override
	public float[] calc(float[] input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double train(float[] input, float[] trueOutput, double learningRate) {
		double error = 0;
		double[] errors = expTrain(input, trueOutput, learningRate);
		for (int i = 0; i < errors.length; i++)
			error += errors[i];
		return error / errors.length;
	}

	public double[] expTrain(float[] input, float[] trueOutput, double learningRate) {
		double[] error = new double[nn.length];
		float[] lastOutput = {};
		for (int i = 0; i < nn.length; i++) {
			error[i] = nn[i].train(combineValues(input, lastOutput), trueOutput, learningRate);
			lastOutput = nn[i].calc(combineValues(input, lastOutput));
		}
		return error;
	}

	public float[] combineValues(float[] a0, float[] a1) {
		float[] c = new float[a0.length + a1.length];
		for (int i = 0; i < c.length; i++) {
			float value = (i < a0.length) ? a0[i] : a1[i - a0.length];
			c[i] = value;
		}
		return c;
	}

	public int size() {
		return nn.length;
	}
}
