package org.saurus.chesswidget;

import org.saurus.chess.pgn.Board;
import org.saurus.chess.pgn.Cell;
import org.saurus.chess.pgn.Move;
import org.saurus.chess.pgn.Piece;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public class BoardDrawer {
	private Board board;
	private int width;
	private int height;
	private int x0, y0, sqSize;
	private int pieceXDelta, pieceYDelta; // top/left pixel draw position
											// relative to square
	private boolean flipped;
	private boolean drawSquareLabels;

	private Paint darkPaint;
	private Paint brightPaint;
	private Paint whitePiecePaint;
	private Paint blackPiecePaint;
	private Paint selectedSquarePaint;
	private Paint labelPaint;
	private Paint decorationPaint;

	private Move lastMove;

	public BoardDrawer(Context context, int width, int height) {
		this.width = width;
		this.height = height;

		sqSize = Math.min(getSqSizeW(width), getSqSizeH(height));
		computeOrigin(width, height);
		pieceXDelta = pieceYDelta = -1;
		flipped = false;
		drawSquareLabels = false;

		darkPaint = new Paint();
		brightPaint = new Paint();

		whitePiecePaint = new Paint();
		whitePiecePaint.setAntiAlias(true);

		blackPiecePaint = new Paint();
		blackPiecePaint.setAntiAlias(true);

		selectedSquarePaint = new Paint();
		selectedSquarePaint.setStyle(Paint.Style.STROKE);
		selectedSquarePaint.setAntiAlias(true);
		selectedSquarePaint.setStrokeWidth(sqSize / (float) 16);

		labelPaint = new Paint();
		labelPaint.setAntiAlias(true);

		decorationPaint = new Paint();
		decorationPaint.setAntiAlias(true);

		Typeface chessFont = Typeface.createFromAsset(context.getAssets(), "fonts/ChessCases.ttf");
		whitePiecePaint.setTypeface(chessFont);
		blackPiecePaint.setTypeface(chessFont);
		// whitePiecePaint.setTypeface(Typeface.MONOSPACE);
		// blackPiecePaint.setTypeface(Typeface.MONOSPACE);

		blackPiecePaint.setTextSize(sqSize);
		whitePiecePaint.setTextSize(sqSize);
		labelPaint.setTextSize(sqSize / 4.0f);
		decorationPaint.setTextSize(sqSize / 3.0f);

		setColors();
	}

	/** Must be called for new color theme to take effect. */
	final void setColors() {
		ColorTheme ct = ColorTheme.instance();
		darkPaint.setColor(ct.getColor(ColorTheme.DARK_SQUARE));
		brightPaint.setColor(ct.getColor(ColorTheme.BRIGHT_SQUARE));
		whitePiecePaint.setColor(ct.getColor(ColorTheme.BRIGHT_PIECE));
		blackPiecePaint.setColor(ct.getColor(ColorTheme.DARK_PIECE));
		selectedSquarePaint.setColor(ct.getColor(ColorTheme.SELECTED_SQUARE));
		labelPaint.setColor(ct.getColor(ColorTheme.SQUARE_LABEL));
		decorationPaint.setColor(ct.getColor(ColorTheme.DECORATION));
	}

	final void setBoard(Board board, Move lastMove) {
		this.board = board;
		this.lastMove = lastMove;
	}

	/** Set/clear the board flipped status. */
	final public void setFlipped(boolean flipped) {
		this.flipped = flipped;
	}

	/** Set/clear the board flipped status. */
	final public void setDrawSquareLabels(boolean drawSquareLabels) {
		this.drawSquareLabels = drawSquareLabels;
	}

	protected int getWidth(int sqSize) {
		return sqSize * 8;
	}

	protected int getHeight(int sqSize) {
		return sqSize * 8;
	}

	protected int getSqSizeW(int width) {
		return (width) / 8;
	}

	protected int getSqSizeH(int height) {
		return (height) / 8;
	}

	protected int getMaxHeightPercentage() {
		return 75;
	}

	protected int getMaxWidthPercentage() {
		return 65;
	}

	protected void computeOrigin(int width, int height) {
		x0 = (width - sqSize * 8) / 2;
		y0 = (height - sqSize * 8) / 2;
	}

	protected int getXFromSq(Cell sq) {
		return sq.getFileInt() - 1;
	}

	protected int getYFromSq(Cell sq) {
		return sq.getRank() - 1;
	}

	protected int getXCrd(int x) {
		return x0 + sqSize * (flipped ? 7 - x : x);
	}

	protected int getYCrd(int y) {
		return y0 + sqSize * (flipped ? y : 7 - y);
	}

	protected int getXSq(int xCrd) {
		int t = (xCrd - x0) / sqSize;
		return flipped ? 7 - t : t;
	}

	protected int getYSq(int yCrd) {
		int t = (yCrd - y0) / sqSize;
		return flipped ? t : 7 - t;
	}

	protected int minValidY() {
		return 0;
	}

	protected int maxValidX() {
		return 7;
	}

	protected Cell getSquare(int x, int y) {
		return new Cell(x + 1, y + 1);
	}

	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// int width = getMeasuredWidth();
	// int height = getMeasuredHeight();
	// int sqSizeW = getSqSizeW(width);
	// int sqSizeH = getSqSizeH(height);
	// int sqSize = Math.min(sqSizeW, sqSizeH);
	// pieceXDelta = pieceYDelta = -1;
	// labelBounds = null;
	// if (height > width) {
	// int p = getMaxHeightPercentage();
	// height = Math.min(getHeight(sqSize), height * p / 100);
	// } else {
	// int p = getMaxWidthPercentage();
	// width = Math.min(getWidth(sqSize), width * p / 100);
	// }
	// setMeasuredDimension(width, height);
	// }

	public Bitmap draw() {
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);

		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				final int xCrd = getXCrd(x);
				final int yCrd = getYCrd(y);
				Paint paint = darkSquare(x, y) ? darkPaint : brightPaint;
				canvas.drawRect(xCrd, yCrd, xCrd + sqSize, yCrd + sqSize, paint);

				Cell sq = getSquare(x, y);
				Piece p = getPiece(sq);
				drawPiece(canvas, xCrd, yCrd, p);
				if (drawSquareLabels) {
					if (x == (flipped ? 7 : 0)) {
						drawLabel(canvas, xCrd, yCrd, false, false, "12345678".charAt(y));
					}
					if (y == (flipped ? 7 : 0)) {
						drawLabel(canvas, xCrd, yCrd, true, true, "abcdefgh".charAt(x));
					}
				}
			}
		}

		if (this.lastMove != null) {
			drawSelection(canvas, this.lastMove.getStartCell());
			drawSelection(canvas, this.lastMove.getEndCell());
		}

		return bmp;
	}

	private void drawSelection(Canvas canvas, Cell cell) {
		if (cell != null) {
			int selX = getXFromSq(cell);
			int selY = getYFromSq(cell);
			int x0 = getXCrd(selX);
			int y0 = getYCrd(selY);
			canvas.drawRect(x0, y0, x0 + sqSize, y0 + sqSize, selectedSquarePaint);
		}
	}

	private Piece getPiece(Cell sq) {
		return board.get(sq);
	}

	private boolean darkSquare(int x, int y) {
		return (x & 1) == (y & 1);
	}

	private final void drawPiece(Canvas canvas, int xCrd, int yCrd, Piece piece) {
		char psb, psw;

		if (piece == null)
			return;

		switch (piece.getPieceType()) {
		default:
			psb = 0;
			psw = 0;
			break;
		case KING:
			psb = 'H';
			psw = 'k';
			break;
		case QUEEN:
			psb = 'I';
			psw = 'l';
			break;
		case ROOK:
			psb = 'J';
			psw = 'm';
			break;
		case BISHOP:
			psb = 'K';
			psw = 'n';
			break;
		case KNIGHT:
			psb = 'L';
			psw = 'o';
			break;
		case PAWN:
			psb = 'M';
			psw = 'p';
			break;
		}
		if (piece.isBlack()) {
			psb += 6;
			psw += 6;
		}

		if (psb > 0) {
			if (pieceXDelta < 0) {
				Rect bounds = new Rect();
				blackPiecePaint.getTextBounds("H", 0, 1, bounds);
				pieceXDelta = (sqSize - (bounds.left + bounds.right)) / 2;
				pieceYDelta = (sqSize - (bounds.top + bounds.bottom)) / 2;
			}
			xCrd += pieceXDelta;
			yCrd += pieceYDelta;
			canvas.drawText("" + psw, xCrd, yCrd, whitePiecePaint);
			canvas.drawText("" + psb, xCrd, yCrd, blackPiecePaint);
		}
	}

	private Rect labelBounds = null;

	private final void drawLabel(Canvas canvas, int xCrd, int yCrd, boolean right, boolean bottom, char c) {
		String s = Character.toString(c);
		if (labelBounds == null) {
			labelBounds = new Rect();
			labelPaint.getTextBounds("f", 0, 1, labelBounds);
		}
		int margin = sqSize / 16;
		if (right) {
			xCrd += sqSize - labelBounds.right - margin;
		} else {
			xCrd += -labelBounds.left + margin;
		}
		if (bottom) {
			yCrd += sqSize - labelBounds.bottom - margin;
		} else {
			yCrd += -labelBounds.top + margin;
		}
		canvas.drawText(s, xCrd, yCrd, labelPaint);
	}

}
