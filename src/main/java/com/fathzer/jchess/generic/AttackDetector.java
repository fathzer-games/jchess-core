package com.fathzer.jchess.generic;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

class AttackDetector {
	private static final List<Direction> BLACK_PAWN_THREAT_DIRECTIONS = Arrays.asList(Direction.NORTH_EAST, Direction.NORTH_WEST);
	private static final List<Direction> WHITE_PAWN_THREAT_DIRECTIONS = Arrays.asList(Direction.SOUTH_EAST, Direction.SOUTH_WEST);
	private final Board<?> board;

	public AttackDetector(Board<?> board) {
		this.board = board;
	}
	
	public boolean isAttacked(int position, Color color) {
		final BoardExplorer explorer = new SkipFirstExplorer(board.getCoordinatesSystem(), position);

		// check for knight
		if (check(explorer, PieceKind.KNIGHT.getDirections(), 1, color, p -> PieceKind.KNIGHT.equals(p.getKind()))) {
			return true;
		}
		// check for KING threads
		if (check(explorer, PieceKind.KING.getDirections(), 1, color, p -> PieceKind.KING.equals(p.getKind()))) {
			return true;
		}
		
		// check for others horizontal or vertical threads from ROOK and QUEEN
		if (check(explorer, PieceKind.ROOK.getDirections(), Integer.MAX_VALUE, color, p -> PieceKind.ROOK.equals(p.getKind()) || PieceKind.QUEEN.equals(p.getKind()))) {
			return true;
		}
		
		// check for others diagonal threads from BISHOP and QUEEN
		if (check(explorer, PieceKind.BISHOP.getDirections(), Integer.MAX_VALUE, color, p -> PieceKind.BISHOP.equals(p.getKind()) || PieceKind.QUEEN.equals(p.getKind()))) {
			return true;
		}

		// Finally check for pawns threats
		Collection<Direction> directions = Color.BLACK.equals(color) ? BLACK_PAWN_THREAT_DIRECTIONS : WHITE_PAWN_THREAT_DIRECTIONS;
		return check(explorer, directions, 1, color, p -> PieceKind.PAWN.equals(p.getKind()));
	}
	
	private boolean check(BoardExplorer explorer, Collection<Direction> directions, int maxIteration, Color color, Predicate<Piece> validator) {
		return directions.stream().anyMatch(d -> check(explorer, d, maxIteration, color, validator));
	}
	
	private boolean check(BoardExplorer explorer, Direction direction, int maxIteration, Color color, Predicate<Piece> validator)  {
		explorer.start(direction);
		int iteration = 0;
		while (explorer.hasNext()) {
			final int target = explorer.next();
			final Piece piece = board.getPiece(target);
			boolean isFree = piece==null;
			if (!isFree && piece.getColor().equals(color)) {
				return validator.test(piece);
			}
			iteration++;
			if (iteration>=maxIteration || !isFree) {
				break;
			}
		}
		return false;
	}
}
