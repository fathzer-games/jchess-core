package com.fathzer.jchess;

import java.util.ArrayList;
import java.util.List;

import com.fathzer.games.GameState;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Rules;

public class CopyBasedMoveGenerator<M> implements MoveGenerator<M> {
	private Rules<Board<M>, M> rules;
	private List<Board<M>> boards;
	private int currentDepth;
	
	public CopyBasedMoveGenerator(Rules<Board<M>, M> rules, Board<M> board) {
		this.rules = rules;
		this.boards = new ArrayList<>();
		newBoard(board);
		this.currentDepth = 0;
	}

	private void newBoard(Board<M> board) {
		final Board<M> newOne = board.create();
		newOne.copy(board);
		this.boards.add(newOne);
	}
	
	@Override
	public void makeMove(M move) {
		final Board<M> previous = getBoard();
		currentDepth++;
		final Board<M> board;
		if (boards.size()==currentDepth) {
			board = previous.create();
			boards.add(board);
		} else {
			board = getBoard();
		}
		board.copy(previous);
//System.out.println("Make move "+move+" on thread "+Thread.currentThread()+" at depth "+currentDepth);
		board.makeMove(move);
	}
	
	@Override
	public void unmakeMove() {
//System.out.println("Unmake move "+move+" on thread "+Thread.currentThread()+" at depth "+currentDepth);
		currentDepth--;
	}

	public Board<M> getBoard() {
		return boards.get(currentDepth);
	}
	
	@Override
	public GameState<M> getState() {
		final Board<M> currentBoard = getBoard();
		return rules.getState(currentBoard);
	}
}
