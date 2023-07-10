package com.fathzer.jchess.fen;

import static com.fathzer.games.Color.*;
import static com.fathzer.jchess.Castling.Side.*;
import static com.fathzer.jchess.Piece.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.standard.StandardBoard;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FENParser {
	private static final Map<Character, Piece> CODE_TO_PIECE;
	
	public static final String NEW_STANDARD_GAME = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	
	static {
		CODE_TO_PIECE = new HashMap<>();
		CODE_TO_PIECE.put('p', BLACK_PAWN);
		CODE_TO_PIECE.put('P', WHITE_PAWN);
		CODE_TO_PIECE.put('r', BLACK_ROOK);
		CODE_TO_PIECE.put('R', WHITE_ROOK);
		CODE_TO_PIECE.put('n', BLACK_KNIGHT);
		CODE_TO_PIECE.put('N', WHITE_KNIGHT);
		CODE_TO_PIECE.put('b', BLACK_BISHOP);
		CODE_TO_PIECE.put('B', WHITE_BISHOP);
		CODE_TO_PIECE.put('q', BLACK_QUEEN);
		CODE_TO_PIECE.put('Q', WHITE_QUEEN);
		CODE_TO_PIECE.put('k', BLACK_KING);
		CODE_TO_PIECE.put('K', WHITE_KING);
	}

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
		b.append(WHITE.equals(board.getActiveColor())?'w':'b');
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
		String[] tokens = fen.split(" ");
		if (tokens.length!=6) {
			throw new IllegalArgumentException("This FEN definition is invalid: "+fen);
		}
		final Dimension dimension = getDimension(tokens[0]);
		final List<PieceWithPosition> pieces = getPieces(tokens[0]);
		final Color color = getColor(tokens[1]);
		final Collection<Castling> castlings = getCastlings(tokens[2]);
		final int enPassant = "-".equals(tokens[3]) ? -1 : getColumn(tokens[3]);
		final int halfMoveCount = Integer.parseInt(tokens[4]);
		final int moveNumber = Integer.parseInt(tokens[5]);
		if (dimension.getWidth()==8 && dimension.getHeight()==8) {
			final int[] rooks = getInitialRookColumns(dimension, pieces, castlings, tokens[2]);
			if (rooks!=null) {
				return new com.fathzer.jchess.chess960.Chess960Board(pieces, color, castlings, rooks, enPassant, halfMoveCount, moveNumber);
			} else {
				return new com.fathzer.jchess.standard.StandardBoard(pieces, color, castlings, enPassant, halfMoveCount, moveNumber);
			}
		} else {
			return new StandardBoard(pieces, color, castlings, enPassant, halfMoveCount, moveNumber);
		}
	}

	private int getColumn(String algebraicNotation) {
		if (algebraicNotation.isEmpty()) {
			throw new IllegalArgumentException();
		}
		final int column = algebraicNotation.charAt(0)-'a';
		if (column<0) {
			throw new IllegalArgumentException(); 
		}
		return column;
	}
	
	private int[] getInitialRookColumns(Dimension dimension, List<PieceWithPosition> pieces, Collection<Castling> castlings, String castlingsString) {
		try {
			if (castlings.isEmpty()) {
				return null;
			}
			final int[] positions = new int[Castling.ALL.size()];
			Arrays.fill(positions, -1);
			final int blackKingColumn = getColumn(pieces, p->p.getPiece()==BLACK_KING);
			final int whiteKingColumn = getColumn(pieces, p->p.getPiece()==WHITE_KING);
			final int defaultKingColumn = dimension.getWidth()/2;
			boolean isDefault = true; // isDefault will remain true if kings and rooks involved in castling are at their default position
			for (Castling castling : castlings) {
				final int kingRow = castling.getColor()==WHITE ? dimension.getHeight()-1 : 0;
				final int kingColumn = castling.getColor()==WHITE ? whiteKingColumn : blackKingColumn;
				//TODO Support inner rook position as start position
				// Initial rook position is the furthest rook from the king
				int rookPosition = getFurthest(pieces, castling.getColor()==BLACK ? BLACK_ROOK : WHITE_ROOK, kingRow, kingColumn, castling.getSide());
				isDefault = isDefault && rookPosition==getStandardRookColumn(dimension, castling) && kingColumn==defaultKingColumn;
				positions[castling.ordinal()] = rookPosition;
			}
			return isDefault ? null : positions;
		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private int getColumn(List<PieceWithPosition> pieces, Predicate<PieceWithPosition> p) {
		return pieces.stream().filter(p).findAny().orElseThrow().getColumn();
	}
	
	private int getStandardRookColumn(Dimension dimension, Castling castling) {
		return castling.getSide()==KING ? dimension.getWidth()-1 : 0;
	}
	
	private int getFurthest(List<PieceWithPosition> pieces, Piece searchedPiece, int kingRow, int kingColumn, Castling.Side side) {
		final Predicate<PieceWithPosition> sidePredicate = p -> side==KING ? p.getColumn()>kingColumn : p.getColumn()<kingColumn;
		final Predicate<PieceWithPosition> pieceOnRow = p -> p.getPiece()==searchedPiece && p.getRow()==kingRow;
		final IntStream sortedColumns = pieces.stream().filter(pieceOnRow.and(sidePredicate)).mapToInt(PieceWithPosition::getColumn).sorted();
		OptionalInt result = side==KING ? sortedColumns.max() : sortedColumns.min();
		return result.getAsInt();
	}

	private static Collection<Castling> getCastlings(String code) {
		if ("-".equals(code)) {
			return Collections.emptyList();
		} else {
			return code.chars().mapToObj(c -> toCastling((char)c)).collect(Collectors.toList());
		}
	}

	private static Castling toCastling(char c) {
		//TODO Does not support X-FEN castling
		final Optional<Castling> value = Castling.ALL.stream().filter(x -> x.getCode().charAt(0)==c).findFirst();
		return value.orElseThrow(IllegalArgumentException::new);
	}

	private static Color getColor(String code) {
		if ("-".equals(code)) {
			return null;
		} else if ("w".equals(code)) {
			return WHITE;
		} else if ("b".equals(code)) {
			return BLACK;
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
			final Piece p = CODE_TO_PIECE.get(code);
			if (p==null) {
				x+=Integer.parseInt(Character.toString(code));
			} else {
				result.add(new PieceWithPosition(p, row, x));
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
			final Piece p = CODE_TO_PIECE.get(code);
			width = width + (p==null ? Integer.parseInt(Character.toString(code)) : 1);
		}
		return new Dimension(width, lines.length);
	}
}
