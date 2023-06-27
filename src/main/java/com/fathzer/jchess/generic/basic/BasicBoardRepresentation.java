package com.fathzer.jchess.generic.basic;

import java.util.List;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.generic.BoardRepresentation;

public class BasicBoardRepresentation extends BoardRepresentation {
	public BasicBoardRepresentation(Dimension dimension, List<PieceWithPosition> pieces) {
		super(new BasicCoordinatesSystem(dimension), dimension.getHeight()*dimension.getWidth(), pieces);
	}

	public BoardExplorer getExplorer() {
		return new BasicBoardExplorer(pieces, 0);
	}

	@Override
	public DirectionExplorer getDirectionExplorer(int pos) {
		return new BasicDirectionExplorer(pieces, getCoordinatesSystem(), pos);
	}
}
