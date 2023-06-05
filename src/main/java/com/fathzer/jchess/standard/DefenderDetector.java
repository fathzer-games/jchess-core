package com.fathzer.jchess.standard;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

import static com.fathzer.jchess.util.BitMapUtils.*;

import java.util.function.IntPredicate;

//TODO
/** No more used.
 */
public class DefenderDetector implements IntPredicate {
	private static final long CORNER_MASK = ~(getMask(0) | getMask(7) | getMask(56) | getMask(63)); 
			
	private long defenderMap;
	
	public DefenderDetector(Board<Move> board, Color color) {
		if (board.getDimension().getWidth()!=8 || board.getDimension().getHeight()!=8) {
			throw new IllegalArgumentException();
		}
		buildDefenderMap(board, color);
	}
	
	private void buildDefenderMap(Board<Move> board, Color color) {
		final BoardExplorer exp = board.getCoordinatesSystem().buildExplorer(board.getKingPosition(color));
		defenderMap = 0;
		for (Direction d : PieceKind.QUEEN.getDirections()) {
			exp.start(d);
			while (exp.hasNext()) {
				final int pos = exp.next();
				final Piece p = board.getPiece(pos);
				if (p!=null) {
					// We found a piece, if it is in the defender's team, it will be pined if there's an attacker in the same direction before any other piece.
					if (color.equals(p.getColor()) && hasAttacker(exp, board, color.opposite(), d)) {
						defenderMap = defenderMap | getMask(pos);
					}
					break;
				}
			}
		}
		// Pieces in the corners can't defend any cell
		defenderMap = defenderMap & CORNER_MASK;
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

	/** Tests whether this position defends the King.
	 * @return false if this position is for sure not a defender. A true return does not implies there's an effective attack on the king
	 * it just mean if there's an attack, the piece at this position blocks it. 
	 */
	@Override
	public boolean test(int position) {
		return (getMask(position) & defenderMap) != 0;
	}
}
