package com.fathzer.jchess;

import java.util.Arrays;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Direction {
	NORTH(-1,0), NORTH_WEST(-1,-1), WEST(0,-1), SOUTH_WEST(1,-1), SOUTH(1,0), SOUTH_EAST(1,1), EAST(0,1), NORTH_EAST(-1,1),
	KNIGHT1(-2,-1),KNIGHT2(-1,-2),KNIGHT3(1,-2),KNIGHT4(2,-1),KNIGHT5(2,1),KNIGHT6(1,2),KNIGHT7(-1,2),KNIGHT8(-2,1);
	
	public static final Collection<Direction> SLIDERS = Arrays.asList(NORTH, NORTH_WEST, WEST, SOUTH_WEST, SOUTH, SOUTH_EAST, EAST, NORTH_EAST);

	private int rowIncrement;
	private int columnIncrement;
}
