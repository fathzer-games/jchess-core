package com.fathzer.jchess.ai.evaluator;

import com.fathzer.games.util.SelectiveComparator;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

/** A move evaluator that considers a capture or a promotion are better than other moves and taking a high value piece with a small value piece is better than the opposite.
 * <br><ul><li>A promotion is evaluated to 16*(nb points of promoted piece - 1) + the capture evaluation if any occurs during the promotion.
 * <br>For example, a simple promotion to a queen is evaluated to 16*(9-1) = 128</li>
 * <li>A capture is evaluated to 16*(nb points of captured piece)-(nb points of moving piece). A moving king counts 10 points.
 * <br>For example, a bishop that captures a rook is evaluated to 16*5-3 = 77. A pawn that captures a queen is evaluated to 16*9-1 = 143.
 * <br>A pawn that captures a queen in a promotion move to a queen is 128+143 = 267.</li>
 * </ul>
 */
public class BasicMoveComparator implements SelectiveComparator<Move> {
	private Board<Move> board;
	
	public BasicMoveComparator(Board<Move> board) {
		this.board = board;
	}

	public int evaluate(Move m) {
		final Piece promotion = m.getPromotion();
		int value = promotion==null ? 0 : (promotion.getKind().getValue()-1)*16;
		final Piece caught = board.getPiece(m.getTo());
		if (caught==null) {
			return value;
		} else {
			value += caught.getKind().getValue()*16;
			final PieceKind catching = board.getPiece(m.getFrom()).getKind();
			return value - (catching==PieceKind.KING ? 10 : catching.getValue());
		}
	}

	@Override
	public int compare(Move o1, Move o2) {
		return evaluate(o2)-evaluate(o1);
	}

	@Override
	public boolean test(Move t) {
		return evaluate(t)!=0;
	}
}
