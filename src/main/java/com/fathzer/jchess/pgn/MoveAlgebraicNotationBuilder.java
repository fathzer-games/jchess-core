package com.fathzer.jchess.pgn;

import static com.fathzer.jchess.PieceKind.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.Status;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

/** A class to get <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)">Algebraic notation</a> of moves.
 */
public class MoveAlgebraicNotationBuilder {
	private String checkSymbol = "+";
	private String checkmateSymbol = "#";
	private char captureSymbol = 'x';
	private String enPassantSymbol = " e.p.";
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
		final List<Move> state = board.getLegalMoves();
		// First, keep only moves with the right destination
		// This list will allow us to check if the move is valid and if it needs disambiguation
		final int to = move.getTo();
		final List<Move> candidates = StreamSupport.stream(state.spliterator(),false).filter(m -> m.getTo()==to).collect(Collectors.toList());
		if (!checkValidMove(move, candidates)) {
			throw new IllegalArgumentException("Move "+moveToString(move, board)+" is not valid");
		}
		final Piece moved = board.getPiece(move.getFrom());
		final Castling castling = moved.getKind()==KING ? board.getCastling(move.getFrom(), to) : null;
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
		if (move.getPromotion()!=null) {
			buf.append('=');
			buf.append(move.getPromotion().getNotation());
		}
		return buf;
	}

	private boolean checkValidMove(Move move, List<Move> candidates) {
		final int from = move.getFrom();
		return candidates.stream().anyMatch(m -> m.getFrom()==from && m.getPromotion()==move.getPromotion());
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
			if (move.getPromotion()!=null) {
				builder.append(promotionSymbolBuilder.apply(move.getPromotion()));
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
		board.makeMove(move, MoveConfidence.LEGAL);
		try {
			final Status status = board.getStatus();
			if (status==Status.BLACK_WON || status==Status.WHITE_WON) {
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

	/** Sets the symbol added to moves that resulted in a check.
	 * <br>The default value is "+".
	 * @param checkSymbol The new symbol
	 * @return this modified MoveAlgebraicNotation instance.
	 */
	public MoveAlgebraicNotationBuilder withCheckSymbol(String checkSymbol) {
		this.checkSymbol = checkSymbol;
		return this;
	}

	/** Sets the symbol added to moves that resulted in a check mate.
	 * <br>The default value is "#".
	 * @param checkmateSymbol The new symbol
	 * @return this modified MoveAlgebraicNotation instance.
	 */
	public MoveAlgebraicNotationBuilder withCheckmateSymbol(String checkmateSymbol) {
		this.checkmateSymbol = checkmateSymbol;
		return this;
	}

	/** Sets the symbol inserted in captures.
	 * <br>The default value is "x".
	 * @param captureSymbol The new symbol
	 * @return this modified MoveAlgebraicNotation instance.
	 */
	public MoveAlgebraicNotationBuilder withCaptureSymbol(char captureSymbol) {
		this.captureSymbol = captureSymbol;
		return this;
	}

	/** Sets the symbol added to 'En passant' captures.
	 * <br>The default value is " e.p.". Be aware that this symbol should be empty when generating a PGN file (see section 8.2.3.3 of <a href="https://ia902908.us.archive.org/26/items/pgn-standard-1994-03-12/PGN_standard_1994-03-12.txt">PGN specification</a>).
	 * @param enPassantSymbol The new symbol
	 * @return this modified MoveAlgebraicNotation instance.
	 */
	public MoveAlgebraicNotationBuilder withEnPassantSymbol(String enPassantSymbol) {
		this.enPassantSymbol = enPassantSymbol;
		return this;
	}

	/** Sets the functions that builds the notation for promotions.
	 * <br>The default value is "=" followed by the English notation of the piece the pawn is promoted to.
	 * @param promotionSymbolBuilder The new builder
	 * @return this modified MoveAlgebraicNotation instance.
	 */
	public MoveAlgebraicNotationBuilder withPromotionSymbolBuilder(Function<Piece, String> promotionSymbolBuilder) {
		this.promotionSymbolBuilder = promotionSymbolBuilder;
		return this;
	}

	/** Sets the functions that builds the notation for castlings.
	 * <br>The default value is "O-O" for king side castling and "O-O-O" for queen size castling.
	 * @param castlingSymbolBuilder The new builder
	 * @return this modified MoveAlgebraicNotation instance.
	 */
	public MoveAlgebraicNotationBuilder withCastlingSymbolBuilder(Function<Castling.Side, String> castlingSymbolBuilder) {
		this.castlingSymbolBuilder = castlingSymbolBuilder;
		return this;
	}

	/** Sets the play move attribute.
	 * <br>Each time the {@link #get(Board, Move)} method is called, the move is played on the board in order to test if a check occurs.
	 * <br>If this attribute is true, the move is not revered, leaving the board changed. Be aware that, even when setting the attribute to false, the board is changed during the {@link #get(Board, Move)} method execution.
	 * <br>Default value is false.
	 * @param sideEffect True to leave the move played on the board.
	 * @return this modified MoveAlgebraicNotation instance.
	 */
	public MoveAlgebraicNotationBuilder withPlayMove(boolean sideEffect) {
		this.playMove = sideEffect;
		return this;
	}
}
