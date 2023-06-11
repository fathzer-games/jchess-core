package com.fathzer.jchess.generic;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

class AttackDetector {
	static final List<Direction> BLACK_PAWN_THREAT_DIRECTIONS = Arrays.asList(Direction.NORTH_EAST, Direction.NORTH_WEST);
	static final List<Direction> WHITE_PAWN_THREAT_DIRECTIONS = Arrays.asList(Direction.SOUTH_EAST, Direction.SOUTH_WEST);
	private final Board<?> board;

	public AttackDetector(Board<?> board) {
		this.board = board;
	}
	
	public boolean isAttacked(int position, Color color) {
		final DirectionExplorer explorer = board.getDirectionExplorer(position);

		// check for knight
		if (checkNear(explorer, PieceKind.KNIGHT.getDirections(), color, p -> PieceKind.KNIGHT.equals(p.getKind()))) {
			return true;
		}
		// check for KING threads
		if (checkNear(explorer, PieceKind.KING.getDirections(), color, p -> PieceKind.KING.equals(p.getKind()))) {
			return true;
		}
		
		// check for others horizontal or vertical threads from ROOK and QUEEN
		if (check(explorer, PieceKind.ROOK.getDirections(), color, p -> PieceKind.ROOK.equals(p.getKind()) || PieceKind.QUEEN.equals(p.getKind()))) {
			return true;
		}
		
		// check for others diagonal threads from BISHOP and QUEEN
		if (check(explorer, PieceKind.BISHOP.getDirections(), color, p -> PieceKind.BISHOP.equals(p.getKind()) || PieceKind.QUEEN.equals(p.getKind()))) {
			return true;
		}

		// Finally check for pawns threats
		Collection<Direction> directions = Color.BLACK.equals(color) ? BLACK_PAWN_THREAT_DIRECTIONS : WHITE_PAWN_THREAT_DIRECTIONS;
		return checkNear(explorer, directions, color, p -> PieceKind.PAWN.equals(p.getKind()));
	}
	
	private boolean check(DirectionExplorer explorer, Collection<Direction> directions, Color color, Predicate<Piece> validator) {
		return directions.stream().anyMatch(d -> check(explorer, d, color, validator));
	}

	private boolean checkNear(DirectionExplorer explorer, Collection<Direction> directions, Color color, Predicate<Piece> validator) {
		return directions.stream().anyMatch(d -> checkNear(explorer, d, color, validator));
	}

	private boolean check(DirectionExplorer explorer, Direction direction, Color color, Predicate<Piece> validator)  {
		explorer.start(direction);
		while (explorer.next()) {
			final Piece piece = explorer.getPiece();
			if (piece!=null) {
				return piece.getColor().equals(color) && validator.test(piece);
			}
		}
		return false;
	}

	private boolean checkNear(DirectionExplorer explorer, Direction direction, Color color, Predicate<Piece> validator)  {
		explorer.start(direction);
		if (explorer.next()) {
			final Piece piece = explorer.getPiece();
			if (piece!=null) {
				return piece.getColor().equals(color) && validator.test(piece);
			}
		}
		return false;
	}
}
