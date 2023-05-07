package com.fathzer.jchess;

import java.util.Arrays;
import java.util.Collection;

import com.fathzer.jchess.util.BitMapUtils;

import static com.fathzer.jchess.Direction.*;

import lombok.Getter;

public enum PieceKind {
	PAWN(1, null, false),
	ROOK(5, Arrays.asList(NORTH,WEST,SOUTH,EAST), true),
	KNIGHT(3, Arrays.asList(KNIGHT1, KNIGHT2, KNIGHT3, KNIGHT4, KNIGHT5, KNIGHT6, KNIGHT7, KNIGHT8), false), 
	BISHOP(3, Arrays.asList(NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST), true),
	QUEEN(9, SLIDERS, true), KING(1000, SLIDERS, false);
	
	@Getter
	private int value;
	@Getter
	private Collection<Direction> directions;
	private boolean sliding;
	private long slidingMap;
	
	PieceKind(int value, Collection<Direction> directions, boolean slides) {
		this.value = value;
		this.directions = directions;
		this.sliding = slides;
		if (sliding) {
			for (Direction d : directions) {
				slidingMap |= BitMapUtils.getMask(d.ordinal());
			}
		}
	}

	public boolean isSliding(Direction d) {
		return (slidingMap & BitMapUtils.getMask(d.ordinal())) != 0; 
	}
}
