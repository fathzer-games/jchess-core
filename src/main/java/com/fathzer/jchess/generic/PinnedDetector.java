package com.fathzer.jchess.generic;

import com.fathzer.games.Color;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
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
	private final ChessBoard board;
	private Direction[] pinedMap;
	private int checkCount;
	private boolean hasPinned;
	 
	public PinnedDetector(ChessBoard board) {
		checkCount = -1;
		this.board = board;
	}
	
	/** Marks this detector as invalidate (typically after a move has been made).
	 */
	public void invalidate() {
		this.checkCount = -1;
	}
	
	public void load() {
		if (checkCount<0) {
			checkCount = 0;
			if (pinedMap==null) {
				pinedMap = new Direction[board.getBoard().pieces.length];
			} else {
				Arrays.fill(pinedMap, null);
			}
			DirectionExplorer exp = board.getDirectionExplorer();
			exp.reset(board.getKingPosition(board.getActiveColor()));
			fill(exp);
		}
	}
	private void fill(DirectionExplorer exp) {
		final Color kingsColor = board.getActiveColor();
		this.hasPinned = false;
		//TODO Maybe stopping search after 2 checks could be enough as all pieces are pinned (test if it has a real impact on performance - My guess is it has not)
		for (Direction d : PieceKind.QUEEN.getDirections()) {
			exploreSlidingDirection(exp, d, kingsColor);
		}
		// Search for knights that attacks king
		final Piece knight = kingsColor.equals(Color.WHITE)? Piece.BLACK_KNIGHT : Piece.WHITE_KNIGHT;
		for (Direction d: PieceKind.KNIGHT.getDirections()) {
			exp.start(d);
			if (exp.next() && knight.equals(exp.getPiece())) {
				checkCount++;
			}
		}
	}

	private void exploreSlidingDirection(DirectionExplorer exp, Direction d, final Color defendersColor) {
		exp.start(d);
		boolean near = true;
		while (exp.next()) {
			final Piece p = exp.getPiece();
			if (p!=null) {
				// We found a piece
				final int pos = exp.getIndex();
				// If it is in the defender's team, it will be pined if there's an attacker in the same direction before any other piece.
				if (defendersColor.equals(p.getColor())) {
					if (hasAttacker(exp, defendersColor.opposite(), d)) {
						pinedMap[pos] = d;
						hasPinned = true;
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

	/** Tests whether the piece at a given cell's index is pinned.
	 * @param position The position of the cell to test
	 * @return null if the piece is not pinned. A direction, if the piece blocks an attack. 
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
	
	/** Tests whether some position is pinned.
	 * @return true if at least one piece is pinned
	 */
	public boolean hasPinned() {
		return hasPinned;
	}
}
