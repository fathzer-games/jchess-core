package com.fathzer.jchess;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fathzer.games.Color;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Castling {
	WHITE_KING_SIDE("K", Color.WHITE, Side.KING, 1),
	WHITE_QUEEN_SIDE("Q", Color.WHITE, Side.QUEEN, 2),
	BLACK_KING_SIDE("k", Color.BLACK, Side.KING, 4),
	BLACK_QUEEN_SIDE("q", Color.BLACK, Side.QUEEN, 8);
	
	@AllArgsConstructor
	public enum Side {
		KING(-1), QUEEN(1);
		
		@Getter
		private int rookOffset;
	}

	/** A list of castlings. Prefer this to Castling.values in order to prevent array duplication.
	 */
	public static final List<Castling> ALL = Collections.unmodifiableList(Arrays.asList(values()));
	
	@Getter
	private final String code;
	@Getter
	private final Color color;
	@Getter
	private final Side side;
	@Getter
	private final int mask;
	
	public static int toInt(Collection<Castling> castlings) {
		int result = 0;
		for (Castling castling : castlings) {
			result += castling.mask;
		}
		return result;
	}
	
	public static Castling get(Color color, boolean kingSide) {
		int index = Color.WHITE.equals(color) ? 0 : 2;
		if (!kingSide) {
			index++;
		}
		return ALL.get(index);
	}
}