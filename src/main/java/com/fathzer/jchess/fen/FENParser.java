package com.fathzer.jchess.fen;

import static com.fathzer.games.Color.BLACK;
import static com.fathzer.games.Color.WHITE;
import static com.fathzer.jchess.Castling.Side.KING;
import static com.fathzer.jchess.Piece.BLACK_BISHOP;
import static com.fathzer.jchess.Piece.BLACK_KING;
import static com.fathzer.jchess.Piece.BLACK_KNIGHT;
import static com.fathzer.jchess.Piece.BLACK_PAWN;
import static com.fathzer.jchess.Piece.BLACK_QUEEN;
import static com.fathzer.jchess.Piece.BLACK_ROOK;
import static com.fathzer.jchess.Piece.WHITE_BISHOP;
import static com.fathzer.jchess.Piece.WHITE_KING;
import static com.fathzer.jchess.Piece.WHITE_KNIGHT;
import static com.fathzer.jchess.Piece.WHITE_PAWN;
import static com.fathzer.jchess.Piece.WHITE_QUEEN;
import static com.fathzer.jchess.Piece.WHITE_ROOK;

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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.standard.StandardBoard;

public class FENParser implements Supplier<Board<Move>> {
	private static final Map<Character, Piece> CODE_TO_PIECE;
	private final Dimension dimension;
	private final List<PieceWithPosition> pieces;
	private final Color color;
	private final Collection<Castling> castlings;
	private final int[] rookPositions;
	private final int enPassant;
	private final int halfMoveCount;
	private final int moveNumber;
	
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
	
	public FENParser(String fen) {
		String[] tokens = fen.split(" ");
		if (tokens.length!=6) {
			throw new IllegalArgumentException("This FEN definition is invalid: "+fen);
		}
		this.dimension = getDimension(tokens[0]);
		this.pieces = getPieces(tokens[0]);
		this.color = getColor(tokens[1]);
		this.castlings = getCastlings(tokens[2]);
		this.rookPositions = getInitialRookColumns(dimension, pieces, castlings, tokens[2]);
		this.enPassant = "-".equals(tokens[3]) ? -1 : getColumn(tokens[3]);
		this.halfMoveCount = Integer.parseInt(tokens[4]);
		this.moveNumber = Integer.parseInt(tokens[5]);
	}
	
	@Override
	public Board<Move> get() {
		if (dimension.getWidth()==8 && dimension.getHeight()==8) {
			if (rookPositions!=null) {
				return new com.fathzer.jchess.chess960.Chess960Board(pieces, color, castlings, rookPositions, enPassant, halfMoveCount, moveNumber);
			} else {
				return new com.fathzer.jchess.standard.StandardBoard(pieces, color, castlings, enPassant, halfMoveCount, moveNumber);
			}
		} else {
			return new StandardBoard(pieces, color, castlings, enPassant, halfMoveCount, moveNumber);
		}
	}

	private Dimension getDimension(String str) {
		final String[] lines = str.split("/");
		int width = 0;
		for (int i = 0; i < lines[0].length(); i++) {
			final char code = str.charAt(i);
			final Piece p = CODE_TO_PIECE.get(code);
			width = width + (p==null ? Integer.parseInt(Character.toString(code)) : 1);
		}
		return new Dimension(width, lines.length);
	}
	
	protected List<PieceWithPosition> getPieces(String str) {
		final String[] lines = str.split("/");
		final List<PieceWithPosition> result = new LinkedList<>();
		for (int i = 0; i < lines.length; i++) {
			result.addAll(lineToPieces(i, lines[i], dimension));
		}
		return result;
	}

	public List<PieceWithPosition> getPieces() {
		return pieces;
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

	private Color getColor(String code) {
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

	private Collection<Castling> getCastlings(String code) {
		if ("-".equals(code)) {
			return Collections.emptyList();
		} else {
			return code.chars().mapToObj(c -> toCastling((char)c)).collect(Collectors.toList());
		}
	}

	private Castling toCastling(char c) {
		final Optional<Castling> value = Castling.ALL.stream().filter(x -> x.getCode().charAt(0)==c).findFirst();
		if (value.isPresent()) {
			return value.get();
		}
		// It should be X-FEN castling. Get castling from their column
		if (Character.isUpperCase(c)) {
			// White color
			int col = c-'A';
			return col > getKingsColumn(WHITE) ? Castling.WHITE_KING_SIDE : Castling.WHITE_QUEEN_SIDE; 
		} else {
			// Black color
			int col = c-'a';
			return col > getKingsColumn(BLACK) ? Castling.BLACK_KING_SIDE : Castling.BLACK_QUEEN_SIDE; 
		}
	}
	
	private int getKingsColumn(Color color) {
		return pieces.stream().filter(p -> p.getPiece().getKind()==PieceKind.KING && p.getPiece().getColor()==color).findFirst().orElseThrow(()-> new IllegalArgumentException("No king found")).getColumn();
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
}
