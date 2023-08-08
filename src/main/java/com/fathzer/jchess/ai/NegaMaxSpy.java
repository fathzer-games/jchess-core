package com.fathzer.jchess.ai;

import com.fathzer.games.ai.AlphaBetaState;
import com.fathzer.games.ai.experimental.Negamax3.*;
import com.fathzer.games.ai.experimental.TreeSearchStateStack;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;

final class NegaMaxSpy implements Spy<Move, Board<Move>> {
	private boolean on;
	private final long searched;
	
	NegaMaxSpy(long searched) {
		this.searched = searched;
	}
	
	@Override
	public void enter(TreeSearchStateStack<Move, Board<Move>> state) {
		if (getKey(state)==searched) {
			on=true;
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			print(state);
		}
	}
	
	private long getKey(TreeSearchStateStack<Move, Board<Move>> state) {
		return state.position.getHashKey();
	}

	@Override
	public void exit(TreeSearchStateStack<Move, Board<Move>> state, Event evt, int score) {
		if (on) {
			if (Math.abs(score)>1500) {
				print(state);
				System.out.println("Exit on "+evt+" -->"+score);
			}
			if (getKey(state)==searched) {
				on=false;
				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			}
		}
	}
	
	private void print(TreeSearchStateStack<Move, Board<Move>> state) {
		final String tab = " ".repeat(state.maxDepth-state.getCurrentDepth());
		final Board<Move> bb = state.position;
		System.out.print(tab+FENParser.to(bb)+" at depth "+state.getCurrentDepth()+"/"+state.maxDepth+": ");
		
	}

	@Override
	public void storeTT(TreeSearchStateStack<Move, Board<Move>> state, AlphaBetaState<Move> abState, boolean store) {
		if (on && store) {
			print(state);
			System.out.print("Store is called on key "+getKey(state)+" with value="+abState.getValue()+", alpha="+abState.getAlpha()+", beta="+abState.getBeta());
			System.out.println(store?" stored":" rejected");
		}
	}
}