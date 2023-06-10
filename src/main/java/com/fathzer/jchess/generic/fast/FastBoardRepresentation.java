package com.fathzer.jchess.generic.fast;

import java.util.List;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.generic.BoardRepresentation;

public class FastBoardRepresentation extends BoardRepresentation {
	
	public FastBoardRepresentation(Dimension dimension, List<PieceWithPosition> pieces) {
		super(new FastCoordinatesSystem(dimension), dimension.getHeight()*(dimension.getWidth()+2), pieces);
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
