package com.fathzer.jchess.ai.evaluator;

import static com.fathzer.games.Color.*;
import static com.fathzer.jchess.PieceKind.*;

import com.fathzer.games.ai.evaluation.AbstractEvaluator;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.util.Stack;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

public class BasicEvaluator extends AbstractEvaluator<Move, Board<Move>> {
	private final Stack<Integer> scores;
	private int toCommit; 

	public BasicEvaluator() {
		this.scores = new Stack<>(null);
	}
	
	@Override
	public void init(Board<Move> board) {
		this.scores.clear();
		scores.set(getPoints(board));
	}
	
	private BasicEvaluator(int score) {
		this.scores = new Stack<>(null);
		scores.set(score);
	}

	@Override
	public Evaluator<Move, Board<Move>> fork() {
		BasicEvaluator result = new BasicEvaluator(scores.get());
		result.viewPoint = viewPoint;
		return result;
	}
	
	@Override
	public void prepareMove(Board<Move> board, Move move) {
        final int from = move.getFrom();
        final int to = move.getTo();
		Piece movingPiece = board.getPiece(from);
		int increment = 0;
		if (movingPiece.getKind()!=KING || board.getCastling(from,to)==null) {
	        if (board.getEnPassant()==to && PAWN==movingPiece.getKind()) {
	            increment = PAWN.getValue();
	        } else {
				final Piece capturedPiece = board.getPiece(to);
	        	if (capturedPiece!=null) {
	        		increment = capturedPiece.getKind().getValue();
	        	}
		        if (move.getPromotion()!=null) {
		        	increment = increment + move.getPromotion().getKind().getValue()-PAWN.getValue();
		        }
	        }
			if (!board.isWhiteToMove()) {
				increment = -increment;
			}
		}
		toCommit = scores.get()+increment;
	}

	@Override
	public void commitMove() {
		scores.next();
		scores.set(toCommit);
	}

	@Override
	public void unmakeMove() {
		scores.previous();
	}

	@Override
	public int evaluateAsWhite(Board<Move> board) {
		return 100*scores.get();
	}

	/** Get evaluation from the white view point.
	 * @param board The board to evaluate.
	 * @return An integer
	 */
	public static int getPoints(Board<Move> board) {
		final BoardExplorer exp = board.getExplorer(); 
		int points = 0;
		do {
			final Piece p = exp.getPiece();
			if (p!=null) {
				int inc = p.getKind().getValue();
				if (p.getColor()==WHITE) {
					points += inc;
				} else {
					points -= inc;
				}
			}
		} while (exp.next());
		return points;
	}
}
