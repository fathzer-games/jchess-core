package com.fathzer.jchess;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import lombok.Getter;

@Getter
public enum Direction {
	NORTH(-1,0), NORTH_WEST(-1,-1), WEST(0,-1), SOUTH_WEST(1,-1), SOUTH(1,0), SOUTH_EAST(1,1), EAST(0,1), NORTH_EAST(-1,1),
	KNIGHT1(-2,-1),KNIGHT2(-1,-2),KNIGHT3(1,-2),KNIGHT4(2,-1),KNIGHT5(2,1),KNIGHT6(1,2),KNIGHT7(-1,2),KNIGHT8(-2,1);
	
	public static final Collection<Direction> SLIDERS = Collections.unmodifiableList(Arrays.asList(NORTH, NORTH_WEST, WEST, SOUTH_WEST, SOUTH, SOUTH_EAST, EAST, NORTH_EAST));

	static {
		NORTH.opposite = SOUTH;
		SOUTH.opposite = NORTH;
		WEST.opposite = EAST;
		EAST.opposite = WEST;
		NORTH_WEST.opposite = SOUTH_EAST;
		SOUTH_EAST.opposite = NORTH_WEST;
		NORTH_EAST.opposite = SOUTH_WEST;
		SOUTH_WEST.opposite = NORTH_EAST;
		
		KNIGHT1.opposite = KNIGHT5;
		KNIGHT5.opposite = KNIGHT1;
		KNIGHT2.opposite = KNIGHT6;
		KNIGHT6.opposite = KNIGHT2;
		KNIGHT3.opposite = KNIGHT7;
		KNIGHT7.opposite = KNIGHT3;
		KNIGHT4.opposite = KNIGHT8;
		KNIGHT8.opposite = KNIGHT4;
	}
	
	private Direction(int rowIncrement, int columnIncrement) {
		this.rowIncrement = rowIncrement;
		this.columnIncrement = columnIncrement;
	}
	private final int rowIncrement;
	private final int columnIncrement;
	private Direction opposite;
}
