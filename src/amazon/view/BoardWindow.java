package amazon.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;

import amazon.board.BoardModel;

import java.awt.BorderLayout;

public class BoardWindow implements BoardView {
	// This GUI's frame.
	private JFrame frame;
	// Panel to draw on.
	private JPanel panel;
	// Board model to draw.
	private BoardModel boardModel;
	// Quality setting.
	boolean quality;

	// Neutral HSB.
	float[] n;
	// Black player HSB.
	float[] b;
	// White player HSB.
	float[] w;

	// Highlight modifiers.
	float[] h;
	// Light modifiers.
	float[] l;
	// Medium modifiers.
	float[] m;
	// Dark modifiers.
	float[] d;
	// Deep modifiers.
	float[] p;

	/**
	 * Create a new board window.
	 * 
	 * @param boardModel
	 *            Model of game board.
	 * @param randomize
	 *            Randomize game hues.
	 * @param match
	 *            Match game hues.
	 * @param quality
	 *            Use high quality rendering.
	 */
	public BoardWindow(BoardModel boardModel, boolean randomize, boolean match, boolean quality) {
		this.boardModel = boardModel;
		this.quality = quality;

		// Initialize colors.
		n = new float[] { 0.3f, 0.1f, 0.5f };
		b = new float[] { 0.6f, 0.3f, 0.5f };
		w = new float[] { 0.1f, 0.5f, 0.5f };

		// Randomize hues.
		if (randomize) {
			n[0] = (float) Math.random();
			b[0] = (n[0] + 0.33f) % 1f;
			w[0] = (n[0] - 0.33f + 1f) % 1f;
		}

		// Match hues.
		if (match) {
			b[0] = n[0];
			w[0] = n[0];
		}

		// Color modifiers.
		h = new float[] { 1.0f, 0.5f, 1.6f };
		l = new float[] { 1.0f, 0.2f, 3.0f };
		m = new float[] { 1.0f, 0.5f, 1.5f };
		d = new float[] { 1.0f, 1.0f, 0.3f };
		p = new float[] { 1.0f, 4.0f, 0.7f };

		// Initialize components.
		initialize();
	}

