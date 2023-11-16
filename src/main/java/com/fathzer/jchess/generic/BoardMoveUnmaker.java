package com.fathzer.jchess.generic;

import java.util.function.Consumer;

import com.fathzer.games.Color;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Piece;

public class BoardMoveUnmaker implements Consumer<Piece[]> {
	private final SimpleMoveBoardUnmaker simple = new SimpleMoveBoardUnmaker();
	private final EnPassantMoveBoardUnmaker enPassant = new EnPassantMoveBoardUnmaker();
	private final CastlingMoveBoardUnmaker castling = new CastlingMoveBoardUnmaker();
	private Consumer<Piece[]> unmakeAction;
	
	private static class SimpleMoveBoardUnmaker implements Consumer<Piece[]> {
		private int fromIndex;
		private Piece fromWas;
		private int toIndex;
		private Piece toWas;
		
		@Override
		public void accept(Piece[] p) {
			p[fromIndex] = fromWas;
			p[toIndex] = toWas;
		}
	}
	
	private static class EnPassantMoveBoardUnmaker implements Consumer<Piece[]> {
		private int fromIndex;
		private Piece fromWas;
		private int toIndex;
		private int capturedIndex;
		
		@Override
		public void accept(Piece[] p) {
			p[fromIndex] = fromWas;
			p[toIndex] = null;
			p[capturedIndex] = fromWas==Piece.BLACK_PAWN ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;
		}
	}

	private static class CastlingMoveBoardUnmaker implements Consumer<Piece[]> {
		private Piece king;
		private int kingFrom;
		private int kingTo;
		private int rookFrom;
		private int rookTo;

		@Override
		public void accept(Piece[] p) {
			// Be aware of Chess360 where rook and king can move to the other castling piece
			p[kingTo] = null;
			p[rookTo] = null;
			p[kingFrom] = king;
			p[rookFrom] = king==Piece.BLACK_KING ? Piece.BLACK_ROOK : Piece.WHITE_ROOK;
		}
	}

	@Override
	public void accept(Piece[] p) {
		this.unmakeAction.accept(p);
	}
	
	public void reset() {
		this.unmakeAction = null;
	}
	
	public boolean isSet() {
		return this.unmakeAction != null;
	}

	public void setSimple(Piece fromWas, int fromIndex, Piece toWas, int toIndex) {
		simple.fromIndex = fromIndex;
		simple.fromWas = fromWas;
		simple.toIndex = toIndex;
		simple.toWas = toWas;
		this.unmakeAction = simple;
	}

	public void setEnPassant(Piece activePawn, int fromIndex, int toIndex, int capturedIndex) {
		enPassant.fromIndex = fromIndex;
		enPassant.fromWas = activePawn;
		enPassant.toIndex = toIndex;
		enPassant.capturedIndex = capturedIndex;
		this.unmakeAction = enPassant;
	}
	
	public void setCastling (Castling castling, int kingFromIndex, int kingToIndex, int rookFromIndex, int rookToIndex) {
		this.castling.king = castling.getColor()==Color.WHITE ? Piece.WHITE_KING : Piece.BLACK_KING;
		this.castling.kingFrom = kingFromIndex;
		this.castling.kingTo = kingToIndex;
		this.castling.rookFrom = rookFromIndex;
		this.castling.rookTo = rookToIndex;
		this.unmakeAction = this.castling;
	}
}
