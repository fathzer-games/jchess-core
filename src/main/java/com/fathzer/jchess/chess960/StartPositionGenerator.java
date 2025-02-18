package com.fathzer.jchess.chess960;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.games.Color;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StartPositionGenerator implements Supplier<Chess960Board> {
	private static final Random RANDOM = new Random(System.currentTimeMillis());
	private static final Piece[][] WHITE_KRN = {
		{Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_ROOK},
		{Piece.WHITE_KNIGHT, Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_KING, Piece.WHITE_ROOK},
		{Piece.WHITE_KNIGHT, Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK},
		{Piece.WHITE_KNIGHT, Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_ROOK, Piece.WHITE_KNIGHT},
		{Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT, Piece.WHITE_KING, Piece.WHITE_ROOK},
		{Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_KING, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK},
		{Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_KING, Piece.WHITE_ROOK, Piece.WHITE_KNIGHT},
		{Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK},
		{Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK, Piece.WHITE_KNIGHT},
		{Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT},
	};
	private static final Piece[][] BLACK_KRN = {
		{Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_ROOK},
		{Piece.BLACK_KNIGHT, Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_KING, Piece.BLACK_ROOK},
		{Piece.BLACK_KNIGHT, Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK},
		{Piece.BLACK_KNIGHT, Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_ROOK, Piece.BLACK_KNIGHT},
		{Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT, Piece.BLACK_KING, Piece.BLACK_ROOK},
		{Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_KING, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK},
		{Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_KING, Piece.BLACK_ROOK, Piece.BLACK_KNIGHT},
		{Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK},
		{Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK, Piece.BLACK_KNIGHT},
		{Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT},
	};
	
	public static final StartPositionGenerator INSTANCE = new StartPositionGenerator();

	@Override
	public Chess960Board get() {
		return fromPositionNumber(RANDOM.nextInt(960));
	}
	
	public Chess960Board fromPositionNumber(int position) {
		final List<PieceWithPosition> pieces = getPiecesFromPositionNumber(position);
		return new Chess960Board(pieces, Color.WHITE, Castling.ALL, buildRookColumns(pieces), -1, 0, 1);
	}

	private int[] buildRookColumns(List<PieceWithPosition> pieces) {
		final int[] rookColumns = new int[2];
		int kingColumn = -1;
		for (PieceWithPosition piece : pieces) {
			if (piece.getPiece()==Piece.WHITE_KING || piece.getPiece()==Piece.BLACK_KING) {
				kingColumn = piece.getColumn();
				break;
			}
		}
		for (PieceWithPosition piece : pieces) {
			if (piece.getPiece()==Piece.WHITE_ROOK) {
				final Castling.Side side = piece.getColumn()<kingColumn ? Castling.Side.QUEEN : Castling.Side.KING;
				rookColumns[side.ordinal()] = piece.getColumn();
			}
		}
		return rookColumns;
	}

	private List<PieceWithPosition> getPiecesFromPositionNumber(int position) {
		if (position<0 || position>=960) {
			throw new IllegalArgumentException();
		}
		final List<PieceWithPosition> pieces = new ArrayList<>();
		// Add pawns
		IntStream.range(0, 8).forEach(i->pieces.add(new PieceWithPosition(Piece.BLACK_PAWN, 1, i)));
		IntStream.range(0, 8).forEach(i->pieces.add(new PieceWithPosition(Piece.WHITE_PAWN, 6, i)));
		
		// Add bishops
		final int whiteCellBishop = 1+(position % 4)*2;
		pieces.add(new PieceWithPosition(Piece.BLACK_BISHOP, 0, whiteCellBishop));
		pieces.add(new PieceWithPosition(Piece.WHITE_BISHOP, 7, whiteCellBishop));
		position = position/4;
		final int blackCellBishop = (position % 4)*2;
		pieces.add(new PieceWithPosition(Piece.BLACK_BISHOP, 0, blackCellBishop));
		pieces.add(new PieceWithPosition(Piece.WHITE_BISHOP, 7, blackCellBishop));
		position = position/4;
		
		// We will maintain a list of free cells in order to position the remaining piece
		final List<Integer> freeCells = IntStream.range(0, 8).mapToObj(Integer::valueOf).collect(Collectors.toList());
		freeCells.remove(Integer.valueOf(whiteCellBishop));
		freeCells.remove(Integer.valueOf(blackCellBishop));
		// Add queens
		final int queenPosition = freeCells.remove(position%6);
		pieces.add(new PieceWithPosition(Piece.BLACK_QUEEN, 0, queenPosition));
		pieces.add(new PieceWithPosition(Piece.WHITE_QUEEN, 7, queenPosition));
		
		// Add remaining pieces
		addKRN(pieces, freeCells, BLACK_KRN[position/6], 0);
		addKRN(pieces, freeCells, WHITE_KRN[position/6], 7);
		return pieces;
	}
	
	private void addKRN(List<PieceWithPosition> pieces, List<Integer> positions, Piece[] krn, int row) {
		IntStream.range(0, positions.size()).mapToObj(i -> new PieceWithPosition(krn[i], row, positions.get(i))).forEach(pieces::add);
	}
}
