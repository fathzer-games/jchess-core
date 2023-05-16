package com.fathzer.jchess.pgn;

import static com.fathzer.jchess.PieceKind.*;
import static com.fathzer.games.Status.DRAW;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fathzer.games.GameState;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.ChessRules;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Notation;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

/** A class to get move <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)">Algebraic notation</a> of moves.
 */
public class SAN {
	private final ChessRules rules;
	private String checkSymbol = "+";
	private String checkmateSymbol = "#";
	private char captureSymbol = 'x';
	private Function<Piece, String> promotionSymbolBuilder = p -> p.getNotation().toUpperCase();
	private boolean playMove;
	
	public SAN(ChessRules rules) {
		this.rules = rules;
	}

	/** Gets the algebraic notation of a move.
	 * <br>If sideEffect attribute is true, move is played on the board
	 * @param board The board before the move occurs
	 * @param move The move to encode
	 * @return The move in algebraic notation
	 * @throws IllegalArgumentException if move is invalid
	 */
	public String get(Board<Move> board, Move move) {
		//TODO Check if it is a en-passant move
		//TODO Castling
		final StringBuilder builder = new StringBuilder();
		final GameState<Move> state = rules.getState(board);
		// First, keep only moves with the right destination
		// This list will allow us to check if the move is valid and if it needs disambiguation
		final int to = move.getTo();
		final List<Move> candidates = StreamSupport.stream(state.spliterator(),false).filter(m -> m.getTo()==to).collect(Collectors.toList());
		if (!checkValidMove(move, candidates)) {
			throw new IllegalArgumentException("Move is not valid");
		}
		final Piece moved = board.getPiece(move.getFrom());
		if (moved.getKind()!=PAWN) {
			builder.append(moved.getNotation().toUpperCase());
		} else {
			//TODO		List<Move> ambiguities = getAmbiguities(move, candidates);

		}
		final Piece caught = board.getPiece(move.getTo());
		if (caught!=null) {
			if (moved.getKind()==PAWN) {
				// Adds column of pawn
				builder.append(Notation.toString(move.getFrom(), board.getDimension()).charAt(0));
			}
			builder.append(captureSymbol);
		}
		builder.append(Notation.toString(to, board.getDimension()));
		if (move.promotedTo()!=null) {
			builder.append(promotionSymbolBuilder.apply(move.promotedTo()));
		}
		final Optional<String> afterMove = afterMove(board, move, rules);
		if (afterMove.isPresent()) {
			builder.append(afterMove.get());
		}
		return builder.toString();
	}

	private boolean checkValidMove(Move move, List<Move> candidates) {
		final int from = move.getFrom();
		return candidates.stream().anyMatch(m -> m.getFrom()==from && m.promotedTo()==move.promotedTo());
	}

	private List<Move> getAmbiguities(Board<Move> board, Move move, List<Move> candidates) {
		final int from = move.getFrom();
		final PieceKind kind = board.getPiece(from).getKind();
		return candidates.stream().filter(m -> m.getFrom()!=from && kind==board.getPiece(m.getFrom()).getKind()).collect(Collectors.toList());
	}
	
	private Optional<String> afterMove(Board<Move> board, Move move, ChessRules rules) {
		final Board<Move> after;
		if (playMove) {
			after = board;
		} else {
			after = board.create();
			after.copy(board);
		}
		after.move(move);
		final GameState<Move> state = rules.getState(after);
		if (state.getStatus()!=DRAW && state.size()==0) {
			return Optional.of(checkmateSymbol);
		} else if (rules.isCheck(after)) {
			return Optional.of(checkSymbol);
		} else {
			return Optional.empty();
		}
	}

	public SAN withCheckSymbol(String checkSymbol) {
		this.checkSymbol = checkSymbol;
		return this;
	}

	public SAN withCheckmateSymbol(String checkmateSymbol) {
		this.checkmateSymbol = checkmateSymbol;
		return this;
	}

	public SAN withCaptureSymbol(char captureSymbol) {
		this.captureSymbol = captureSymbol;
		return this;
	}

	public SAN withPlayMove(boolean sideEffect) {
		this.playMove = sideEffect;
		return this;
	}

	public SAN withPromotionSymbolBuilder(Function<Piece, String> promotionSymbolBuilder) {
		this.promotionSymbolBuilder = promotionSymbolBuilder;
		return this;
	}
}
