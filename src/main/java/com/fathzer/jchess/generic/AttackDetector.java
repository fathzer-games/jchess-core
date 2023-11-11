package com.fathzer.jchess.generic;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Predicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

/** A class that can tests if a cell is attacked by the pieces of a color.
 */
public class AttackDetector {
	static final Collection<Direction> BLACK_PAWN_THREAT_DIRECTIONS = EnumSet.of(Direction.NORTH_EAST, Direction.NORTH_WEST);
	static final Collection<Direction> WHITE_PAWN_THREAT_DIRECTIONS = EnumSet.of(Direction.SOUTH_EAST, Direction.SOUTH_WEST);

	private final DirectionExplorer explorer;
	
	/** Constructor.
	 * @param explorer A direction explorer
	 */
	public AttackDetector(DirectionExplorer explorer) {
		this.explorer = explorer;
	}
	
	/** Checks if a cell is attacked by the pieces of a color.
	 * @param position the cell's position
	 * @param color The attacking color
	 * @return true if the cell is attacked
	 */
	public boolean isAttacked(int position, Color color) {
		explorer.reset(position);
		
		// check for knight
		if (checkNear(explorer, PieceKind.KNIGHT.getDirections(), color, p -> PieceKind.KNIGHT.equals(p.getKind()))) {
			return true;
		}
		// check for KING threats
		if (checkNear(explorer, PieceKind.KING.getDirections(), color, p -> PieceKind.KING.equals(p.getKind()))) {
			return true;
		}
		
		// check for others horizontal or vertical threats from ROOK and QUEEN
		if (check(explorer, PieceKind.ROOK.getDirections(), color, p -> PieceKind.ROOK.equals(p.getKind()) || PieceKind.QUEEN.equals(p.getKind()))) {
			return true;
		}
		
		// check for others diagonal threats from BISHOP and QUEEN
		if (check(explorer, PieceKind.BISHOP.getDirections(), color, p -> PieceKind.BISHOP.equals(p.getKind()) || PieceKind.QUEEN.equals(p.getKind()))) {
			return true;
		}

		// Finally check for pawns threats
		Collection<Direction> directions = Color.BLACK.equals(color) ? BLACK_PAWN_THREAT_DIRECTIONS : WHITE_PAWN_THREAT_DIRECTIONS;
		return checkNear(explorer, directions, color, p -> PieceKind.PAWN.equals(p.getKind()));
	}
	
	private boolean check(DirectionExplorer explorer, Collection<Direction> directions, Color color, Predicate<Piece> validator) {
		for (Direction d:directions) {
			if (check(explorer, d, color, validator)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkNear(DirectionExplorer explorer, Collection<Direction> directions, Color color, Predicate<Piece> validator) {
		for (Direction d:directions) {
			if (checkNear(explorer, d, color, validator)) {
				return true;
			}
		}
		return false;
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
