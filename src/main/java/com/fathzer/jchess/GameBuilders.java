package com.fathzer.jchess;

import com.fathzer.games.GameBuilder;
import com.fathzer.jchess.chess960.Chess960Board;
import com.fathzer.jchess.chess960.StartPositionGenerator;
import com.fathzer.jchess.fen.FENParser;

public class GameBuilders {
	public static final GameBuilder<Board<Move>> STANDARD = () -> FENParser.from(FENParser.NEW_STANDARD_GAME);
	public static final GameBuilder<Board<Move>> CHESS960 = () -> new Chess960Board(StartPositionGenerator.INSTANCE.get());
	
	private GameBuilders() {
		super();
	}
}
