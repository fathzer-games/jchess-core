package com.fathzer.jchess.generic.fast;

import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Piece;

import lombok.Getter;

public class FastDirectionExplorer implements DirectionExplorer {
	private final Piece[] pieces;
	@Getter
	private int startPosition;
	@Getter
	private Piece piece;
	@Getter
	private int index;
	private int cellIncrement;
	private int rowIncrement;

	public FastDirectionExplorer(Piece[] pieces, Dimension cs, int startPosition) {
		this.rowIncrement = cs.getWidth()+2;
		this.pieces = pieces;
		this.startPosition = startPosition;
	}

	public void reset(int startPosition) {
		this.startPosition = startPosition;
	}

	public void start(Direction direction) {
		this.index = startPosition;
		this.cellIncrement = direction.getRowIncrement()*rowIncrement+direction.getColumnIncrement();
	}

	@Override
	public boolean next() {
		index += cellIncrement;
		if (index<0 || index>=pieces.length) {
			return false;
		}
		piece = pieces[index];
		return piece != Piece.BORDER;
	}
}