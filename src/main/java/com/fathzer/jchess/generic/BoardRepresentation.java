package com.fathzer.jchess.generic;

import java.util.List;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.ZobristKeyBuilder;

import lombok.Getter;

public abstract class BoardRepresentation {
	@Getter
	private final Dimension dimension;
	@Getter
	private final CoordinatesSystem coordinatesSystem;
	@Getter
	private final ZobristKeyBuilder zobrist;

	@Getter
	protected final Piece[] pieces;
	@Getter
	private final Direction[] pinnedMap;
	
	protected BoardRepresentation(CoordinatesSystem coordinatesSystem, int arrayDimension, List<PieceWithPosition> pieces) {
		this.dimension = coordinatesSystem.getDimension();
		this.coordinatesSystem = coordinatesSystem;
		zobrist = ZobristKeyBuilder.get(arrayDimension);
		this.pieces = new Piece[arrayDimension];
		this.pinnedMap = new Direction[this.pieces.length];
		for (PieceWithPosition p : pieces) {
			final int dest = coordinatesSystem.getIndex(p.getRow(), p.getColumn());
			if (this.pieces[dest]!=null) {
				throw new IllegalArgumentException ("More than one piece at "+dest+": "+this.pieces[dest]+"/"+p.getPiece());
			}
			this.pieces[dest]=p.getPiece();
		}
	}
	
	public Piece getPiece(int index) {
		return pieces[index];
	}
	
	public void copy(BoardRepresentation other) {
		System.arraycopy(other.pieces, 0, pieces, 0, pieces.length);
	}
	
	void setPiece(int index, Piece piece) {
		this.pieces[index] = piece;
	}
	
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < getDimension().getHeight() ; i++) {
			if (i!=0) {
				b.append('\n');
			}
			b.append(getDimension().getHeight() - i);
			for (int j = 0; j < getDimension().getWidth(); j++) {
				b.append(' ');
				b.append(getNotation(getPiece(coordinatesSystem.getIndex(i, j))));
			}
		}
		b.append(getLastLine());
		return b.toString();
	}
	
	private CharSequence getLastLine() {
		StringBuilder b = new StringBuilder("\n ");
		char coord = 'a';
		for (int j = 0; j < getDimension().getWidth(); j++) {
			b.append(' ');
			b.append(coord);
			coord++;
		}
		return b;
	}
	
	private String getNotation(Piece p) {
		if (p==null) {
			return " ";
		} else {
			return p.getNotation();
		}
	}

	public abstract BoardExplorer getExplorer();
	public abstract DirectionExplorer getDirectionExplorer(int pos);
}
