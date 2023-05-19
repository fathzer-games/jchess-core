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
import java.util.function.IntFunction;
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
import com.fathzer.jchess.Notation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FENParser {
	private static final Map<Character, IntFunction<PieceWithPosition>> BUILDERS;
	
	public static final String NEW_STANDARD_GAME = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	
	static {
		BUILDERS = new HashMap<>();
		BUILDERS.put('p', p->new PieceWithPosition(BLACK_PAWN,p));
		BUILDERS.put('P', p->new PieceWithPosition(WHITE_PAWN, p));
		BUILDERS.put('r', p->new PieceWithPosition(BLACK_ROOK, p));
		BUILDERS.put('R', p->new PieceWithPosition(WHITE_ROOK, p));
		BUILDERS.put('n', p->new PieceWithPosition(BLACK_KNIGHT, p));
		BUILDERS.put('N', p->new PieceWithPosition(WHITE_KNIGHT, p));
		BUILDERS.put('b', p->new PieceWithPosition(BLACK_BISHOP, p));
		BUILDERS.put('B', p->new PieceWithPosition(WHITE_BISHOP, p));
		BUILDERS.put('q', p->new PieceWithPosition(BLACK_QUEEN, p));
		BUILDERS.put('Q', p->new PieceWithPosition(WHITE_QUEEN, p));
		BUILDERS.put('k', p->new PieceWithPosition(BLACK_KING, p));
		BUILDERS.put('K', p->new PieceWithPosition(WHITE_KING, p));
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
		final List<PieceWithPosition> pieces = getPieces(tokens[0]);
		final Color color = getColor(tokens[1]);
		final Collection<Castling> castlings = getCastlings(tokens[2]);
		final int enPassant = getPosition(tokens[3], dimension);
		final int halfMoveCount = Integer.parseInt(tokens[4]);
		final int moveNumber = Integer.parseInt(tokens[5]);
		if (dimension.getWidth()==8 && dimension.getHeight()==8) {
			final int[] rooks = getInitialRookPositions(dimension, pieces, castlings, tokens[2]);
			if (rooks!=null) {
				return new com.fathzer.jchess.fischerrandom.Chess960Board(pieces, color, castlings, rooks, enPassant, halfMoveCount, moveNumber);
			} else {
				return new com.fathzer.jchess.standard.StandardBoard(pieces, color, castlings, enPassant, halfMoveCount, moveNumber);
			}
		} else {
			return new StandardBoard(pieces, color, castlings, enPassant, halfMoveCount, moveNumber);
		}
	}
	
	private int[] getInitialRookPositions(Dimension dimension, List<PieceWithPosition> pieces, Collection<Castling> castlings, String castlingsString) {
		try {
			if (castlings.isEmpty()) {
				return null;
			}
			final int[] positions = new int[Castling.ALL.size()];
			Arrays.fill(positions, -1);
			final int blackKing = getPosition(pieces, p->p.getPiece()==BLACK_KING);
			final int whiteKing = getPosition(pieces, p->p.getPiece()==WHITE_KING);
			boolean isDefault = blackKing==dimension.getWidth()/2; // isDefault is true is Kings are at their default position
			for (Castling castling : castlings) {
				final int kingPosition = castling.getColor()==WHITE ? whiteKing : blackKing;
				//TODO Support inner rook position as start position
				// Initial rook position is the furthest rook from the king
				int rookPosition = getFurthest(dimension, pieces, castling.getColor()==BLACK ? BLACK_ROOK : WHITE_ROOK, kingPosition, castling.getSide());
				isDefault = isDefault && rookPosition==getStandardRookPosition(dimension, castling);
				positions[castling.ordinal()] = rookPosition;
			}
			return isDefault ? null : positions;
		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private int getPosition(List<PieceWithPosition> pieces, Predicate<PieceWithPosition> p) {
		return pieces.stream().filter(p).findAny().orElseThrow().getPosition();
	}
	
	private int getStandardRookPosition(Dimension dimension, Castling castling) {
		int pos = castling.getColor()==BLACK ? 0 : dimension.getPosition(dimension.getHeight()-1, 0);
		if (castling.getSide()==KING) {
			pos += dimension.getWidth()-1;
		}
		return pos;
	}
	
	private int getFurthest(Dimension dimension, List<PieceWithPosition> pieces, Piece searchedPiece, int kingPosition, Castling.Side side) {
		final int kingRow = dimension.getRow(kingPosition);
		final Predicate<PieceWithPosition> sidePredicate = p -> side==KING ? p.getPosition()>kingPosition : p.getPosition()<kingPosition;
		final Predicate<PieceWithPosition> pieceOnRow = p -> p.getPiece()==searchedPiece && dimension.getRow(p.getPosition())==kingRow;
		final IntStream sortedPositions = pieces.stream().filter(pieceOnRow.and(sidePredicate)).mapToInt(PieceWithPosition::getPosition).sorted();
		OptionalInt result = side==KING ? sortedPositions.max() : sortedPositions.min();
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

	private static int getPosition(String pos, Dimension dimension) {
		return "-".equals(pos) ? -1 : Notation.toPosition(pos, dimension);
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
