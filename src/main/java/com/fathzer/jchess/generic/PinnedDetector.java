package com.fathzer.jchess.generic;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

import java.util.function.IntFunction;

/** A class to detect pinned pieces.
 * <br>A pinned piece is a piece that can't move because it is the only protection of the King from an attack.
 * <br>Additionally, it gives the direction where the attack that pinned the piece is coming from.
 */
public class PinnedDetector implements IntFunction<Direction> {
	private Direction[] pinedMap;
	 
	public PinnedDetector(Board<Move> board) {
		final Color color = board.getActiveColor();
		final BoardExplorer exp = new SkipFirstExplorer(board.getCoordinatesSystem(), board.getKingPosition(color));
		pinedMap = new Direction[board.getDimension().getSize()];
		for (Direction d : PieceKind.QUEEN.getDirections()) {
			exp.start(d);
			while (exp.hasNext()) {
				final int pos = exp.next();
				final Piece p = board.getPiece(pos);
				if (p!=null) {
					// We found a piece, if it is in the defender's team, it will be pined if there's an attacker in the same direction before any other piece.
					if (color.equals(p.getColor()) && hasAttacker(exp, board, color.opposite(), d)) {
						pinedMap[pos] = d;
					}
					break;
				}
			}
		}
	}
	
	private boolean hasAttacker(BoardExplorer exp, Board<Move> board, Color attackerColor, Direction direction) {
		while (exp.hasNext()) {
			final int pos = exp.next();
			final Piece p = board.getPiece(pos);
			// We found a piece, it is an attacker if it is not in the defender's team,
			// and it can attack the king (it is a sliding piece, and it slides in the right direction).
			if (p!=null) {
				return attackerColor.equals(p.getColor()) && p.getKind().isSliding(direction);
			}
		}
		return false;
	}

	/** Tests whether this position is pinned.
	 * @return null if this position is not pinned. A direction, if the piece at the given position blocks an attack. 
	 * The direction returned is where the attack is coming from. 
	 */
	@Override
	public Direction apply(int position) {
		return pinedMap[position];
	}
}
