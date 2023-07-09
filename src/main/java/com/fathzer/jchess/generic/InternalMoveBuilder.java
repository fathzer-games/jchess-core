package com.fathzer.jchess.generic;

import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Move;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;

public class InternalMoveBuilder {
	@FunctionalInterface
	public interface MoveGenerator {
		void generate(List<Move> moves, int from, int to);
	}
	
	private static final int MAX_POSSIBLE_MOVES = 218;
	public static final MoveGenerator DEFAULT = (moves, from, to) -> moves.add(new BasicMove(from,to));
	@Getter
	private Board<Move> board;
	@Getter
	private List<Move> moves;
	@Getter
	private BoardExplorer from;
	@Getter
	private DirectionExplorer to;
	private PinnedDetector checkManager;
	MoveValidator mv;
	
	public InternalMoveBuilder(ChessBoard board) {
		this.board = board;
		this.moves = new ArrayList<>(MAX_POSSIBLE_MOVES);
		this.from = board.getExplorer();
		this.to = board.getDirectionExplorer(-1);
		this.checkManager = new PinnedDetector(board);
		this.mv = new MoveValidator(board, checkManager);
	}

	void clearMoves() {
		this.moves = new ArrayList<>();
	}
	
	public int getCheckCount() {
		return checkManager.getCheckCount();
	}

	public void addMove(Direction direction, BiPredicate<BoardExplorer, BoardExplorer> validator)  {
		addMove(direction, validator, DEFAULT);
	}

	public void addMove(Direction direction, BiPredicate<BoardExplorer, BoardExplorer> validator, MoveGenerator moveGenerator)  {
		to.start(direction);
		if (to.next() && validator.test(from, to)) {
			moveGenerator.generate(moves, from.getIndex(), to.getIndex());
		}
	}

	public void addAllMoves(Direction direction, BiPredicate<BoardExplorer, BoardExplorer> validator)  {
		to.start(direction);
		while (to.next()) {
			if (validator.test(from, to)) {
				DEFAULT.generate(moves, from.getIndex(), to.getIndex());
			}
			if (to.getPiece()!=null) {
				break;
			}
		}
	}

	public void addMoves(Direction direction, int maxIteration, BiPredicate<BoardExplorer, BoardExplorer> validator)  {
		addMoves(direction, maxIteration, validator, DEFAULT);
	}

	public void addMoves(Direction direction, int maxIteration, BiPredicate<BoardExplorer, BoardExplorer> validator, MoveGenerator moveGenerator)  {
		to.start(direction);
		int iteration = 0;
		while (to.next()) {
			if (validator.test(from, to)) {
				moveGenerator.generate(moves, from.getIndex(), to.getIndex());
			}
			iteration++;
			if (iteration>=maxIteration || to.getPiece()!=null) {
				break;
			}
		}
	}
	
	public boolean hasPinned() {
		return checkManager.hasPinned();
	}

	public Direction getPinnedDirection(int index) {
		return checkManager.apply(index);
	}
}
