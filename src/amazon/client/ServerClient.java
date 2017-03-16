package amazon.client;

/**
 * Server client interface for sending moves.
 */
public interface ServerClient {
	/**
	 * Send move message to server.
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
	 */
	public void sendMove(int rQI, int cQI, int rQF, int cQF, int rA, int cA);

	/**
	 * Receive move message from server.
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
	 */
	public void receiveMove(int rQI, int cQI, int rQF, int cQF, int rA, int cA);

	/**
	 * Check whether current player is black or white.
	 * 
	 * @return Whether player is black.
	 */
	public Boolean isBlackPlayer();
}
