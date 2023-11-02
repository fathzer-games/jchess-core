package com.fathzer.jchess.time;

import com.fathzer.games.ai.time.RemainingMoveCountPredictor;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

/** A {@link RemainingMoveCountPredictor} that uses the function described in chapter 4 of <a href="http://facta.junis.ni.ac.rs/acar/acar200901/acar2009-07.pdf">Vuckovic and Solak paper</a>.
 */
public class VuckovicSolakTimeManager implements RemainingMoveCountPredictor<Board<?>> {
	public static final VuckovicSolakTimeManager INSTANCE = new VuckovicSolakTimeManager();
	
	@Override
	public int getRemainingHalfMoves(Board<?> board) {
		final int points = getPoints(board);
		final int remainingMoves;
		if (points<20) {
			remainingMoves = points+10;
		} else if (points<60) {
			remainingMoves = 3*points/8+22;
		} else {
			remainingMoves = 5*points/4-30;
		}
		return remainingMoves;
	}
	
	private int getPoints(Board<?> board) {
		int points = 0;
		final BoardExplorer exp = board.getExplorer();
		do {
			final Piece p = exp.getPiece();
			if (p!=null && !PieceKind.KING.equals(p.getKind())) {
				points += p.getKind().getValue();
			}
			
		} while (exp.next());
		return points;
	}
}
