package com.fathzer.jchess;

import com.fathzer.games.GameState;
import com.fathzer.games.Status;

public interface ChessGameState extends GameState<Move> {
	void add(int from, int to);
	void add(int from, int to, Piece promotion);
	void setStatus(Status state);
}
