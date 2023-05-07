package com.fathzer.jchess.fischerrandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StartPositionGenerator implements Supplier<List<PieceWithPosition>> {
	private static final Random RANDOM = new Random(System.currentTimeMillis());
	private static final Piece[][] WHITE_KRN = new Piece[][] {
			new Piece[] {Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_ROOK},
			new Piece[] {Piece.WHITE_KNIGHT, Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_KING, Piece.WHITE_ROOK},
			new Piece[] {Piece.WHITE_KNIGHT, Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK},
			new Piece[] {Piece.WHITE_KNIGHT, Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_ROOK, Piece.WHITE_KNIGHT},
			new Piece[] {Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT, Piece.WHITE_KING, Piece.WHITE_ROOK},
			new Piece[] {Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_KING, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK},
			new Piece[] {Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_KING, Piece.WHITE_ROOK, Piece.WHITE_KNIGHT},
			new Piece[] {Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK},
			new Piece[] {Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK, Piece.WHITE_KNIGHT},
			new Piece[] {Piece.WHITE_ROOK, Piece.WHITE_KING, Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT},
	};
	private static final Piece[][] BLACK_KRN = new Piece[][] {
		new Piece[] {Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_ROOK},
		new Piece[] {Piece.BLACK_KNIGHT, Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_KING, Piece.BLACK_ROOK},
		new Piece[] {Piece.BLACK_KNIGHT, Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK},
		new Piece[] {Piece.BLACK_KNIGHT, Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_ROOK, Piece.BLACK_KNIGHT},
		new Piece[] {Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT, Piece.BLACK_KING, Piece.BLACK_ROOK},
		new Piece[] {Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_KING, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK},
		new Piece[] {Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_KING, Piece.BLACK_ROOK, Piece.BLACK_KNIGHT},
		new Piece[] {Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK},
		new Piece[] {Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK, Piece.BLACK_KNIGHT},
		new Piece[] {Piece.BLACK_ROOK, Piece.BLACK_KING, Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT},
};
	
	public static final StartPositionGenerator INSTANCE = new StartPositionGenerator();

	@Override
	public List<PieceWithPosition> get() {
		return fromPositionNumber(RANDOM.nextInt(960));
	}
	
	public List<PieceWithPosition> fromPositionNumber(int position) {
		if (position<0 || position>=960) {
			throw new IllegalArgumentException();
		}
		final List<PieceWithPosition> pieces = new ArrayList<>();
		// Add pawns
		IntStream.range(8, 16).forEach(i->pieces.add(new PieceWithPosition(Piece.BLACK_PAWN, i)));
		IntStream.range(48, 56).forEach(i->pieces.add(new PieceWithPosition(Piece.WHITE_PAWN, i)));
		
		// Add bishops
		final int whiteCellBishop = (position % 4)*2;
		pieces.add(new PieceWithPosition(Piece.WHITE_BISHOP, 57 + whiteCellBishop));
		pieces.add(new PieceWithPosition(Piece.BLACK_BISHOP, 1 + whiteCellBishop));
		position = position/4;
		final int blackCellBishop = (position % 4)*2;
		pieces.add(new PieceWithPosition(Piece.WHITE_BISHOP, 56 + blackCellBishop));
		pieces.add(new PieceWithPosition(Piece.BLACK_BISHOP, blackCellBishop));
		position = position/4;
		
		// We will maintain a list of free cells in order to position the remaining piece
		final List<Integer> freeCells = IntStream.range(0, 8).mapToObj(Integer::valueOf).collect(Collectors.toList());
		freeCells.remove(Integer.valueOf(whiteCellBishop+1));
		freeCells.remove(Integer.valueOf(blackCellBishop));
		// Add queens
		final int queenPosition = freeCells.remove(position%6);
		pieces.add(new PieceWithPosition(Piece.WHITE_QUEEN, 56 + queenPosition));
		pieces.add(new PieceWithPosition(Piece.BLACK_QUEEN, queenPosition));
		
		// Add rest of pieces
		addKRN(pieces, freeCells, WHITE_KRN[position/6], 56);
		addKRN(pieces, freeCells, BLACK_KRN[position/6], 0);
		return pieces;
	}
	
	private void addKRN(List<PieceWithPosition> pieces, List<Integer> positions, Piece[] krn, int offset) {
		IntStream.range(0, positions.size()).mapToObj(i -> new PieceWithPosition(krn[i], offset+positions.get(i))).forEach(pieces::add);
	}
}
