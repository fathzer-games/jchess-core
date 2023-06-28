package com.fathzer.jchess;

import java.util.Collection;
import java.util.EnumSet;

import static com.fathzer.jchess.Direction.*;

import lombok.Getter;

public enum PieceKind {
	PAWN(1, null, false),
	ROOK(5, EnumSet.of(NORTH,WEST,SOUTH,EAST), true),
	KNIGHT(3, EnumSet.of(KNIGHT1, KNIGHT2, KNIGHT3, KNIGHT4, KNIGHT5, KNIGHT6, KNIGHT7, KNIGHT8), false), 
	BISHOP(3, EnumSet.of(NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST), true),
	QUEEN(9, SLIDERS, true), KING(1000, SLIDERS, false);
	
	@Getter
	private int value;
	@Getter
	private Collection<Direction> directions;
	private boolean sliding;
	
	PieceKind(int value, Collection<Direction> directions, boolean slides) {
		this.value = value;
		this.directions = directions;
		this.sliding = slides;
	}

	public boolean isSliding(Direction d) {
		return sliding && this.directions.contains(d); 
	}
}
