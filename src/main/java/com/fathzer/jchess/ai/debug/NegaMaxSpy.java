package com.fathzer.jchess.ai.debug;

import static com.fathzer.games.ai.experimental.KeyBasedNegaMaxSpyFilter.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.games.ai.AlphaBetaState;
import com.fathzer.games.ai.experimental.Spy;
import com.fathzer.games.ai.experimental.KeyBasedNegaMaxSpyFilter;
import com.fathzer.games.ai.experimental.TreeSearchState;
import com.fathzer.games.ai.experimental.TreeSearchStateStack;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;

final class NegaMaxSpy implements Spy<Move, Board<Move>> {
	private final KeyBasedNegaMaxSpyFilter<Move, Board<Move>> filter;
	
	NegaMaxSpy(long searched) {
		this.filter = new KeyBasedNegaMaxSpyFilter<>(searched) {
			@Override
			public long getKey(TreeSearchStateStack<Move, Board<Move>> state) {
				return state.context.getGamePosition().getHashKey();
			}
		};
	}
	
	public NegaMaxSpy withAtMaxDepth(int atMaxDepth) {
		filter.setAtMaxDepth(atMaxDepth);
		return this;
	}

	@Override
	public void enter(TreeSearchStateStack<Move, Board<Move>> state) {
		if (filter.enter(state)) {
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		}
		if (filter.isOn()) {
			print(state);
			System.out.println("Entering");
		}
	}
	

	@Override
	public void cut(TreeSearchStateStack<Move, Board<Move>> state, Move move) {
		if (filter.isOn() && scoreFilter(state)) {
			final TreeSearchState<Move> current = state.get(state.getCurrentDepth());
			System.out.println(getTab(state)+" Cut on move "+current.lastMove.toString(state.context.getGamePosition().getCoordinatesSystem())+" with score "+current.value);
		}
	}

	@Override
	public void storeTT(TreeSearchStateStack<Move, Board<Move>> state, AlphaBetaState<Move> abState, boolean store) {
		if (filter.isOn() && scoreFilter(state)) {
			print(state); System.out.println("Reminder");
			System.out.print(getTab(state)+" Store is called on key "+filter.getKey(state)+" with value="+abState.getValue()+", alpha="+abState.getAlpha()+", beta="+abState.getBeta());
			System.out.println(store?" stored":" rejected");
		}
	}
	
	@Override
	public void exit(TreeSearchStateStack<Move, Board<Move>> state, Event evt) {
		if (filter.isOn()) {
			if (scoreFilter(state)) {
				System.out.println(getTab(state)+" Exit on "+evt+" -->"+state.getCurrent().value);
			}
			if (filter.exit(state)) {
				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			}
		}
	}

	protected boolean scoreFilter(TreeSearchStateStack<Move, Board<Move>> state) {
		return Math.abs(state.getCurrent().value)>30000;
	}

	private void print(TreeSearchStateStack<Move, Board<Move>> state) {
		final CharSequence tab = getTab(state);
		final Board<Move> bb = state.context.getGamePosition();
		System.out.print(tab+getMoves(state).toString()+", "+FENUtils.to(bb)+" at depth "+state.getCurrentDepth()+"/"+state.maxDepth+": ");
	}
	
	private List<String> getMoves(TreeSearchStateStack<Move, Board<Move>> state) {
		final CoordinatesSystem cs = state.context.getGamePosition().getCoordinatesSystem();
		return IntStream.rangeClosed(state.getCurrentDepth()+1, state.maxDepth).map(i -> state.getCurrentDepth()+1 + state.maxDepth - i)
			.mapToObj(i->{
				final Move mv = state.get(i).lastMove;
				return mv==null ? "?"+i: mv.toString(cs);
			})
			.collect(Collectors.toList());
	}
}