package com.fathzer.jchess;

public interface Move {
	/** Gets the start position of the moving piece.
	 * @return an integer
	 */
	int getFrom();
	
	/** Gets the destination position of the moving piece.
	 * @return an integer
	 */
	int getTo();
	
	/** Gets the promotion of the moving piece.
	 * @return a piece or null if the move doesn't lead to any promotion
	 */
	Piece getPromotion();
	
	default String toString(CoordinatesSystem cs) {
		return cs.getAlgebraicNotation(getFrom())+"-"+cs.getAlgebraicNotation(getTo())+(getPromotion()==null?"":getPromotion().toString());
	}
}
