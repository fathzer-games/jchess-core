package com.fathzer.jchess.generic.basic;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.generic.BoardRepresentation;

/** A <a href="https://www.chessprogramming.org/8x8_Board">basic '8x8 like' square centric board representation</a>
 * <br>'8x8 like' means that the board of r ranks per f files is represented by an array of r*f elements.
*/
public class BasicBoardRepresentation extends BoardRepresentation {
	/** Constructor
	 * @param dimension the board's dimension
	 */
	public BasicBoardRepresentation(Dimension dimension) {
		super(new BasicCoordinatesSystem(dimension), dimension.getHeight()*dimension.getWidth());
	}

	@Override
	public BoardExplorer getExplorer() {
		return new BasicBoardExplorer(pieces, 0);
	}

	@Override
	public DirectionExplorer getDirectionExplorer(int pos) {
		return new BasicDirectionExplorer(pieces, getCoordinatesSystem(), pos);
	}
}
