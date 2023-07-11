package com.fathzer.jchess.generic;

import java.util.List;
import java.util.function.IntPredicate;

import com.fathzer.games.Color;
import com.fathzer.games.Status;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.generic.InternalMoveBuilder.MoveGenerator;

public class MovesBuilder {
	private final ChessBoard board;
	// Cache
	private List<Move> moves;
	private Status status;
	
	public MovesBuilder(ChessBoard board) {
		super();
		this.board = board;
	}
	
	protected void clear() {
		this.moves = null;
		this.status = null;
	}

	protected List<Move> getMoves() {
		if (moves==null) {
			final Color color = board.getActiveColor();
			final InternalMoveBuilder tools = new InternalMoveBuilder(board);
			final BoardExplorer exp = tools.getFrom();
			if (tools.getCheckCount()>1) {
				// If double check, only king can move
				final int kingPosition = tools.getBoard().getKingPosition(color);
				exp.reset(kingPosition);
				tools.getTo().reset(kingPosition);
				addKingMoves(tools);
			} else {
				do {
					if (exp.getPiece()!=null && color==exp.getPiece().getColor()) {
						addPossibleMoves(tools);
					}
				} while (exp.next());
			}
			moves = tools.getMoves();
		}
		return moves;
	}
	
	private void addPossibleMoves(InternalMoveBuilder tools) {
		final Piece piece = tools.getFrom().getPiece();
		tools.getTo().reset(tools.getFrom().getIndex());
		if (PieceKind.ROOK.equals(piece.getKind()) || PieceKind.BISHOP.equals(piece.getKind()) || PieceKind.QUEEN.equals(piece.getKind())) {
			for (Direction d:piece.getKind().getDirections()) {
				tools.addAllMoves(d, tools.mv.getDefault());
			}
		} else if (PieceKind.KNIGHT.equals(piece.getKind())) {
			for (Direction d:PieceKind.KNIGHT.getDirections()) {
				tools.addMove(d, tools.mv.getDefault());
			}
		} else if (PieceKind.KING.equals(piece.getKind())) {
			addKingMoves(tools);
		} else if (PieceKind.PAWN.equals(piece.getKind())) {
			addPawnMoves(tools);
		} else {
			throw new IllegalArgumentException("Unknown piece kind: "+piece.getKind());
		}
	}
	
	private void addKingMoves(InternalMoveBuilder tools) {
		// We can think remember the free safe cells could be reused in castling //TODO
		// StandardMoves => King can't go to attacked cell
		for (Direction d:PieceKind.KING.getDirections()) {
			tools.addMove(d, tools.mv.getKing());
		}
		// Castlings
		if (tools.getCheckCount()==0) {
			// No castlings allowed when you're in check
			if (Color.WHITE==tools.getFrom().getPiece().getColor()) {
				tryCastling(tools, Castling.WHITE_KING_SIDE);
				tryCastling(tools, Castling.WHITE_QUEEN_SIDE);
			} else {
				tryCastling(tools, Castling.BLACK_KING_SIDE);
				tryCastling(tools, Castling.BLACK_QUEEN_SIDE);
			}
		}
	}
	private void tryCastling(InternalMoveBuilder tools, Castling castling) {
		if (tools.getBoard().hasCastling(castling)) {
			final int kingPosition = tools.getFrom().getIndex(); 
			final int kingDestination = tools.getBoard().getKingDestination(castling);
			final int rookPosition = tools.getBoard().getInitialRookPosition(castling);
			final int rookDestination  = kingDestination + castling.getSide().getRookOffset();
			if (areCastlingCellsFree(tools.getFrom(), kingDestination, rookPosition, rookDestination) &&
					areCastlingCellsSafe(tools.mv, tools.getBoard().getActiveColor().opposite(), kingPosition, kingDestination)) {
				addCastling(tools.getMoves(), kingPosition, rookPosition, kingDestination, rookDestination);
			}
		}
	}

	/** Adds a castling to the list of moves.
	 * <br>This default implementation creates a move from the king's current position to its destination.
	 * @param moves The moves list in which to add the move.
	 * @param kingPosition The current king's position.
	 * @param rookPosition The current rook's position.
	 * @param kingDestination The king's destination.
	 * @param rookDestination The rook's destination.
	 */
	protected void addCastling(List<Move> moves, int kingPosition, int rookPosition, int kingDestination, int rookDestination) {
		moves.add(new BasicMove(kingPosition, kingDestination));
	}
	
	private boolean areCastlingCellsFree(BoardExplorer exp, int kingDestination, int rookPosition, int rookDestination) {
		final int kingPosition = exp.getIndex();
		final int min = Math.min(Math.min(kingPosition, kingDestination), Math.min(rookPosition, rookDestination));
		final int last = Math.max(Math.max(kingPosition, kingDestination), Math.max(rookPosition, rookDestination));
		exp.reset(min);
		for (int i=min; i<=last; i = exp.getIndex()) {
			if (i!=kingPosition && i!=rookPosition && exp.getPiece()!=null) {
				exp.reset(kingPosition);
				return false;
			}
			exp.next();
		}
		exp.reset(kingPosition);
		return true;
	}
	
