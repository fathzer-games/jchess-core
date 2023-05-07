package com.fathzer.jchess.fen;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.standard.ChessBoard;
import com.fathzer.jchess.Notation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FENParser {
	private static final Map<Character, IntFunction<PieceWithPosition>> BUILDERS;
	
	public static final String NEW_STANDARD_GAME = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	
	static {
		BUILDERS = new HashMap<>();
		BUILDERS.put('p', p->new PieceWithPosition(Piece.BLACK_PAWN,p));
		BUILDERS.put('P', p->new PieceWithPosition(Piece.WHITE_PAWN, p));
		BUILDERS.put('r', p->new PieceWithPosition(Piece.BLACK_ROOK, p));
		BUILDERS.put('R', p->new PieceWithPosition(Piece.WHITE_ROOK, p));
		BUILDERS.put('n', p->new PieceWithPosition(Piece.BLACK_KNIGHT, p));
		BUILDERS.put('N', p->new PieceWithPosition(Piece.WHITE_KNIGHT, p));
		BUILDERS.put('b', p->new PieceWithPosition(Piece.BLACK_BISHOP, p));
		BUILDERS.put('B', p->new PieceWithPosition(Piece.WHITE_BISHOP, p));
		BUILDERS.put('q', p->new PieceWithPosition(Piece.BLACK_QUEEN, p));
		BUILDERS.put('Q', p->new PieceWithPosition(Piece.WHITE_QUEEN, p));
		BUILDERS.put('k', p->new PieceWithPosition(Piece.BLACK_KING, p));
		BUILDERS.put('K', p->new PieceWithPosition(Piece.WHITE_KING, p));
	}

	public static String to(Board<Move> board) {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i <board.getDimension().getHeight() ; i++) {
			b.append(getRow(board, i));
			if (i!=board.getDimension().getHeight()-1) {
				b.append('/');
			}
		}
		b.append(' ');
		b.append(Color.WHITE.equals(board.getActiveColor())?'w':'b');
		b.append(' ');
		addCastlings(b, board);
		b.append(' ');
		b.append(board.getEnPassant()<0?"-":Notation.toString(board.getEnPassant(),board.getDimension()));
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
			final Piece piece = board.getPiece(board.getDimension().getPosition(row, col));
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
		String[] tokens = fen.split(" ");
		if (tokens.length!=6) {
			throw new IllegalArgumentException("This FEN definition is invalid: "+fen);
		}
		final Dimension dimension = getDimension(tokens[0]);
		if (dimension.getWidth()==8 && dimension.getHeight()==8) {
			return new com.fathzer.jchess.standard.ChessBoard(getPieces(tokens[0]), getColor(tokens[1]), getCastings(tokens[2]), getPosition(tokens[3], dimension), Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]));
		} else {
			return new ChessBoard(getPieces(tokens[0]), getColor(tokens[1]), getCastings(tokens[2]), getPosition(tokens[3], dimension), Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]));
		}
	}

	private static Collection<Castling> getCastings(String code) {
		if ("-".equals(code)) {
			return Collections.emptyList();
		} else {
			return code.chars().mapToObj(c -> toCastling((char)c)).collect(Collectors.toList());
		}
	}

	private static Castling toCastling(char c) {
		final Optional<Castling> value = Castling.ALL.stream().filter(x -> x.getCode().charAt(0)==c).findFirst();
		return value.orElseThrow(IllegalArgumentException::new);
	}

	private static int getPosition(String pos, Dimension dimension) {
		return "-".equals(pos) ? -1 : Notation.toPosition(pos, dimension);
	}

	private static Color getColor(String code) {
		if ("-".equals(code)) {
			return null;
		} else if ("w".equals(code)) {
			return Color.WHITE;
		} else if ("b".equals(code)) {
			return Color.BLACK;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public static List<PieceWithPosition> getPieces(String str) {
		final Dimension dimension = getDimension(str);
		final String[] lines = str.split("/");
		final List<PieceWithPosition> result = new LinkedList<>();
		for (int i = 0; i < lines.length; i++) {
			result.addAll(lineToPieces(i, lines[i], dimension));
		}
		return result;
	}

	private static Collection<PieceWithPosition> lineToPieces(int row, String str, Dimension dimension) {
		final List<PieceWithPosition> result = new LinkedList<>();
		int x = 0;
		for (int i = 0; i < str.length(); i++) {
			final char code = str.charAt(i);
			final IntFunction<PieceWithPosition> b = BUILDERS.get(code);
			if (b==null) {
				x+=Integer.parseInt(Character.toString(code));
			} else {
				final int pos = dimension.getPosition(row, x);
				result.add(b.apply(pos));
				x++;
			}
		}
		return result;
	}
	
	private static Dimension getDimension(String str) {
		final String[] lines = str.split("/");
		int width = 0;
		for (int i = 0; i < lines[0].length(); i++) {
			final char code = str.charAt(i);
			final IntFunction<PieceWithPosition> b = BUILDERS.get(code);
			width = width + (b==null ? Integer.parseInt(Character.toString(code)) : 1);
		}
		return new Dimension(width, lines.length);
	}
}
