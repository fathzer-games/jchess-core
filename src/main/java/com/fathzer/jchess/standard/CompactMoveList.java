package com.fathzer.jchess.standard;

import com.fathzer.jchess.Move;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.games.Status;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.Piece;

import lombok.AllArgsConstructor;

/** A list of possible moves encoded in a compact way.
 * <br>Each move is encoded as an int.
 */
public class CompactMoveList implements ChessGameState {
	// It seems the maximum number of moves for a position is 218
	private static final int MAX_POSSIBLE_MOVES = 218;
	private static final int FROM_MASK = 0x3f;
	private static final int TO_OFFSET = 6;
	private static final int TO_MASK = 0xfc0;
	private static final int PROMOTION_OFFSET = 12;
	private static final int PROMOTION_MASK = 0xf000;
	// A copy of Piece values to prevent the use of Piece.values() which creates a new array at every call
	// We will shift the elements of the array in order to use the shifted promoted field without
	// testing for null value and decreasing its value.
	private static final Piece[] PIECES;
	// Same idea for game states (without the shift and null value hack).
	private static final Status[] STATES;

	static {
		final Piece[] arr = Piece.values();
		PIECES = new Piece[arr.length+1];
		System.arraycopy(arr, 0, PIECES, 1, arr.length);

		final Status[] arr2 = Status.values();
		STATES = new Status[arr2.length];
		System.arraycopy(arr2, 0, STATES, 0, arr2.length);
	}
	
	@AllArgsConstructor
	private static class EncodedMove implements Move {
		private long code;

		private static int to(long code) {
			return ((int)code & TO_MASK) >> TO_OFFSET;
		}
		private static int from(long code) {
			return (int)code & FROM_MASK;
		}
		private static Piece promotedTo(long code) {
			final int index = ((int)code & PROMOTION_MASK) >> PROMOTION_OFFSET;
			return PIECES[index];
		}
		

		@Override
		public int getFrom() {
			return from(code);
		}

		@Override
		public int getTo() {
			return to(code);
		}

		@Override
		public Piece promotedTo() {
			return promotedTo(code);
		}

		@Override
		public String toString() {
			return Coord.toString(getFrom())+"-"+Coord.toString(getTo())+(promotedTo()==null?"":promotedTo().toString());
		}
	}

	private long[] moves;
	private int size;
	private int gameState;
	
	public CompactMoveList() {
		this.moves = new long[MAX_POSSIBLE_MOVES];
		this.size = 0;
		this.gameState = 0;
	}
	
	public void add(int from, int to) {
		this.moves[size] = (to << TO_OFFSET) | from;
		size++;
	}
	
	public void add(int from, int to, Piece promotion) {
		this.moves[size] = ((promotion.ordinal()+1)<<PROMOTION_OFFSET) | (to << TO_OFFSET) | from;
		size++;
	}

	public Move get(int index) {
		return new EncodedMove(moves[index]);
	}

	void shrink() {
		long[] old = moves;
		moves = new long[size];
		System.arraycopy(old, 0, moves, 0, size);
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public void setStatus(Status state) {
		this.gameState = state.ordinal();
	}

	@Override
	public Status getStatus() {
		return STATES[this.gameState];
	}

	@Override
	public String toString() {
		if (Status.PLAYING.equals(getStatus())) {
			return IntStream.range(0, size).mapToObj(this::get).collect(Collectors.toList()).toString();
		} else {
			return getStatus().toString();
		}
	}

	@Override
	public void sort(IntUnaryOperator evaluator) {
		// First, remove useless values (they would be sorted by Arrays.sort)
		shrink();
		// We will first add the evaluator result in the high order bits of moves 
		for (int i = 0; i < moves.length; i++) {
			// The and between move and a mask clears the bits of a preceding sort
			moves[i] = (moves[i] & 0x00000000ffffffffL) | ((long)evaluator.applyAsInt(i)<<32);
		}
		// Then sort in ascending order (java does not allow to sort in descending order)
		Arrays.sort(moves);
		// Finally, revert the array to have it in descending order
		for (int left=0, right=moves.length-1; left<right; left++, right--) {
		    // exchange the first and last
		    long temp = moves[left];
		    moves[left] = moves[right];
		    moves[right] = temp;
		}
		// We don't clear the high order bits because
		// - They are useless in the move coding
		// - They will be cleared if another sort will happen
	}
	
	/** Gets a fast evaluation of a move.
	 * <br>This is particularly useful while implementing <a href="https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning">alpha-beta pruning</a>
	 * @param index The index of the move
	 * @param board The board before the move occurs
	 * @return a value, higher when move is a capture or a promotion, higher when captured piece has a higher value than capturing piece.
	 */
	public int fastEvaluate(int index, Board<Move> board) {
		int value = 0;
		final long code = moves[index];
		final Piece promotedTo = EncodedMove.promotedTo(code);
		if (promotedTo!=null) {
			// Promoted a pawn to a piece is better than a standard move 
			value = value+promotedTo.getKind().getValue()*10;
		}
		Piece p = board.getPiece(EncodedMove.to(code));
		if (p!=null) {
			// If move is a capture
			// Adds a value that denotes that capture is always better than no capture
			// and capturing a queen with a pawn is better than the opposite
			value = value + 10*p.getKind().getValue() - board.getPiece(EncodedMove.from(code)).getKind().getValue();
		}
		return value;
	}
}
