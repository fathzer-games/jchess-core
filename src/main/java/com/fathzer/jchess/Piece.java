package com.fathzer.jchess;

import lombok.AllArgsConstructor;
import static com.fathzer.jchess.PieceKind.*;

import com.fathzer.games.Color;

import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Piece {
	BLACK_PAWN(Color.BLACK,PAWN,"p"), WHITE_PAWN(Color.WHITE,PAWN,"P"),
	BLACK_ROOK(Color.BLACK,ROOK,"r"), WHITE_ROOK(Color.WHITE,ROOK,"R"),
	BLACK_KNIGHT(Color.BLACK,KNIGHT,"n"), WHITE_KNIGHT(Color.WHITE,KNIGHT,"N"),
	BLACK_BISHOP(Color.BLACK,BISHOP,"b"), WHITE_BISHOP(Color.WHITE,BISHOP,"B"),
	BLACK_QUEEN(Color.BLACK,QUEEN,"q"), WHITE_QUEEN(Color.WHITE,QUEEN,"Q"),
	BLACK_KING(Color.BLACK,KING,"k"), WHITE_KING(Color.WHITE,KING,"K");
	
	private Color color;
	private PieceKind kind;
	private String notation;
}
