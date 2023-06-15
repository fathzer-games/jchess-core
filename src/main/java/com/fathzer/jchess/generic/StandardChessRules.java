package com.fathzer.jchess.generic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import com.fathzer.games.Color;
import com.fathzer.games.Status;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.generic.DefaultMoveExplorer.MoveGenerator;

import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.ChessRules;
import com.fathzer.jchess.Direction;

public class StandardChessRules implements ChessRules {
	public static final ChessRules INSTANCE = new StandardChessRules();
	public static final ChessRules PERFT = new StandardChessRules() {
		@Override
		protected boolean isInsufficientMaterial(Board<Move> board) {
			return false;
		}
	};
	
	protected StandardChessRules() {
		// Nothing to do
	}
	
	@Override
	public Board<Move> newGame() {
		return FENParser.from(FENParser.NEW_STANDARD_GAME);
	}
	
	@Override
	public boolean isCheck(Board<Move> board) {
		Color color = board.getActiveColor();
		return isThreatened(board, color.opposite(), board.getKingPosition(color));
	}

	@Override
	public ChessGameState getState(Board<Move> board) {
		//FIXME This cast is ugly. It's the result of not having getState in Board class! 
		final DefaultMoveExplorer tools = new DefaultMoveExplorer((ChessBoard) board);
		final ChessGameState list = tools.getMoves();
		if (isInsufficientMaterial(board)) {
			list.setStatus(Status.DRAW);
			return list;
		}
		return buildMoves(tools);
	}

	private ChessGameState buildMoves(DefaultMoveExplorer tools) {
		final Color color = tools.getBoard().getActiveColor();
		final ChessGameState moves = tools.getMoves();
		final BoardExplorer exp = tools.getFrom();
		do {
			if (exp.getPiece()!=null && color==exp.getPiece().getColor()) {
				addPossibleMoves(tools);
			}
		} while (exp.next());
		if (moves.size()==0) {
			if (tools.isCheck()) {
				moves.setStatus(color.equals(Color.WHITE) ? Status.BLACK_WON : Status.WHITE_WON);
			} else {
				moves.setStatus(Status.DRAW);
			}
		}
		return moves;
	}

	protected boolean isInsufficientMaterial(Board<Move> board) {
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

	private void addPossibleMoves(DefaultMoveExplorer tools) {
		final Piece piece = tools.getFrom().getPiece();
		tools.getTo().reset(tools.getFrom().getIndex());
		if (PieceKind.ROOK.equals(piece.getKind()) || PieceKind.BISHOP.equals(piece.getKind()) || PieceKind.QUEEN.equals(piece.getKind())) {
			piece.getKind().getDirections().stream().forEach(d->tools.addAllMoves(d, tools.mv.getDefault()));
		} else if (PieceKind.KNIGHT.equals(piece.getKind())) {
			piece.getKind().getDirections().stream().forEach(d->tools.addMove(d, tools.mv.getDefault()));
		} else if (PieceKind.KING.equals(piece.getKind())) {
			addKingMoves(tools);
		} else if (PieceKind.PAWN.equals(piece.getKind())) {
			addPawnMoves(tools);
		} else {
			throw new IllegalArgumentException("Unknown piece kind: "+piece.getKind());
		}
	}
	
	private void addKingMoves(DefaultMoveExplorer tools) {
		// StandardMoves => King can't go to attacked cell
		PieceKind.KING.getDirections().stream().forEach(d->tools.addMove(d, tools.mv.getKing()));
		// Castlings
		if (!tools.isCheck()) {
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
	private void tryCastling(DefaultMoveExplorer tools, Castling castling) {
		if (tools.getBoard().hasCastling(castling)) {
			final int kingPosition = tools.getFrom().getIndex(); 
			final int kingDestination = tools.getBoard().getKingDestination(castling);
			final int rookPosition = tools.getBoard().getInitialRookPosition(castling);
			final int rookDestination  = kingDestination + castling.getSide().getRookOffset();
			if (getFreeCells(kingPosition, rookPosition, kingDestination, rookDestination).allMatch(p-> tools.getBoard().getPiece(p)==null) &&
			!tools.mv.isThreatened(castling.getColor().opposite(), getSafeCells(kingPosition, kingDestination))) {
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
	protected void addCastling(ChessGameState moves, int kingPosition, int rookPosition, int kingDestination, int rookDestination) {
		moves.add(kingPosition, kingDestination);
	}
	
	private IntStream getFreeCells(int kingPosition, int rookPosition, int kingDestination, int rookDestination) {
		final List<Integer> positions = Arrays.asList(kingDestination,kingPosition,rookPosition,rookDestination);
		Collections.sort(positions);
		return IntStream.range(positions.get(0), positions.get(3)).filter(i-> i!=rookPosition && i!=kingPosition);
	}

	/** Gets the positions that should be safe (not attacked) to have the castling allowed.
	 * @return an int stream. Please note that the king's cell is not returned in this array as the check state is verified before this method is called.
	 */
	private IntStream getSafeCells(int kingPosition, int kingDestination) {
		if (kingPosition==kingDestination) {
			return IntStream.empty();
		} else if (kingPosition<kingDestination) {
			return IntStream.range(kingPosition+1, kingDestination+1);
		} else {
			return IntStream.range(kingDestination, kingPosition);
		}
	}

	private void addPawnMoves(DefaultMoveExplorer tools) {
		final boolean black = Color.BLACK == tools.getFrom().getPiece().getColor();
		// Take care of promotion when generating move
		final int promotionRow = black?tools.getBoard().getDimension().getHeight()-1:0;
		final IntPredicate promoted = i -> tools.getBoard().getCoordinatesSystem().getRow(i)==promotionRow;
		final MoveGenerator generator = (m, f, t) -> {
			if (promoted.test(t)) {
				m.add(f, t, black ? Piece.BLACK_KNIGHT : Piece.WHITE_KNIGHT);
				m.add(f, t, black ? Piece.BLACK_QUEEN : Piece.WHITE_QUEEN);
				m.add(f, t, black ? Piece.BLACK_ROOK : Piece.WHITE_ROOK);
				m.add(f, t, black ? Piece.BLACK_BISHOP : Piece.WHITE_BISHOP);
			} else {
				DefaultMoveExplorer.DEFAULT.generate(m, f, t);
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
	
	private boolean isThreatened(Board<Move> board, Color color, int position) {
		return new AttackDetector(board.getDirectionExplorer(-1)).isAttacked(position, color);
	}
}
