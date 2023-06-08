package com.fathzer.jchess.generic;

import java.util.List;

import com.fathzer.games.Color;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.ZobristKeyBuilder;

import lombok.Getter;

public class BoardRepresentation {
	@Getter
	private final Dimension dimension;
	@Getter
	private final CoordinatesSystem coordinatesSystem;
	@Getter
	private final ZobristKeyBuilder zobrist;

	private final Piece[] pieces;
	@Getter
	private final Direction[] pinnedMap;
	private final Piece[] backup;
	private final int[] kingPositions=new int[2];
	private final int[] kingPositionBackup=new int[2];
	
	public BoardRepresentation(Dimension dimension, List<PieceWithPosition> pieces) {
		this.dimension = dimension;
		this.coordinatesSystem = new DefaultCoordinatesSystem(dimension);
		final int size = dimension.getHeight()*dimension.getWidth();
		zobrist = ZobristKeyBuilder.get(size);
		this.pieces = new Piece[size];
		this.backup = new Piece[this.pieces.length];
		this.pinnedMap = new Direction[this.pieces.length];
		for (PieceWithPosition p : pieces) {
			final int dest = coordinatesSystem.getIndex(p.getRow(), p.getColumn());
			if (this.pieces[dest]!=null) {
				throw new IllegalArgumentException ("More than one piece at "+dest+": "+this.pieces[dest]+"/"+p.getPiece());
			}
			this.pieces[dest]=p.getPiece();
			if (PieceKind.KING.equals(p.getPiece().getKind())) {
				this.kingPositions[p.getPiece().getColor().ordinal()] = dest;
			}
		}
	}
	
	public Piece getPiece(int index) {
		return pieces[index];
	}
	
	public void copy(BoardRepresentation other) {
		System.arraycopy(other.kingPositions, 0, kingPositions, 0, kingPositions.length);
		System.arraycopy(other.pieces, 0, pieces, 0, pieces.length);
	}
	
	public void save() {
		System.arraycopy(this.pieces, 0, backup, 0, pieces.length);
		System.arraycopy(this.kingPositions, 0, this.kingPositionBackup, 0, kingPositions.length);
	}
	
	public void restore() {
		System.arraycopy(backup, 0, this.pieces, 0, pieces.length);
		System.arraycopy(kingPositionBackup, 0, this.kingPositions, 0, kingPositions.length);
	}

	void setPiece(int index, Piece piece) {
		this.pieces[index] = piece;
	}
	
	boolean is(int index, Piece piece) {
		return piece==pieces[index];
	}
	
	public int getKingPosition(Color color) {
		return kingPositions[color.ordinal()];
	}
	
	void updateKingPosition(Color kingsColor, int index) {
		kingPositions[kingsColor.ordinal()] = index;
	}
	
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		final BoardExplorer exp = getExplorer();
		exp.setDirection(Direction.EAST);
		for (int i = 0; i < getDimension().getHeight() ; i++) {
			if (i!=0) {
				b.append('\n');
			}
			b.append(getDimension().getHeight() - i);
			exp.setPosition(coordinatesSystem.getIndex(i, 0));
			do {
				b.append(' ');
				b.append(getNotation(exp.getPiece()));
			} while (exp.next());
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

	BoardExplorer getExplorer() {
		return getExplorer(0);
	}
	BoardExplorer getExplorer(int pos) {
		return new MyBoardExplorer(pieces, coordinatesSystem, pos);
	}
}
