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
import com.fathzer.jchess.util.BiIntPredicate;

import lombok.Getter;

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
	
	private static class Tools {
		private Board<Move> board;
		private AttackDetector attacks;
		private DefaultMoveExplorer explorer;
		private MoveValidator mv;
		private BoardExplorer exp;
		@Getter
		private boolean check;
		
		public Tools(Board<Move> board) {
			this.board = board;
			this.attacks = new AttackDetector(board);
			this.explorer = new DefaultMoveExplorer(board);
			this.exp = board.getCoordinatesSystem().buildExplorer(-1);
			Color color = board.getActiveColor();
			this.check = attacks.isAttacked(board.getKingPosition(color), color.opposite());
			this.mv = new MoveValidator(board, attacks, check);
		}
	}
	
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
		final ChessGameState list = board.newMoveList();
		if (isInsufficientMaterial(board)) {
			list.setStatus(Status.DRAW);
			return list;
		}
		return buildMoves(board, list);
	}

	private ChessGameState buildMoves(Board<Move> board, final ChessGameState list) {
		final Tools tools = new Tools(board);
		final Color color = board.getActiveColor();
		IntStream.range(0, board.getDimension().getSize()) //FIXME Works only with "old school" board
				.filter(i -> board.getPiece(i)!=null && color.equals(board.getPiece(i).getColor()))
				.forEach(i -> addPossibleMoves(list, tools, i));
		if (list.size()==0) {
			if (tools.isCheck()) {
				list.setStatus(board.getActiveColor().equals(Color.WHITE) ? Status.BLACK_WON : Status.WHITE_WON);
			} else {
				list.setStatus(Status.DRAW);
			}
		}
		return list;
	}

	protected boolean isInsufficientMaterial(Board<Move> board) {
		int whiteKnightOrBishopCount = 0;
		int blackKnightOrBishopCount = 0;
		for (int i = 0; i < board.getDimension().getSize(); i++) { //FIXME Works only with "old school" board
			final Piece p = board.getPiece(i);
			if (p!=null && !PieceKind.KING.equals(p.getKind())) {
				if (Piece.BLACK_BISHOP.equals(p) || Piece.BLACK_KNIGHT.equals(p)) {
					blackKnightOrBishopCount++;
				} else if (Piece.WHITE_BISHOP.equals(p) || Piece.WHITE_KNIGHT.equals(p)) {
					whiteKnightOrBishopCount++;
				} else {
					return false;
				}
			}
		}
		return whiteKnightOrBishopCount <= 1 && blackKnightOrBishopCount <= 1;
	}

	private void addPossibleMoves(ChessGameState list, Tools tools, int position) {
		final Piece piece = tools.board.getPiece(position);
		if (piece!=null) {
			tools.exp.restart(position);
			if (PieceKind.ROOK.equals(piece.getKind()) || PieceKind.BISHOP.equals(piece.getKind()) || PieceKind.QUEEN.equals(piece.getKind())) {
				addBasicPieceMove(list, tools, piece.getKind(), Integer.MAX_VALUE, tools.mv.getDefault());
			} else if (PieceKind.KNIGHT.equals(piece.getKind())) {
				addBasicPieceMove(list, tools, piece.getKind(), 1, tools.mv.getDefault());
			} else if (PieceKind.KING.equals(piece.getKind())) {
				addKingMoves(list, tools);
			} else if (PieceKind.PAWN.equals(piece.getKind())) {
				addPawnMoves(list, tools);
			} else {
				throw new IllegalArgumentException("Unknown piece kind: "+piece.getKind());
			}
		}
	}

	private void addBasicPieceMove(ChessGameState moves, Tools tools, PieceKind piece, int maxIter, BiIntPredicate validator) {
		piece.getDirections().stream().forEach(d->tools.explorer.addMoves(moves, tools.exp, d, maxIter, validator));
	}
	
	private void addKingMoves(ChessGameState moves, Tools tools) {
		final Color c = tools.board.getPiece(tools.exp.getStartPosition()).getColor();
		// StandardMoves => King can't go to attacked cell
		addBasicPieceMove(moves, tools, PieceKind.KING, 1, tools.mv.getKing());
		// Castlings
		if (!tools.check) {
			// No castlings allowed when you're in check
			if (Color.WHITE.equals(c)) {
				tryCastling(moves, tools, Castling.WHITE_KING_SIDE);
				tryCastling(moves, tools, Castling.WHITE_QUEEN_SIDE);
			} else {
				tryCastling(moves, tools, Castling.BLACK_KING_SIDE);
				tryCastling(moves, tools, Castling.BLACK_QUEEN_SIDE);
			}
		}
	}
	
	private void tryCastling(ChessGameState moves, Tools tools, Castling castling) {
		if (tools.board.hasCastling(castling)) {
			final int kingPosition = tools.exp.getStartPosition();
			final int kingDestination = tools.board.getKingDestination(castling);
			final int rookPosition = tools.board.getInitialRookPosition(castling);
			final int rookDestination  = kingDestination + castling.getSide().getRookOffset();
			if (getFreeCells(tools.board, kingPosition, rookPosition, kingDestination, rookDestination).allMatch(p-> tools.board.getPiece(p)==null) &&
			!isThreatened(tools.board, castling.getColor().opposite(), getSafeCells(tools.exp.getStartPosition(), kingDestination))) {
				addCastling(moves, kingPosition, rookPosition, kingDestination, rookDestination);
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
	
	private IntStream getFreeCells(Board<Move> board, int kingPosition, int rookPosition, int kingDestination, int rookDestination) {
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

	private void addPawnMoves(ChessGameState moves, Tools tools) {
		final boolean black = Color.BLACK.equals(tools.board.getPiece(tools.exp.getStartPosition()).getColor());
		// Take care of promotion when generating move
		final IntPredicate promoted = black ? i -> i >= tools.board.getDimension().getSize()-tools.board.getDimension().getWidth() : i -> i < tools.board.getDimension().getWidth(); //FIXME Works only with "old school" board
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
		// Standard moves (no catch)
		final int startRow = black ? 1 : tools.board.getDimension().getHeight()-2;
		final int countAllowed = tools.board.getCoordinatesSystem().getRow(tools.exp.getStartPosition()) == startRow ? 2 : 1;
		if (black) {
			tools.explorer.addMoves(moves, tools.exp, Direction.SOUTH, countAllowed, tools.mv.getPawnNoCatch(), generator);
			// Catches (including En-passant)
			tools.explorer.addMoves(moves, tools.exp, Direction.SOUTH_EAST, 1, tools.mv.getPawnCatch(), generator);
			tools.explorer.addMoves(moves, tools.exp, Direction.SOUTH_WEST, 1, tools.mv.getPawnCatch(), generator);
		} else {
			tools.explorer.addMoves(moves, tools.exp, Direction.NORTH, countAllowed, tools.mv.getPawnNoCatch(), generator);
			// Catches (including En-passant)
			tools.explorer.addMoves(moves, tools.exp, Direction.NORTH_EAST, 1, tools.mv.getPawnCatch(), generator);
			tools.explorer.addMoves(moves, tools.exp, Direction.NORTH_WEST, 1, tools.mv.getPawnCatch(), generator);
		}
	}
	
	private boolean isThreatened(Board<Move> board, Color color, int position) {
		return new AttackDetector(board).isAttacked(position, color);
	}
	
	private boolean isThreatened(Board<Move> board, Color color, IntStream positions) {
		final AttackDetector d = new AttackDetector(board);
		return positions.anyMatch(pos -> d.isAttacked(pos, color));
	}
}