	/** Checks the positions that should be safe (not attacked) to have the castling allowed.
	 * @param mv A move validator to be used to check if positions are safe
	 * @param attacker The color of the attacker of cell 
	 * @param kingPosition Current king's position
	 * @param kingDestination King's destination
	 * @return true if safe. Please note that the king's cell is not checked in this method because the check state is verified before this method is called.
	 */
	private boolean areCastlingCellsSafe(MoveValidator mv, Color attacker, int kingPosition, int kingDestination) {
		if (kingPosition<kingDestination) {
			for (int i = kingPosition+1; i <= kingDestination; i++) {
				if (mv.isAttacked(i, attacker)) {
					return false;
				}
			}
		} else if (kingPosition!=kingDestination) {
			// Warning, in chess960, king can stay at in position during castling
			for (int i = kingDestination; i < kingPosition; i++) {
				if (mv.isAttacked(i, attacker)) {
					return false;
				}
			}
		}
		return true;
	}

	private void addPawnMoves(InternalMoveBuilder tools) {
		final boolean black = Color.BLACK == tools.getFrom().getPiece().getColor();
		// Take care of promotion when generating move
		final int promotionRow = black?tools.getBoard().getDimension().getHeight()-1:0;
		final IntPredicate promoted = i -> tools.getBoard().getCoordinatesSystem().getRow(i)==promotionRow;
		final MoveGenerator generator = (m, f, t) -> {
			if (promoted.test(t)) {
				m.add(new BasicMove(f, t, black ? Piece.BLACK_KNIGHT : Piece.WHITE_KNIGHT));
				m.add(new BasicMove(f, t, black ? Piece.BLACK_QUEEN : Piece.WHITE_QUEEN));
				m.add(new BasicMove(f, t, black ? Piece.BLACK_ROOK : Piece.WHITE_ROOK));
				m.add(new BasicMove(f, t, black ? Piece.BLACK_BISHOP : Piece.WHITE_BISHOP));
			} else {
				m.add(new BasicMove(f, t));
			}
		};
		final int startRow = black ? 1 : tools.getBoard().getDimension().getHeight()-2;
		final int countAllowed = tools.getBoard().getCoordinatesSystem().getRow(tools.getFrom().getIndex()) == startRow ? 2 : 1;
		if (black) {
			// Standard moves (no catch)
			tools.addMoves(Direction.SOUTH, countAllowed, tools.mv.getPawnNoCatch(), generator);
			// Catches (including En-passant)
			tools.addMove(Direction.SOUTH_EAST, tools.mv.getPawnCatch(), generator);
			tools.addMove(Direction.SOUTH_WEST, tools.mv.getPawnCatch(), generator);
		} else {
			// Standard moves (no catch)
			tools.addMoves(Direction.NORTH, countAllowed, tools.mv.getPawnNoCatch(), generator);
			// Catches (including En-passant)
			tools.addMove(Direction.NORTH_EAST, tools.mv.getPawnCatch(), generator);
			tools.addMove(Direction.NORTH_WEST, tools.mv.getPawnCatch(), generator);
		}
	}
	
	protected boolean isInsufficientMaterial() {
		int whiteKnightOrBishopCount = 0;
		int blackKnightOrBishopCount = 0;
		final BoardExplorer exp = board.getExplorer();
		do {
			final Piece p = exp.getPiece();
			if (p!=null && !PieceKind.KING.equals(p.getKind())) {
				if (Piece.BLACK_BISHOP.equals(p) || Piece.BLACK_KNIGHT.equals(p)) {
					blackKnightOrBishopCount++;
				} else if (Piece.WHITE_BISHOP.equals(p) || Piece.WHITE_KNIGHT.equals(p)) {
					whiteKnightOrBishopCount++;
				} else {
					return false;
				}
			}
			
		} while (exp.next());
		return whiteKnightOrBishopCount <= 1 && blackKnightOrBishopCount <= 1;
	}
	
	protected Status getStatus() {
		if (status==null) {
			status=buildStatus();
		}
		return status;
	}

	private Status buildStatus() {
		if (isInsufficientMaterial() || board.getHalfMoveCount()>=100 || isDrawByRepetition()) {
			return Status.DRAW;
		}
		if (board.getMoves().isEmpty()) {
			if (board.isCheck()) {
				return board.getActiveColor().equals(Color.WHITE) ? Status.BLACK_WON : Status.WHITE_WON;
			} else {
				return Status.DRAW;
			}
		} else {
			return Status.PLAYING;
		}
	}

	protected boolean isDrawByRepetition() {
        final List<Long> history = board.getKeyHistory();
		final int size = Math.min(history.size(), board.getHalfMoveCount());
		if (size<6) {
			return false;
		}
		final Long key = board.getZobristKey();
		int repetition = 0;
		for (int i = history.size()-2; i>=history.size()-size; i -= 2) {
			if (history.get(i).equals(key)) {
				repetition++;
				if (repetition==2) {
					return true;
				}
			}
		}
		return false;
	}
}
