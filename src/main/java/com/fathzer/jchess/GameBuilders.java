package com.fathzer.jchess;

import java.util.function.Supplier;

import com.fathzer.jchess.chess960.Chess960Board;
import com.fathzer.jchess.chess960.StartPositionGenerator;
import com.fathzer.jchess.fen.FENUtils;

public class GameBuilders {
	public static final Supplier<Board<Move>> STANDARD = () -> FENUtils.from(FENUtils.NEW_STANDARD_GAME);
	public static final Supplier<Board<Move>> CHESS960 = () -> new Chess960Board(StartPositionGenerator.INSTANCE.get());
	
	private GameBuilders() {
		super();
	}
}
