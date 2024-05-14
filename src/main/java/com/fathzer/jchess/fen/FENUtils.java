package com.fathzer.jchess.fen;

import java.util.stream.IntStream;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Castling.Side;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FENUtils {
	public static final String NEW_STANDARD_GAME = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	
	public static String to(Board<Move> board) {
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

	private static void addCastlings(final StringBuilder b, Board<Move> board) {
		final int initialSize = b.length();
		Castling.ALL.stream().filter(board::hasCastling).forEach(c -> b.append(getCode(board, c)));
		if (b.length()==initialSize) {
			b.append('-');
		}
	}
	
	private static String getCode(Board<Move> board, Castling castling) {
		final int rookPos = board.getInitialRookPosition(castling);
		int column = board.getCoordinatesSystem().getColumn(rookPos);
		if (column==0 || column==board.getDimension().getWidth()-1) {
			// If standard rook position, return the FEN code
			return castling.getCode();
		}
		// Check if we should use X-FEN code => Check if there's another rook farther from the king than the initial rook's position
		final boolean isWhite = castling.getColor()==Color.WHITE;
		final Piece searched = isWhite ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
		final IntStream indexes = castling.getSide()==Side.KING ? IntStream.range(rookPos+1, rookPos-column+board.getDimension().getWidth()) :
			IntStream.range(rookPos-column, rookPos);
		final boolean isFursthest = indexes.noneMatch(i -> searched.equals(board.getPiece(i)));
		if (isFursthest) {
			return castling.getCode();
		} else {
			return String.valueOf((char)((isWhite ? 'A' : 'a')+column)); 
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
