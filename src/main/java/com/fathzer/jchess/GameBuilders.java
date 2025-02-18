package com.fathzer.jchess;

import java.util.function.Supplier;

import com.fathzer.jchess.chess960.StartPositionGenerator;
import com.fathzer.jchess.fen.FENUtils;

/**
 * Utility class to build games.
 */
public class GameBuilders {
	/** A standard chess game builder. */
	public static final Supplier<Board<Move>> STANDARD = () -> FENUtils.from(FENUtils.NEW_STANDARD_GAME);
	/** A random chess960 game builder. */
	public static final Supplier<Board<Move>> CHESS960 = StartPositionGenerator.INSTANCE::get;
	
	private GameBuilders() {
		super();
	}
}
