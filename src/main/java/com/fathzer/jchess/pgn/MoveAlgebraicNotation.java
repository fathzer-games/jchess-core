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
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

/** A class to get move <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)">Algebraic notation</a> of moves.
 */
public class MoveAlgebraicNotation {
	private String checkSymbol = "+";
	private String checkmateSymbol = "#";
	private char captureSymbol = 'x';
	private String enPassantSymbol = " e.p";
	private Function<Castling.Side,String> castlingSymbolBuilder = s -> s==Castling.Side.KING?"O-O":"O-O-O";
	private Function<Piece, String> promotionSymbolBuilder = p -> "="+p.getNotation().toUpperCase();
	private boolean playMove;
	
	/** Gets the algebraic notation of a move.
	 * <br>If sideEffect attribute is true, move is played on the board
	 * @param board The board before the move occurs
	 * @param move The move to encode
	 * @return The move in algebraic notation
	 * @throws IllegalArgumentException if move is invalid
	 */
	public String get(Board<Move> board, Move move) {
		final StringBuilder builder = new StringBuilder();
		final GameState<Move> state = board.getState();
		// First, keep only moves with the right destination
		// This list will allow us to check if the move is valid and if it needs disambiguation
		final int to = move.getTo();
		final List<Move> candidates = StreamSupport.stream(state.spliterator(),false).filter(m -> m.getTo()==to).collect(Collectors.toList());
		if (!checkValidMove(move, candidates)) {
			throw new IllegalArgumentException("Move "+moveToString(move, board)+" is not valid");
		}
		final Piece moved = board.getPiece(move.getFrom());
		final Castling castling = moved.getKind()==KING ? board.getCastling(move.getFrom(), to, board.getActiveColor()) : null;
		if (castling!=null) {
			builder.append(castlingSymbolBuilder.apply(castling.getSide()));
		} else {
			builder.append(encodeMove(board, move, candidates));
		}
		final Optional<String> afterMove = afterMove(board, move);
		if (afterMove.isPresent()) {
			builder.append(afterMove.get());
		}
		return builder.toString();
	}
	
	private CharSequence moveToString(Move move, Board<Move> board) {
		final StringBuilder buf = new StringBuilder();
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		buf.append(cs.getAlgebraicNotation(move.getFrom()));
		buf.append('-');
		buf.append(cs.getAlgebraicNotation(move.getTo()));
		if (move.promotedTo()!=null) {
			buf.append('=');
			buf.append(move.promotedTo().getNotation());
		}
		return buf;
	}

	private boolean checkValidMove(Move move, List<Move> candidates) {
		final int from = move.getFrom();
		return candidates.stream().anyMatch(m -> m.getFrom()==from && m.promotedTo()==move.promotedTo());
	}

	private CharSequence encodeMove(Board<Move> board, Move move, List<Move> candidates) {
		final StringBuilder builder = new StringBuilder();
		final Piece moved = board.getPiece(move.getFrom());
		// Add caught symbol if needed
		final Piece caught = board.getPiece(move.getTo());
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		if (moved.getKind()==PAWN) {
			if (caught!=null || board.getEnPassant()==move.getTo()) {
				// Adds column of pawn
				builder.append(cs.getAlgebraicNotation(move.getFrom()).charAt(0));
				builder.append(captureSymbol);
			}
			// Add move's destination
			builder.append(cs.getAlgebraicNotation(move.getTo()));
			if (move.getTo()==board.getEnPassant()) {
				builder.append(enPassantSymbol);
			}
			// Add promotion if needed
			if (move.promotedTo()!=null) {
				builder.append(promotionSymbolBuilder.apply(move.promotedTo()));
			}
		} else {
			// Add piece symbol
			builder.append(moved.getNotation().toUpperCase());
			builder.append(getAmbiguitiesRemoval(board, move, candidates));
			if (caught!=null) {
				builder.append(captureSymbol);
			}
			// Add move's destination
			builder.append(cs.getAlgebraicNotation(move.getTo()));
		}
		return builder;
	}
	
	private String getAmbiguitiesRemoval(Board<Move> board, Move move, List<Move> candidates) {
		final int from = move.getFrom();
		final PieceKind kind = board.getPiece(from).getKind();
		final List<Move> ambiguities = candidates.stream().filter(m -> m.getFrom()!=from && kind==board.getPiece(m.getFrom()).getKind()).collect(Collectors.toList());
		if (ambiguities.isEmpty()) {
			return "";
		}
		final int row = board.getCoordinatesSystem().getRow(from);
		final int column = board.getCoordinatesSystem().getColumn(from);
		final String pos = board.getCoordinatesSystem().getAlgebraicNotation(from);
		if (ambiguities.stream().noneMatch(m->column==board.getCoordinatesSystem().getColumn(m.getFrom()))) {
			// No candidates with same column => return column
			return pos.substring(0,1);
		} else if (ambiguities.stream().noneMatch(m->row==board.getCoordinatesSystem().getRow(m.getFrom()))) {
			// No candidates with same row => return row
			return pos.substring(1);
		} else {
			// No way to disambiguate with row or column, return coordinates
			return pos;
		}
	}
	
	private Optional<String> afterMove(Board<Move> board, Move move) {
		board.makeMove(move);
		try {
			final GameState<Move> state = board.getState();
			if (state.getStatus()!=DRAW && state.size()==0) {
				return Optional.of(checkmateSymbol);
			} else if (board.isCheck()) {
				return Optional.of(checkSymbol);
			} else {
				return Optional.empty();
			}
		} finally {
			if (!playMove) {
				board.unmakeMove();
			}
		}
	}

	public MoveAlgebraicNotation withCheckSymbol(String checkSymbol) {
		this.checkSymbol = checkSymbol;
		return this;
	}

	public MoveAlgebraicNotation withCheckmateSymbol(String checkmateSymbol) {
		this.checkmateSymbol = checkmateSymbol;
		return this;
	}

	public MoveAlgebraicNotation withCaptureSymbol(char captureSymbol) {
		this.captureSymbol = captureSymbol;
		return this;
	}

	public MoveAlgebraicNotation withPlayMove(boolean sideEffect) {
		this.playMove = sideEffect;
		return this;
	}

	public MoveAlgebraicNotation withPromotionSymbolBuilder(Function<Piece, String> promotionSymbolBuilder) {
		this.promotionSymbolBuilder = promotionSymbolBuilder;
		return this;
	}

	public MoveAlgebraicNotation withCastlingSymbolBuilder(Function<Castling.Side, String> castlingSymbolBuilder) {
		this.castlingSymbolBuilder = castlingSymbolBuilder;
		return this;
	}

	public MoveAlgebraicNotation withEnPassantSymbol(String enPassantSymbol) {
		this.enPassantSymbol = enPassantSymbol;
		return this;
	}
}
