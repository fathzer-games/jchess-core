package com.fathzer.jchess.ai.evaluator.simple;

import static com.fathzer.games.Color.*;
import static com.fathzer.jchess.PieceKind.*;

import java.util.EnumMap;
import java.util.Map;

import com.fathzer.games.Color;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

import lombok.Setter;

/** A simple evaluator described at <a href="https://www.chessprogramming.org/Simplified_Evaluation_Function">https://www.chessprogramming.org/Simplified_Evaluation_Function</a>
 * <br>This only work with 8*8 games
 */
public class SimpleEvaluator implements Evaluator<Board<Move>> {
	public static final Map<PieceKind, Integer> PIECE_VALUE;
	private static final Map<PieceKind, int[]> PIECE_POSITION_EVALUATOR_MAP;
	private static final int[] KING_MID_GAME_EVAL = new int[] {
			-30,-40,-40,-50,-50,-40,-40,-30,
			-30,-40,-40,-50,-50,-40,-40,-30,
			-30,-40,-40,-50,-50,-40,-40,-30,
			-30,-40,-40,-50,-50,-40,-40,-30,
			-20,-30,-30,-40,-40,-30,-30,-20,
			-10,-20,-20,-20,-20,-20,-20,-10,
			 20, 20,  0,  0,  0,  0, 20, 20,
			 20, 30, 10,  0,  0, 10, 30, 20};
	private static final int[] KING_END_GAME_EVAL = new int[] {
			-50,-40,-30,-20,-20,-30,-40,-50,
			-30,-20,-10,  0,  0,-10,-20,-30,
			-30,-10, 20, 30, 30, 20,-10,-30,
			-30,-10, 30, 40, 40, 30,-10,-30,
			-30,-10, 30, 40, 40, 30,-10,-30,
			-30,-10, 20, 30, 30, 20,-10,-30,
			-30,-30,  0,  0,  0,  0,-30,-30,
			-50,-30,-30,-30,-30,-30,-30,-50};
	
	@Setter
	private Color viewPoint;
	
	static {
		PIECE_VALUE = new EnumMap<>(PieceKind.class);
		PIECE_VALUE.put(KING, 20000);
		PIECE_VALUE.put(QUEEN, 900);
		PIECE_VALUE.put(ROOK, 500);
		PIECE_VALUE.put(BISHOP, 330);
		PIECE_VALUE.put(KNIGHT, 320);
		PIECE_VALUE.put(PAWN, 100);
		
		PIECE_POSITION_EVALUATOR_MAP = new EnumMap<>(PieceKind.class);
		PIECE_POSITION_EVALUATOR_MAP.put(PAWN, new int[] {
				0,  0,  0,  0,  0,  0,  0,  0,
				50, 50, 50, 50, 50, 50, 50, 50,
				10, 10, 20, 30, 30, 20, 10, 10,
				 5,  5, 10, 25, 25, 10,  5,  5,
				 0,  0,  0, 20, 20,  0,  0,  0,
				 5, -5,-10,  0,  0,-10, -5,  5,
				 5, 10, 10,-20,-20, 10, 10,  5,
				 0,  0,  0,  0,  0,  0,  0,  0});
		PIECE_POSITION_EVALUATOR_MAP.put(KNIGHT, new int[] {
				-50,-40,-30,-30,-30,-30,-40,-50,
				-40,-20,  0,  0,  0,  0,-20,-40,
				-30,  0, 10, 15, 15, 10,  0,-30,
				-30,  5, 15, 20, 20, 15,  5,-30,
				-30,  0, 15, 20, 20, 15,  0,-30,
				-30,  5, 10, 15, 15, 10,  5,-30,
				-40,-20,  0,  5,  5,  0,-20,-40,
				-50,-40,-30,-30,-30,-30,-40,-50});
		PIECE_POSITION_EVALUATOR_MAP.put(BISHOP, new int[] {
				-20,-10,-10,-10,-10,-10,-10,-20,
				-10,  0,  0,  0,  0,  0,  0,-10,
				-10,  0,  5, 10, 10,  5,  0,-10,
				-10,  5,  5, 10, 10,  5,  5,-10,
				-10,  0, 10, 10, 10, 10,  0,-10,
				-10, 10, 10, 10, 10, 10, 10,-10,
				-10,  5,  0,  0,  0,  0,  5,-10,
				-20,-10,-10,-10,-10,-10,-10,-20});
		PIECE_POSITION_EVALUATOR_MAP.put(ROOK, new int[] {
				  0,  0,  0,  0,  0,  0,  0,  0,
				  5, 10, 10, 10, 10, 10, 10,  5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				  0,  0,  0,  5,  5,  0,  0,  0});
		PIECE_POSITION_EVALUATOR_MAP.put(QUEEN, new int[] {
				-20,-10,-10, -5, -5,-10,-10,-20,
				-10,  0,  0,  0,  0,  0,  0,-10,
				-10,  0,  5,  5,  5,  5,  0,-10,
				 -5,  0,  5,  5,  5,  5,  0, -5,
				  0,  0,  5,  5,  5,  5,  0, -5,
				-10,  5,  5,  5,  5,  5,  0,-10,
				-10,  0,  5,  0,  0,  0,  0,-10,
				-20,-10,-10, -5, -5,-10,-10,-20
		});
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
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		int points = 0;
		final PhaseDetector phaseDetector = new PhaseDetector();
		do {
			final Piece p = exp.getPiece();
			if (p!=null) {
				phaseDetector.add(p);
				int inc = PIECE_VALUE.get(p.getKind());
				final int[] positionMap = PIECE_POSITION_EVALUATOR_MAP.get(p.getKind());
				if (positionMap!=null) {
					int index = exp.getIndex();
					inc += getPositionValue(positionMap, index, cs, p.getColor());
				}
				if (p.getColor()==WHITE) {
					points += inc;
				} else {
					points -= inc;
				}
			}
		} while (exp.next());
		final int[] kingMap = phaseDetector.getPhase()==Phase.MIDDLE_GAME ? KING_MID_GAME_EVAL : KING_END_GAME_EVAL;
		points += getPositionValue(kingMap, board.getKingPosition(WHITE), cs, WHITE); 
		points -= getPositionValue(kingMap, board.getKingPosition(BLACK), cs, BLACK); 
		return points;
	}
	
	private int getPositionValue(int[] positionMap, int index, CoordinatesSystem cs, Color color) {
		index = 8*cs.getRow(index)+cs.getColumn(index);
		if (color==BLACK) {
			index = 63-index;
		}
		return positionMap[index];
	}
}
