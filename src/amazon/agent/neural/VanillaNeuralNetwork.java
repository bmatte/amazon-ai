package amazon.agent.neural;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;

public class VanillaNeuralNetwork implements NeuralNetwork {

	public static void main(String[] args) throws IOException {
		float[] in = { 1f, 2f };
		VanillaNeuralNetwork nn;
		try {
			nn = new VanillaNeuralNetwork("test.nn", 2, 3, 3, 2);
			System.out.println("Loaded neural network from file.");
		} catch (Exception e) {
			System.out.println("Creating new neural network.");
			nn = new VanillaNeuralNetwork(2, 3, 3, 2);
		}
		float[] out = nn.calc(in);
		int[] answer = new int[out.length];
		for (int i = 0; i < out.length; i++)
			answer[i] = (int) (out[i] * 100);
		System.out.println("Answer: " + Arrays.toString(answer));
		nn.save("test.nn");
	}

	/**
	 * Hierarchy of layers, nodes, and weights. Weight layer count is one less
	 * than node layer count, due to input layer not having weights. Weight
	 * count is one greater than previous layer size, due to the inclusion of
	 * bias weights.
	 */
	public ArrayList<ArrayList<float[]>> w;
	/**
	 * Hierarchy of layers and nodes, including input, hidden, and output nodes.
	 */
	public ArrayList<float[]> n;

	/**
	 * Create a new artificial neural network with a given input, hidden, and
	 * output layer size, hidden layer count, and learning rate.
	 * 
	 * @param inputSize
	 *            The number of input layer nodes.
	 * @param hiddenSize
	 *            The number of hidden layer nodes.
	 * @param hiddenCount
	 *            The number of hidden layers.
	 * @param outputSize
	 *            The number of output layer nodes.
	 */
	public VanillaNeuralNetwork(int inputSize, int hiddenSize, int hiddenCount, int outputSize) {
		// Create network's nodes and initial randomized weights.
		int layerCount = hiddenCount + 2;
		n = new ArrayList<>();
		w = new ArrayList<>();
		// Create input, hidden, and output layers.
		for (int lI = 0; lI < layerCount; lI++) {
			// Determine layer size and declare node values.
			int size;
			if (lI == 0)
				size = inputSize;
			else if (lI == layerCount - 1)
				size = outputSize;
			else
				size = hiddenSize;
			float[] nodes = new float[size];
			// Add new layer of nodes.
			n.add(nodes);
			// Don't create weights after output layer.
			if (lI >= layerCount - 1)
				continue;
			// Create weights for next layer.
			ArrayList<float[]> weights = new ArrayList<>();
			int nextLayerSize = lI >= layerCount - 2 ? outputSize : hiddenSize;
			for (int nI = 0; nI < nextLayerSize; nI++) {
				// Create an extra weight for node bias.
				float[] nodeWeights = new float[size + 1];
				// Randomize each weight from -1 to 1.
				for (int wI = 0; wI < nodeWeights.length; wI++)
					nodeWeights[wI] = (float) ((Math.random() * 2 - 1) / Math.sqrt(nodeWeights.length));
				weights.add(nodeWeights);
			}
			w.add(weights);
		}
	}

	/**
	 * Load a given number of weights from a file and create a neural network.
	 * 
	 * @param filename
	 *            Name of file to load weights from.
	 * @param inputSize
	 *            The number of input layer nodes.
	 * @param hiddenSize
	 *            The number of hidden layer nodes.
	 * @param hiddenCount
	 *            The number of hidden layers.
	 * @param outputSize
	 *            The number of output layer nodes.
	 * @throws IOException
	 */
	public VanillaNeuralNetwork(String filename, int inputSize, int hiddenSize, int hiddenCount, int outputSize)
			throws IOException {
		// Read data from file.
		File file = new File(filename);
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		byte[] fileData = new byte[(int) file.length()];
		raf.readFully(fileData);
		// Convert into weight floats.
		float[] fileWeights = new float[fileData.length / 4];
		// Current weight index.
		int cW = 0;
		for (int i = 0; i < fileWeights.length; i++) {
			int bits = 0;
			for (int j = 0; j < 4; j++) {
				bits = bits | ((fileData[i * 4 + j] & 0xFF) << ((3 - j) * 8));
			}
			fileWeights[i] = Float.intBitsToFloat(bits);
		}
		raf.close();
		if (fileData.length == 0)
			throw new IOException("File is empty.");

		// Create network's nodes and use weights from file.
		int layerCount = hiddenCount + 2;
		n = new ArrayList<>();
		w = new ArrayList<>();
		// Create input, hidden, and output layers.
		for (int lI = 0; lI < layerCount; lI++) {
			// Determine layer size and declare node values.
			int size;
			if (lI == 0)
				size = inputSize;
			else if (lI == layerCount - 1)
				size = outputSize;
			else
				size = hiddenSize;
			float[] nodes = new float[size];
			// Add new layer of nodes.
			n.add(nodes);
			// Don't create weights after output layer.
			if (lI >= layerCount - 1)
				continue;
			// Create weights for next layer.
			ArrayList<float[]> weights = new ArrayList<>();
			int nextLayerSize = lI >= layerCount - 2 ? outputSize : hiddenSize;
			for (int nI = 0; nI < nextLayerSize; nI++) {
				// Create an extra weight for node bias.
				float[] nodeWeights = new float[size + 1];
				// Use current file weight and increment.
				for (int wI = 0; wI < nodeWeights.length; wI++)
					nodeWeights[wI] = fileWeights[cW++];
				weights.add(nodeWeights);
			}
			w.add(weights);
		}
		if (cW != fileWeights.length)
			throw new IllegalArgumentException("Different number of weights in file!");
	}

