package com.fathzer.jchess.generic;

import java.util.function.Supplier;

import com.fathzer.games.Color;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

public class MoveHelperHolder implements Supplier<MoveHelper> {
	private final SimpleMoveHelper simple = new SimpleMoveHelper();
	private final EnPassantMoveHelper enPassant = new EnPassantMoveHelper();
	private final CastlingMoveHelper castling = new CastlingMoveHelper();
	private MoveHelper unmakeAction;
	
	private static class SimpleMoveHelper extends MoveHelper {
		private Piece toWas;
		
		@Override
		public void unmakePieces(Piece[] p) {
			p[fromIndex] = piece;
			p[toIndex] = toWas;
		}

		@Override
		public void unmakeKingPosition(int[] kingPositions, int index) {
			if (piece.getKind()==PieceKind.KING) {
				kingPositions[index] = fromIndex;
			}
		}

		@Override
		public boolean isKingSafetyTestRequired() {
			return piece.getKind()==PieceKind.KING;
		}

		@Override
		public boolean shouldIncHalfMoveCount() {
			return PieceKind.PAWN!=piece.getKind() && toWas==null;
		}

		@Override
		public Piece getCaptured() {
			return toWas;
		}
	}
	
	private static class EnPassantMoveHelper extends MoveHelper {
		private int capturedIndex;
		
		@Override
		public void unmakePieces(Piece[] p) {
			p[fromIndex] = piece;
			p[toIndex] = null;
			p[capturedIndex] = getCaptured();
		}
		
		@Override
		public Piece getCaptured() {
			return piece==Piece.BLACK_PAWN ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;
		}	
	}

	private static class CastlingMoveHelper  extends MoveHelper {
		private int rookFrom;
		private int rookTo;

		@Override
		public void unmakePieces(Piece[] p) {
			// Be aware of Chess360 where rook and king can move to the other castling piece
			p[toIndex] = null;
			p[rookTo] = null;
			p[fromIndex] = piece;
			p[rookFrom] = piece==Piece.BLACK_KING ? Piece.BLACK_ROOK : Piece.WHITE_ROOK;
		}

		@Override
		public void unmakeKingPosition(int[] kingPositions, int index) {
			kingPositions[index] = fromIndex;
		}

		@Override
		public boolean shouldIncHalfMoveCount() {
			return true;
		}

		@Override
		public boolean isCastling() {
			return true;
		}
	}

	public MoveHelper get() {
		return this.unmakeAction;
	}
	
	public void setSimple(Piece fromWas, int fromIndex, Piece toWas, int toIndex) {
		simple.fromIndex = fromIndex;
		simple.piece = fromWas;
		simple.toIndex = toIndex;
		simple.toWas = toWas;
		this.unmakeAction = simple;
	}

	public void setEnPassant(Piece activePawn, int fromIndex, int toIndex, int capturedIndex) {
		enPassant.fromIndex = fromIndex;
		enPassant.piece = activePawn;
		enPassant.toIndex = toIndex;
		enPassant.capturedIndex = capturedIndex;
		this.unmakeAction = enPassant;
	}
	
	public void setCastling (Castling castling, int kingFromIndex, int kingToIndex, int rookFromIndex, int rookToIndex) {
		this.castling.piece = castling.getColor()==Color.WHITE ? Piece.WHITE_KING : Piece.BLACK_KING;
		this.castling.fromIndex = kingFromIndex;
		this.castling.toIndex = kingToIndex;
		this.castling.rookFrom = rookFromIndex;
		this.castling.rookTo = rookToIndex;
		this.unmakeAction = this.castling;
	}
}
