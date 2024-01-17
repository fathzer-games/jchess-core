package com.fathzer.jchess.chessutils;

import static com.fathzer.jchess.PieceKind.*;
import static com.fathzer.jchess.chessutils.JChessBoardExplorer.*;

import com.fathzer.chess.utils.adapters.MoveData;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

public class JChessMoveData implements MoveData<Move, Board<Move>> {
	private CoordinatesSystem cs;
	private int movingIndex;
	private Piece movingPiece;
	private int movingDestination;
	private Piece promotion;
	private PieceKind captured;
	private int capturedIndex;
	private int castlingRookIndex;
	private int castlingRookDestinationIndex;
	
	private int toIndex(int index) {
		return index < 0 ? index : cs.getRow(index)*cs.getDimension().getWidth() + cs.getColumn(index);
	}

	@Override
	public int getMovingIndex() {
		return toIndex(movingIndex);
	}

	@Override
	public int getMovingPiece() {
		return toPiece(movingPiece);
	}

	@Override
	public int getMovingDestination() {
		return toIndex(movingDestination);
	}

	@Override
	public int getCapturedType() {
		return fromPieceType(captured);
	}

	private int getType(Piece piece) {
		return piece==null ? 0 : fromPieceType(piece.getKind());
	}

	@Override
	public int getCapturedIndex() {
		return toIndex(capturedIndex);
	}

	@Override
	public int getPromotionType() {
		return getType(promotion);
	}

	@Override
	public int getCastlingRookIndex() {
		return toIndex(castlingRookIndex);
	}

	@Override
	public int getCastlingRookDestinationIndex() {
		return toIndex(castlingRookDestinationIndex);
	}

	@Override
	public boolean update(Move move, Board<Move> board) {
		this.cs = board.getCoordinatesSystem();
		this.movingIndex = move.getFrom();
		this.movingPiece = board.getPiece(movingIndex);
		if (movingPiece==null) {
			return false;
		}
		final PieceKind movingType = movingPiece.getKind();
		if (movingType==KING) {
			this.promotion = null;
			final Castling castling = board.getCastling(movingIndex, move.getTo());
			if (castling!=null) {
				this.captured = null;
				this.movingDestination = board.getKingDestination(castling);
				this.castlingRookIndex = board.getInitialRookPosition(castling);
				this.castlingRookDestinationIndex = this.movingDestination + castling.getSide().getRookOffset();
			} else {
				this.castlingRookIndex = -1;
				this.movingDestination = move.getTo();
				setCaptured(board.getPiece(movingDestination));
			}
		} else {
			// Not a king move => no castling
			this.castlingRookIndex = -1;
			this.movingDestination = move.getTo();
			if (movingType==PAWN && movingDestination==board.getEnPassant()) {
				this.captured = PAWN;
				this.capturedIndex = board.getEnPassantTarget();
				this.promotion = null;
			} else {
				this.promotion = move.getPromotion();
				setCaptured(board.getPiece(movingDestination));
			}
		}
		return true;
	}
	
	private void setCaptured(Piece capturedPiece) {
		if (capturedPiece!=null) {
			this.captured = capturedPiece.getKind();
			this.capturedIndex = this.movingDestination;
		} else {
			this.captured = null;
		}
	}
}
