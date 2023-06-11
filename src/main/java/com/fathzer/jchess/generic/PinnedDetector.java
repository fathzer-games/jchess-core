package com.fathzer.jchess.generic;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

import java.util.Arrays;
import java.util.function.IntFunction;

/** A class to detect check situations and pinned pieces.
 * <br>A pinned piece is a piece that can't move because it is the only protection of the King from an attack.
 * Additionally, it gives the direction where the attack that pinned the piece is coming from.
 * <br>It also gives the number of pieces that put the king in check 
 */
public class PinnedDetector implements IntFunction<Direction> {
	private Direction[] pinedMap;
	private int checkCount = 0;
	 
	public PinnedDetector(Board<Move> board) {
		//TODO Maybe stopping search after 2 checks could be enough as all pieces are pinned (test if it has a real impact on performance - My guess is it has not)
		final Color color = board.getActiveColor();
		final DirectionExplorer exp = board.getDirectionExplorer(board.getKingPosition(color));
		pinedMap = ((ChessBoard)board).getBoard().getPinnedMap();
		Arrays.fill(pinedMap, null);
		for (Direction d : PieceKind.QUEEN.getDirections()) {
			exp.start(d);
			boolean near = true;
			while (exp.next()) {
				final Piece p = exp.getPiece();
				if (p!=null) {
					// We found a piece
					final int pos = exp.getIndex();
					// If it is in the defender's team, it will be pined if there's an attacker in the same direction before any other piece.
					if (color.equals(p.getColor())) {
						if (hasAttacker(exp, color.opposite(), d)) {
							pinedMap[pos] = d;
						}
					} else if (isAttacker(p, d, near)) {
						// If it is in opponent's team, and able to move in that direction, king is in check
						checkCount++;
					}
					break;
				}
				near = false;
			}
		}
		// Search for knights that attacks king
		final Piece knight = color.equals(Color.WHITE)? Piece.BLACK_KNIGHT : Piece.WHITE_KNIGHT;
		for (Direction d: PieceKind.KNIGHT.getDirections()) {
			exp.start(d);
			if (exp.next() && knight.equals(exp.getPiece())) {
				checkCount++;
			}
		}
	}
	
	private boolean isAttacker(Piece candidate, Direction direction, boolean near) {
		if (candidate==Piece.WHITE_PAWN) {
			return near && AttackDetector.WHITE_PAWN_THREAT_DIRECTIONS.contains(direction);
		} else if (candidate==Piece.BLACK_PAWN) {
			return near && AttackDetector.BLACK_PAWN_THREAT_DIRECTIONS.contains(direction);
		} else if (candidate.getKind()!=PieceKind.KING) {
			return candidate.getKind().getDirections().contains(direction);
		} else {
			return false;
		}
	}
	
	private boolean hasAttacker(DirectionExplorer exp, Color attackerColor, Direction direction) {
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

	/** Gets the number of pieces that put the king in check.
	 * @return a positive or null integer
	 */
	public int getCheckCount() {
		return checkCount;
	}
}
