package com.fathzer.jchess.generic;

import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

import lombok.EqualsAndHashCode;

/** A basic move.
 */
@EqualsAndHashCode
public class BasicMove implements Move {
	private final int from;
	private final int to;
	private final Piece promotion;
	
	/** Constructor.
	 * <br>Builds a move with no promotion
	 * @param from The cell's position of the moving piece.
	 * @param to The position of the destination cell.
	 */
	public BasicMove(int from, int to) {
		this(from, to, null);
	}
	
	/** Constructor.
	 * @param from The cell's position of the moving piece.
	 * @param to The position of the destination cell.
	 * @param promoted The promotion (null if no promotion).
	 */
	public BasicMove(int from, int to, Piece promoted) {
		this.from = from;
		this.to = to;
		this.promotion = promoted;
	}

	@Override
	public String toString() {
		return from + "-" + to + (promotion==null ? "" : "("+promotion+ ")");
	}

	/** Gets the start position of the moving piece.
	 * @return an integer
	 */
	public int getFrom() {
		return from;
	}

	/** Gets the destination position of the moving piece.
	 * @return an integer
	 */
	public int getTo() {
		return to;
	}

	/** Gets the promotion of the moving piece.
	 * @return a piece or null if the move doesn't lead to any promotion
	 */
	public Piece getPromotion() {
		return promotion;
	}
}