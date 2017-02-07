package amazon.view;

import amazon.board.BoardModel;

public class BoardASCII implements BoardView {
	// Board model to print.
	BoardModel boardModel;

	// Board position state character representations.
	private char e0;
	private char e1;
	private char b;
	private char w;
	private char ab;
	private char aw;

	/**
	 * Create a new ASCII board representation.
	 * 
	 * @param boardModel
	 *            - Board model to draw.
	 * @param e0
	 *            - First empty position character representation.
	 * @param e1
	 *            - Second empty position character representation.
	 * @param b
	 *            - Black queen character representation.
	 * @param w
	 *            - White queen character representation.
	 * @param ab
	 *            - Black arrow position character representation.
	 * @param aw
	 *            - White arrow position character representation.
	 */
	public BoardASCII(BoardModel boardModel, char e0, char e1, char b, char w, char ab, char aw) {
		this.boardModel = boardModel;
		this.e0 = e0;
		this.e1 = e1;
		this.b = b;
		this.w = w;
		this.ab = ab;
		this.aw = aw;
	}

	/**
	 * Create a new ASCII board representation with default characters.
	 * 
	 * @param boardModel
	 *            - Board model to draw.
	 */
	public BoardASCII(BoardModel boardModel) {
		this(boardModel, '^', '\'', 'B', 'W', 'b', 'w');
	}

	@Override
	public void repaint() {
		// Print top board border.
		String s = "+";
		for (int i = 0; i < boardModel.getColumnCount() * 2 + 1; i++)
			s += '-';
		s += "+\n";

		// For each row.
		for (int i = 0; i < boardModel.getRowCount(); i++) {
			s += "| ";
			// For each column.
			for (int j = 0; j < boardModel.getColumnCount(); j++) {
				// Print game piece.
				switch (boardModel.get(i, j)) {
				case (BoardModel.E):
					s += ((i + j) % 2 == 0 ? e0 : e1) + " ";
					break;
				case (BoardModel.B):
					s += b + " ";
					break;
				case (BoardModel.W):
					s += w + " ";
					break;
				case (BoardModel.AB):
					s += ab + " ";
					break;
				case (BoardModel.AW):
					s += aw + " ";
				}
			}
			s += "|\n";
		}

		// Print bottom board border.
		s += "+";
		for (int i = 0; i < boardModel.getColumnCount() * 2 + 1; i++)
			s += '-';
		s += "+\n";

		System.out.println(s);
	}

}
