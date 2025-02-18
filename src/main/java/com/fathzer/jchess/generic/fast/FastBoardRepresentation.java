package com.fathzer.jchess.generic.fast;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.generic.BoardRepresentation;

/** A square centric board representation that uses an array of (r)*(f+2) elements.
 * <br>The idea is to place border pieces on each side of each rank in order to speed up the detection of possible exits from the board
 *  when going through it square by square.
 *	This implementation is faster than jkjm's.
*/
public class FastBoardRepresentation extends BoardRepresentation {
	
	/** Constructor
	 * @param dimension the board's dimension
	 */
	public FastBoardRepresentation(Dimension dimension) {
		super(new FastCoordinatesSystem(dimension), dimension.getHeight()*(dimension.getWidth()+2));
		fillBorders();
	}
	
	/** Put BORDER pseudo pieces at the borders
	 */
	private void fillBorders() {
		int index = 0;
		while (index<pieces.length) {
			pieces[index] = Piece.BORDER;
			index += getDimension().getWidth()+1;
			pieces[index] = Piece.BORDER;
			index++;
		}
	}

	@Override
	public BoardExplorer getExplorer() {
		return new FastBoardExplorer(pieces, 1);
	}

	@Override
	public DirectionExplorer getDirectionExplorer(int pos) {
		return new FastDirectionExplorer(pieces, getDimension(), pos);
	}
}
