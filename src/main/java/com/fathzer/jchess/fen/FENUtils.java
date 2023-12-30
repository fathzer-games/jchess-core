package com.fathzer.jchess.fen;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FENUtils {
	public static final String NEW_STANDARD_GAME = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	
	public static String to(Board<Move> board) {
		//TODO output X-FEN castling when chess960 needs it
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i <board.getDimension().getHeight() ; i++) {
			b.append(getRow(board, i));
			if (i!=board.getDimension().getHeight()-1) {
				b.append('/');
			}
		}
		b.append(' ');
		b.append(board.isWhiteToMove()?'w':'b');
		b.append(' ');
		addCastlings(b, board);
		b.append(' ');
		b.append(board.getEnPassant()<0?"-":board.getCoordinatesSystem().getAlgebraicNotation(board.getEnPassant()));
		b.append(' ');
		b.append(board.getHalfMoveCount());
		b.append(' ');
		b.append(board.getMoveNumber());
		return b.toString();
	}

	protected static void addCastlings(final StringBuilder b, Board<Move> board) {
		final int initialSize = b.length();
		Castling.ALL.stream().filter(board::hasCastling).forEach(c -> b.append(c.getCode()));
		if (b.length()==initialSize) {
			b.append('-');
		}
	}

	private static CharSequence getRow(Board<Move> board, int row) {
		final StringBuilder b = new StringBuilder();
		int emptyCount = 0;
		for (int col = 0; col < board.getDimension().getHeight(); col++) {
			final Piece piece = board.getPiece(board.getCoordinatesSystem().getIndex(row, col));
			if (piece==null) {
				emptyCount++;
			} else {
				if (emptyCount>0) {
					b.append(emptyCount);
					emptyCount = 0;
				}
				b.append(piece.getNotation());
			}
		}
		if (emptyCount>0) {
			b.append(emptyCount);
		}
		return b;
	}

	public static Board<Move> from(String fen) {
		return new FENParser(fen).get();
	}

}
