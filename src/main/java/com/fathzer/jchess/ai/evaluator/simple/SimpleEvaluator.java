package com.fathzer.jchess.ai.evaluator.simple;

import static com.fathzer.games.Color.*;
import static com.fathzer.jchess.PieceKind.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import com.fathzer.games.Color;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

import lombok.Setter;

/** A simple evaluator described at <a href="https://www.chessprogramming.org/Simplified_Evaluation_Function">https://www.chessprogramming.org/Simplified_Evaluation_Function</a>
 * <br>This only work with 8*8 games
 */
public class SimpleEvaluator implements Evaluator<Board<Move>> {
	public static final Map<PieceKind, Integer> PIECE_VALUE;
	private static final Map<PieceKind, Supplier<PositionEvaluator>> PIECE_POSITION_EVALUATOR_MAP;
	
	@Setter
	private Color viewPoint;
	
	static {
		PIECE_VALUE = new EnumMap<>(PieceKind.class);
		PIECE_VALUE.put(QUEEN, 900);
		PIECE_VALUE.put(ROOK, 500);
		PIECE_VALUE.put(BISHOP, 330);
		PIECE_VALUE.put(KNIGHT, 320);
		PIECE_VALUE.put(PAWN, 100);
		
		PIECE_POSITION_EVALUATOR_MAP = new EnumMap<>(PieceKind.class);
		PIECE_POSITION_EVALUATOR_MAP.put(PAWN, PawnPositionEvaluator::new);
	}
	
	@Override
	public int evaluate(Board<Move> board) {
		int points = getPoints(board);
		if (BLACK==viewPoint || (viewPoint==null && BLACK==board.getActiveColor())) {
			points = -points;
		}
		return points;
	}

	public int getPoints(Board<Move> board) {
		final BoardExplorer exp = board.getExplorer();
		int points = 0;
		do {
			final Piece p = exp.getPiece();
			if (p!=null) {
				int inc = PIECE_VALUE.get(p.getKind());
				if (p.getColor()==WHITE) {
					points += inc;
				} else {
					points -= inc;
				}
			}
		} while (exp.next());
		return points;
	}
	
	protected Phase getPhase(BoardExplorer exp) {
		final int index = exp.getIndex();
		exp.reset(0);
		try {
			boolean blackQueen = false;
			boolean whiteQueen = false;
			boolean whiteRook = false;
			boolean blackRook = false;
			int whiteMinor = 0;
			int blackMinor = 0;
			do {
				final Piece p = exp.getPiece();
				if (p!=null) {
					switch (p) {
						case BLACK_QUEEN : blackQueen=true; break;
						case WHITE_QUEEN : whiteQueen=true; break;
						case BLACK_ROOK : blackRook=true; break;
						case WHITE_ROOK : whiteRook=true; break;
						case BLACK_BISHOP : blackMinor++; break;
						case BLACK_KNIGHT : blackMinor++; break;
						case WHITE_BISHOP : whiteMinor++; break;
						case WHITE_KNIGHT : whiteMinor++; break;
					default:
						break;
					}
				}
				if (blackQueen && whiteQueen) {
					return Phase.MIDDLE_GAME;
				}
				if ((blackQueen && (blackRook || blackMinor>1)) || (whiteQueen && (whiteRook || whiteMinor>1))) {
					return Phase.MIDDLE_GAME;
				}
			} while (exp.next());
			return Phase.END_GAME;
		} finally {
			exp.reset(index);
		}
	}
}
