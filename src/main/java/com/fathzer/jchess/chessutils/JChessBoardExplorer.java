package com.fathzer.jchess.chessutils;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

public class JChessBoardExplorer implements com.fathzer.chess.utils.adapters.BoardExplorer {
	
	private final BoardExplorer explorer;
	private final CoordinatesSystem cs;
	
	public JChessBoardExplorer(Board<Move> board) {
		this.cs = board.getCoordinatesSystem();
		explorer = board.getExplorer();
		if (explorer.getPiece()==null) {
			next();
		}
	}

	@Override
	public boolean next() {
		if (!explorer.next()) {
			return false;
		}
		final Piece p = explorer.getPiece();
		return p!=null || next();
	}

	@Override
	public int getIndex() {
		return toIndex(explorer.getIndex());
	}

	@Override
	public int getPiece() {
		return toPiece(explorer.getPiece());
	}

	static int fromPieceType(PieceKind type) {
		return type==null ? 0 : type.ordinal()+1;
	}

	static int toPiece(Piece piece) {
		final int index = fromPieceType(piece.getKind());
		return piece.getColor()==Color.WHITE ? index : -index;
	}
	
	int toIndex(int index) {
		return cs.getRow(index)*cs.getDimension().getWidth() + cs.getColumn(index);
	}
}
