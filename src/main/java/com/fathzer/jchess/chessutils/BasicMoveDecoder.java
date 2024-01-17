package com.fathzer.jchess.chessutils;

import static com.fathzer.chess.utils.Pieces.PAWN;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

public final class BasicMoveDecoder {
	private BasicMoveDecoder() {
		super();
	}
	
	public static int getCapturedType(Board<Move> board, Move move) {
		final Piece moving = board.getPiece(move.getFrom());
		if (move.getTo()==board.getEnPassantTarget() && PieceKind.PAWN==moving.getKind()) {
			return PAWN; // A pawn is captured
		} else {
			final Piece piece = board.getPiece(move.getTo());
			// Be aware of castling in chess 960 where we can consider the king captures its own rook!
			return piece!=null && piece.getColor()!=moving.getColor() ? JChessBoardExplorer.fromPieceType(piece.getKind()) : 0;
		}
	}

	public static int getPromotionType(Board<Move> board, Move move) {
		final Piece promotion = move.getPromotion();
		return promotion==null ? 0 : JChessBoardExplorer.fromPieceType(promotion.getKind());
	}
}