	/**
	 * Save this neural network's weights to file.
	 * 
	 * @param filename
	 *            Filename to save to.
	 * @throws IOException
	 */
	public void save(String filename) throws IOException {
		// Write data to file.
		File file = new File(filename);
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.setLength(0);
		// List of bytes to write.
		ArrayList<Byte> bL = new ArrayList<>();
		// Create input, hidden, and output layers.
		for (int lI = 0; lI < w.size(); lI++) {
			for (int nI = 0; nI < w.get(lI).size(); nI++) {
				for (int wI = 0; wI < w.get(lI).get(nI).length; wI++) {
					int bits = Float.floatToRawIntBits(w.get(lI).get(nI)[wI]);
					for (int i = 0; i < 4; i++)
						bL.add((byte) (bits >> ((3 - i) * 8)));
				}
			}
		}
		byte[] b = new byte[bL.size()];
		for (int i = 0; i < bL.size(); i++)
			b[i] = bL.get(i);
		raf.write(b);
		raf.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.agent.NeuralNetwork#calc(float[])
	 */
	@Override
	public float[] calc(float[] input) {
		// Check if input is improper size.
		if (input.length != n.get(0).length)
			throw new IllegalArgumentException("Bad input length.");
		// Set input node values.
		for (int nI = 0; nI < input.length; nI++)
			n.get(0)[nI] = input[nI];
		// Calculate hidden and output layers.
		for (int lI = 1; lI < n.size(); lI++) {
			float[] backLayer = n.get(lI - 1);
			float[] currLayer = n.get(lI);
			// Input layer does not have weights, decrease index by 1.
			ArrayList<float[]> layerWeights = w.get(lI - 1);
			// Calculate current layer's node values.
			for (int nI = 0; nI < currLayer.length; nI++) {
				// Clear previous node value.
				currLayer[nI] = 0;
				float[] nodeWeights = layerWeights.get(nI);
				// Sum weighted values.
				for (int wI = 0; wI < nodeWeights.length; wI++) {
					// Use node value of 1 for bias weight.
					float backNodeValue = wI < nodeWeights.length - 1 ? backLayer[wI] : 1f;
					currLayer[nI] += backNodeValue * nodeWeights[wI];
				}
				currLayer[nI] = (float) Math.tanh(currLayer[nI]);
			}
		}
		return n.get(n.size() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.agent.NeuralNetwork#train(float[], float[], double)
	 */
	@Override
	public double train(float[] input, float[] trueOutput, double learningRate) {
		if (trueOutput.length != n.get(n.size() - 1).length)
			throw new IllegalArgumentException("Bad output length.");
		// Calculate output.
		calc(input);
		// Create layers of delta values corresponding to calculated nodes.
		ArrayList<float[]> d = new ArrayList<>();
		for (int lI = 1; lI < n.size(); lI++) {
			float[] dValues = new float[n.get(lI).length];
			d.add(dValues);
		}
		// Keep track of total error;
		double error = 0;
		// Calculate hidden and output layer's delta values.
		for (int lI = n.size() - 1; lI > 0; lI--) {
			float[] currLayer = n.get(lI - 1);
			float[] forwLayer = n.get(lI);
			// Don't try using nonexistent input delta.
			float[] currDelta = lI > 1 ? d.get(lI - 2) : null;
			float[] forwDelta = d.get(lI - 1);
			// Forward node index.
			for (int fNI = 0; fNI < forwLayer.length; fNI++) {
				// Calculate output layer delta using error from true output.
				if (lI >= n.size() - 1) {
					forwDelta[fNI] = trueOutput[fNI] - forwLayer[fNI];
					error += Math.pow(forwDelta[fNI], 2);
				}
				// Multiply each of the already summed delta values by the
				// sigmoid derivative of the node's value, to complete the delta
				// calculation.
				forwDelta[fNI] *= 1 - forwLayer[fNI] * forwLayer[fNI];
				// Sum weighted deltas for current node layer then increment
				// weights using forward layer deltas.
				for (int cNI = 0; cNI < currLayer.length + 1; cNI++) {
					if (cNI < currLayer.length && currDelta != null)
						currDelta[cNI] += w.get(lI - 1).get(fNI)[cNI] * forwDelta[fNI];
					// Use node value of 1 for bias weight.
					float currNodeValue = cNI < currLayer.length ? currLayer[cNI] : 1f;
					// Adjust weights including bias weight.
					w.get(lI - 1).get(fNI)[cNI] += currNodeValue * forwDelta[fNI] * learningRate;
				}

			}
		}
		return Math.pow(error / d.get(n.size() - 2).length, 0.5);
	}
}
