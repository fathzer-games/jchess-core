package com.fathzer.jchess.generic;

import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Move;

import lombok.Getter;

import java.util.List;
import java.util.function.BiPredicate;

import com.fathzer.jchess.BoardExplorer;

class InternalMoveBuilder {
	@FunctionalInterface
	public interface MoveGenerator {
		void generate(List<Move> moves, int from, int to);
	}
	
	public static final MoveGenerator DEFAULT = (moves, from, to) -> moves.add(new BasicMove(from,to));
	@Getter
	private ChessBoard board;
	private List<Move> moves;
	@Getter
	private BoardExplorer from;
	@Getter
	private DirectionExplorer to;
	private PinnedDetector checkManager;
	MoveValidator mv;
	
	InternalMoveBuilder(ChessBoard board) {
		this.board = board;
		this.from = board.getExplorer();
		this.to = board.getDirectionExplorer(-1);
		this.checkManager = board.getPinnedDetector();
		this.mv = new MoveValidator(board);
	}

	void init(List<Move> moves) {
		this.moves = moves;
		this.mv = new MoveValidator(board);
		this.from.reset(0);
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

	public Direction getPinnedDirection(int index) {
		return checkManager.apply(index);
	}
}
