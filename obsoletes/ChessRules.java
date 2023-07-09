package com.fathzer.jchess;

import com.fathzer.games.Rules;

public interface ChessRules extends Rules<Board<Move>, Move> {
	boolean isCheck(Board<Move> board);
}
