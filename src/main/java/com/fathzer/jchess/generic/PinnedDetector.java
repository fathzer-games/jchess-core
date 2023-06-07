package com.fathzer.jchess.generic;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

import java.util.Arrays;
import java.util.function.IntFunction;

/** A class to detect pinned pieces.
 * <br>A pinned piece is a piece that can't move because it is the only protection of the King from an attack.
 * <br>Additionally, it gives the direction where the attack that pinned the piece is coming from.
 */
public class PinnedDetector implements IntFunction<Direction> {
	private Direction[] pinedMap;
	 
	public PinnedDetector(Board<Move> board) {
		final Color color = board.getActiveColor();
		final BoardExplorer exp = ((ChessBoard)board).getBoard().getExplorer();
		final int startPos = board.getKingPosition(color);
		pinedMap = ((ChessBoard)board).getBoard().getPinnedMap();
		Arrays.fill(pinedMap, null);
		for (Direction d : PieceKind.QUEEN.getDirections()) {
			exp.reset(startPos);
			exp.setDirection(d);
			while (exp.next()) {
				final Piece p = exp.getPiece();
				if (p!=null) {
					final int pos = exp.getIndex();
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
		while (exp.next()) {
			final Piece p = exp.getPiece();
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