	/**
	 * Initialize the component contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setSize(640, 480);
		frame.setTitle("Game of Amazons");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				// Component graphics.
				Graphics2D gg = (Graphics2D) g;

				// Higher quality drawing.
				if (quality) {
					gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					gg.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
					gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				}

				// Corner padding.
				int cP = 10;

				// Find square dimensions of board.
				int width = (int) Math.min(g.getClipBounds().getWidth() / 2 - cP * 2,
						g.getClipBounds().getHeight() * 2 / 3 - cP * 2);
				// Tile width.
				int tW = (width - 1) / boardModel.getRowCount();
				width = 1 + tW * boardModel.getRowCount();

				// Draw border lines.
				g.setColor(gc(n, d));
				g.drawLine(cP + 0, cP + 0, cP + 0, cP + width);
				g.drawLine(cP + 0, cP + 0, cP + width, cP + 0);
				g.drawLine(cP + width, cP + 0, cP + width, cP + width);
				g.drawLine(cP + 0, cP + width, cP + width, cP + width);

				// Get chambers.
				byte[][][] chambers = boardModel.getChambers();
				// Max label found.
				int max = 0;
				for (int i = 0; i < chambers[0].length; i++)
					for (int j = 0; j < chambers[0][i].length; j++)
						max = Math.max(max, chambers[0][i][j]);
				// Different chamber label colors.
				Color[] chamberColors = new Color[max + 1];
				for (int i = 0; i < max + 1; i++)
					chamberColors[i] = Color.getHSBColor((float) Math.random(), (float) Math.random(),
							0.25f * (float) Math.random() + 0.25f);
				// chamberColors[i] = Color.getHSBColor((float) i / (max+1),
				// 1.0f, 0.5f);

				// For each row.
				for (int i = 0; i < boardModel.getRowCount(); i++) {
					// For each column.
					for (int j = 0; j < boardModel.getColumnCount(); j++) {
						int x = cP + 1 + j * tW;
						int y = cP + 1 + i * tW;
						// Draw board background.
						g.setColor((i + j) % 2 == 0 ? gc(n, h) : gc(n, l));
						g.fillRect(x, y, tW, tW);

						// // Draw background lines.
						// g.setColor((i + j) % 2 == 0 ? gc(n, l) : gc(n, h));
						// g.drawLine(x, y, x+tW, y+tW);
						// g.drawLine(x+tW, y, x, y+tW);
						// g.drawLine(x, y+tW/2, x+tW, y+tW/2);
						// g.drawLine(x+tW/2, y, x+tW/2, y+tW);

						// Check the four corners for filling.
						for (int c = 0; c < 4; c++) {
							boolean tL = isArrow(i - 1, j - 1);
							boolean tM = isArrow(i - 1, j + 0);
							boolean tR = isArrow(i - 1, j + 1);
							boolean mL = isArrow(i - 0, j - 1);
							boolean mR = isArrow(i - 0, j + 1);
							boolean bL = isArrow(i + 1, j - 1);
							boolean bM = isArrow(i + 1, j + 0);
							boolean bR = isArrow(i + 1, j + 1);
							if (isArrow(i, j)) {
								switch (c) {
								case (0):
									if (mL || tM)
										drawCorner(gg, 0, gc(n, d), gc(n, l), gc(n, d), x, y, tW);
									break;
								case (1):
									if (tM || mR)
										drawCorner(gg, 1, gc(n, d), gc(n, l), gc(n, d), x, y, tW);
									break;
								case (2):
									if (mR || bM)
										drawCorner(gg, 2, gc(n, d), gc(n, l), gc(n, d), x, y, tW);
									break;
								case (3):
									if (bM || mL)
										drawCorner(gg, 3, gc(n, d), gc(n, l), gc(n, d), x, y, tW);
								}
							} else {
								switch (c) {
								case (0):
									if (mL && tL && tM)
										drawCorner(gg, 0, gc(n, d), gc(n, l), gc(n, d), x, y, tW);
									break;
								case (1):
									if (tM && tR && mR)
										drawCorner(gg, 1, gc(n, d), gc(n, l), gc(n, d), x, y, tW);
									break;
								case (2):
									if (mR && bR && bM)
										drawCorner(gg, 2, gc(n, d), gc(n, l), gc(n, d), x, y, tW);
									break;
								case (3):
									if (bM && bL && mL)
										drawCorner(gg, 3, gc(n, d), gc(n, l), gc(n, d), x, y, tW);
								}
							}
						}
						// Draw game piece.
						switch (boardModel.get(i, j)) {
						case (BoardModel.B):
							drawQueen(gg, gc(b, p), gc(b, h), gc(b, p), x, y, tW);
							break;
						case (BoardModel.W):
							drawQueen(gg, gc(w, m), gc(w, l), gc(w, d), x, y, tW);
							break;
						case (BoardModel.AB):
							drawArrow(gg, gc(b, d), gc(b, l), gc(b, d), x, y, tW);
							break;
						case (BoardModel.AW):
							drawArrow(gg, gc(w, d), gc(w, l), gc(w, d), x, y, tW);
						}

						// XXX Draw chambers for debugging.
						g.setColor(chamberColors[chambers[0][i][j]]);
						g.fillRect(x + width + cP, y, tW, tW);
						g.setColor(new Color(255, 255, 255));
						g.drawString(Integer.toString((int) chambers[1][i][j]), x + width + cP, y + cP);
						g.drawString(Integer.toString((int) chambers[2][i][j]), x + width + cP, y + cP + tW / 2);
					}
				}
				// XXX Draw points for debugging.
				g.setColor(new Color(0, 0, 0));
				g.drawString(
						"B:" + boardModel.getPoints()[0][0] + "(" + boardModel.getPoints()[1][0] + ") W:"
								+ boardModel.getPoints()[0][1] + "(" + boardModel.getPoints()[1][1] + ")",
						cP, width + cP * 3);
			}
		};
		panel.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
			}

		});
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setVisible(true);
	}

	private boolean isArrow(int r, int c) {
		// Consider off board as arrow.
		if (r < 0 || r >= boardModel.getRowCount() || c < 0 || c >= boardModel.getColumnCount())
			return true;
		// Check board position.
		return boardModel.get(r, c) == BoardModel.AB || boardModel.get(r, c) == BoardModel.AW;
	}

	private void drawQueen(Graphics2D g, Color c1, Color c2, Color c3, int x, int y, int size) {
		g.setColor(c1);
		g.fillPolygon(new int[] { x + size / 2, x + size, x + size / 2, x },
				new int[] { y, y + size / 2, y + size, y + size / 2 }, 4);
		g.setColor(c2);
		g.fillPolygon(new int[] { x + size / 2, x + size, x + size / 2 },
				new int[] { y + size / 2, y + size / 2, y + size }, 3);
		// g.fillRoundRect(x + size / 8, y + size / 8, size / 2, size / 2, size,
		// size);
		g.setColor(c3);
		g.drawPolygon(new int[] { x + size / 2, x + size, x + size / 2, x },
				new int[] { y, y + size / 2, y + size, y + size / 2 }, 4);
	}

	private void drawArrow(Graphics2D g, Color c1, Color c2, Color c3, int x, int y, int size) {
		g.setColor(c1);
		g.fillPolygon(new int[] { x + size / 2, x + size, x + size / 2, x },
				new int[] { y, y + size / 2, y + size, y + size / 2 }, 4);
		// g.setColor(c2);
		// g.fillPolygon(new int[] { x + size / 2, x+size, x + size / 2, x },
		// new int[] { y, y + size / 2, y + size, y + size / 2 }, 4);
		g.setColor(c3);
		g.drawPolygon(new int[] { x + size / 2, x + size, x + size / 2, x },
				new int[] { y, y + size / 2, y + size, y + size / 2 }, 4);
	}

	private void drawCorner(Graphics2D g, int corner, Color c1, Color c2, Color c3, int x, int y, int size) {
		// X points.
		int[] xP = new int[] { 0, 0, 0 };
		// Y points.
		int[] yP = new int[] { 0, 0, 0 };
		// Define corner polygon by points.
		int xL = x;
		int xM = x + size / 2;
		int xR = x + size;
		int yT = y;
		int yM = y + size / 2;
		int yB = y + size;
		switch (corner) {
		// Top left.
		case (0):
			xP = new int[] { xL, xM, xL };
			yP = new int[] { yT, yT, yM };
			break;
		// Top right.
		case (1):
			xP = new int[] { xM, xR, xR };
			yP = new int[] { yT, yT, yM };
			break;
		// Bottom right.
		case (2):
			xP = new int[] { xM, xR, xR };
			yP = new int[] { yB, yM, yB };
			break;
		// Bottom left.
		case (3):
			xP = new int[] { xL, xM, xL };
			yP = new int[] { yM, yB, yB };
		}

		g.setColor(c1);
		g.fillPolygon(xP, yP, 3);
		// g.setColor(c2);
		// g.fillPolygon(new int[] { x + size / 2, x+size, x + size / 2, x },
		// new int[] { y, y + size / 2, y + size, y + size / 2 }, 4);
		g.setColor(c3);
		g.drawPolygon(xP, yP, 3);
	}

	/**
	 * Generate a color from HSB float values and modification percentages.
	 * 
	 * @param color
	 *            Starting HSB values.
	 * @param mod
	 *            HSB modification values.
	 * @return Generated color.
	 */
	private Color gc(float[] color, float[] mod) {
		float h = Math.max(Math.min(color[0] * mod[0], 1), 0);
		float s = Math.max(Math.min(color[1] * mod[1], 1), 0);
		float b = Math.max(Math.min(color[2] * mod[2], 1), 0);
		return Color.getHSBColor(h, s, b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.BoardView#repaint()
	 */
	public void repaint() {
		panel.repaint();
	}
}
