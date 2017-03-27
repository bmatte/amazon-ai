package amazon.agent;

/**
 * Evaluation function based on differential of score ratios before and after
 * move.
 */
public class ScoreFunction implements EvaluationFunction {
	@Override
	public double eF(boolean moveTurn, byte[][] initialState, byte[][][] initialChambers, byte[][] finalState,
			byte[][][] finalChambers) {
		// Initial and final, shared and unshared points, for black and white.
		int[][] pI = new int[2][2];
		int[][] pF = new int[2][2];
		for (int i = 0; i < initialChambers[1].length; i++)
			for (int j = 0; j < initialChambers[1][i].length; j++) {
				if (initialChambers[1][i][j] > 0) {
					pI[0][0]++;
					if (initialChambers[2][i][j] <= 0)
						pI[1][0]++;
				}
				if (initialChambers[2][i][j] > 0) {
					pI[0][1]++;
					if (initialChambers[1][i][j] <= 0)
						pI[1][1]++;
				}
				if (finalChambers[1][i][j] > 0) {
					pF[0][0]++;
					if (finalChambers[2][i][j] <= 0)
						pF[1][0]++;
				}
				if (finalChambers[2][i][j] > 0) {
					pF[0][1]++;
					if (finalChambers[1][i][j] <= 0)
						pF[1][1]++;
				}
			}
		double ratioI = (double) pI[0][0] / (pI[0][0] + pI[0][1]);
		ratioI += (double) pI[1][0] / (pI[1][0] + pI[1][1]);
		double ratioF = (double) pF[0][0] / (pF[0][0] + pF[0][1]);
		ratioF += (double) pF[1][0] / (pF[1][0] + pF[1][1]);
		return (ratioF - ratioI) / 4;
	}

}