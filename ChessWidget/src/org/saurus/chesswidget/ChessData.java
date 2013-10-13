package org.saurus.chesswidget;

import java.io.Reader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.saurus.chess.pgn.Board;
import org.saurus.chess.pgn.Game;
import org.saurus.chess.pgn.Move;
import org.saurus.chess.pgn.MoveList;
import org.saurus.chess.pgn.MovePair;
import org.saurus.chess.pgn.PGNException;
import org.saurus.chess.pgn.PGNReader;

public class ChessData {
	public class PlayerData {
		private String name;
		private boolean turn;
		private Move lastMove;
		private String evaluation;
		private String time;
		private int moveNumber;

		public PlayerData(String name, boolean turn, int moveNumber, Move lastMove, String evaluation, String time) {
			this.name = name;
			this.turn = turn;
			this.moveNumber = moveNumber;
			this.lastMove = lastMove;
			this.evaluation = evaluation;
			this.time = time;
		}

		public String getName() {
			return name;
		}

		public boolean isTurn() {
			return turn;
		}

		public Move getLastMove() {
			return lastMove;
		}

		public String getEvaluation() {
			return evaluation;
		}

		public String getTime() {
			return time;
		}

		public int getMoveNumber() {
			return moveNumber;
		}

		public String getLastMoveText() {
			return "" + moveNumber + ":" + (lastMove == null?"???": lastMove.toString());
		}
	}

	private PGNReader pgnReader;
	private Game game;
	private Board board;
	private PlayerData white;
	private PlayerData black;

	public boolean Read(Reader reader) {
		pgnReader = new PGNReader();

		List<Game> games = pgnReader.read(reader);

		if (games == null || games.size() == 0)
			return false;
		
		try {
			game = games.get(0);
			board = pgnReader.fixupMoves(game);
			MoveList moves = game.getMoves();
			Move whiteMove = null;
			Move blackMove = null;
			Move lastMove = null;

			int pos = moves.size() - 1;
			int wMoveNumber = pos + 1;
			int bMoveNumber = wMoveNumber;

			if (pos >= 0) {
				MovePair mp = moves.get(pos);

				blackMove = mp.getBlack();
				lastMove = blackMove;
				whiteMove = mp.getWhite();
				if (lastMove == null)
					lastMove = whiteMove;

				if (blackMove == null && pos > 1) {
					bMoveNumber--;
					mp = moves.get(pos - 1);
					blackMove = mp.getBlack();
					if (lastMove == null)
						lastMove = blackMove;
				}
			}

			String evaluation = null;
			String time = null;

			if (whiteMove != null) {
				evaluation = getCommentData(whiteMove.getComment(), "wv");
				time = getCommentData(whiteMove.getComment(), "tl");
			}
			this.white = new PlayerData(game.getTagWhite(), !moves.isBlackTurn(), wMoveNumber, whiteMove, evaluation, time);
			
			evaluation = null;
			time = null;

			if (blackMove != null) {
				evaluation = getCommentData(blackMove.getComment(), "wv");
				time = getCommentData(blackMove.getComment(), "tl");
			}
			this.black = new PlayerData(game.getTagBlack(), moves.isBlackTurn(), bMoveNumber, blackMove, evaluation, time);
			
		} catch (PGNException e) {
			System.out.println("ERROR: " + e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public PlayerData getWhite() {
		return white;
	}

	public PlayerData getBlack() {
		return black;
	}

	public Board getBoard() {
		return board;
	}

	public Board getBoard(int plyMoves) {
		try {
			return pgnReader.getPosition(game, plyMoves);
		} catch (PGNException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String[] getDump(boolean addLabels) {
		return board.dump(addLabels);
	}

	public Move getLastMove() {
		if (white != null && !white.isTurn())
			return white.getLastMove();
		
		if (black != null)
			return black.getLastMove();
		
		return null;
	}

	public static char getUnicodeChar(char c) {
		switch (c) {
		// white unicode pieces
		case 'K':
			return (char) 0x2654; // King
		case 'Q':
			return (char) 0x2655; // Queen
		case 'R':
			return (char) 0x2656; // Rook
		case 'B':
			return (char) 0x2657; // Bishop
		case 'N':
			return (char) 0x2658; // Knight
		case 'P':
			return (char) 0x2659; // Pawn
			// black unicode pieces
		case 'k':
			return (char) 0x265A; // King
		case 'q':
			return (char) 0x265B; // Queen
		case 'r':
			return (char) 0x265C; // Rook
		case 'b':
			return (char) 0x265D; // Bishop
		case 'n':
			return (char) 0x265E; // Knight
		case 'p':
			return (char) 0x265F; // Pawn
		default:
			return c;
		}
	}

	public static String toUnicode(String s) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < s.length(); i++)
			sb.append(getUnicodeChar(s.charAt(i)));

		return sb.toString();
	}

	// Comment format:
	// @formatter:off
	// {
	//   ev=0.13, 		-- evaluation score.
	//   d=22,          -- depth
	//   pd=O-O,        -- predicted response
	//   mt=00:02:45,   -- move time
	//   tl=01:57:44,   -- time left
	//   s=26048 kN/s,  -- speed
	//   n=4317604221,  -- ???
	//   pv=..., 		-- principal variation
	//   tb=0, 			-- tablebase hits
	//   R50=50, 		-- ???
	//   wv=0.13, 		-- white value (evaluation relative to white)
	// }
	// @formatter:on
	private String getCommentData(String comment, String name) {
		if (comment != null) {
			String regex = " " + name + "=([^,]*),";

			Pattern pattern = Pattern.compile(regex);
			// In case you would like to ignore case sensitivity you could use
			// this
			// statement
			// Pattern pattern = Pattern.compile("\\s+",
			// Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(comment);

			if (matcher.find()) {
				int start = matcher.start(1);
				int end = matcher.end(1);
				return comment.substring(start, end);
			}
		}

		return null;
	}

}
