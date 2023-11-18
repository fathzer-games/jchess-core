package com.fathzer.jchess.generic.fast;

import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Piece;

import lombok.Getter;

public class FastDirectionExplorer implements DirectionExplorer {
	private final Piece[] pieces;
	private final int rowIncrement;
	@Getter
	private int startPosition;
	@Getter
	private Piece piece;
	@Getter
	private int index;
	private int cellIncrement;

	public FastDirectionExplorer(Piece[] pieces, Dimension cs, int startPosition) {
		this.rowIncrement = cs.getWidth()+2;
		this.pieces = pieces;
		this.startPosition = startPosition;
	}

	@Override
	public void reset(int startPosition) {
		this.startPosition = startPosition;
	}
	
	@Override
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

	@Override
	public boolean canReach(int toIndex, int maxIteration) {
		final int distance = toIndex-getIndex();
		if (maxIteration==1) {
			return distance==cellIncrement;
		} else if ((distance>0 && cellIncrement<0) || (distance<0 && cellIncrement>0)) {
			return false;
		} else {
			return DirectionExplorer.super.canReach(toIndex, maxIteration);
		}
	}
}