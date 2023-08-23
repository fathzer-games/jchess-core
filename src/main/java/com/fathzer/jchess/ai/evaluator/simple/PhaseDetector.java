package com.fathzer.jchess.ai.evaluator.simple;

import com.fathzer.jchess.Piece;

class PhaseDetector {
	private boolean blackQueen = false;
	private boolean whiteQueen = false;
	private boolean whiteRook = false;
	private boolean blackRook = false;
	private int whiteMinor = 0;
	private int blackMinor = 0;
	
	public void add(Piece piece) {
		switch (piece) {
			case BLACK_QUEEN : blackQueen=true; break;
			case WHITE_QUEEN : whiteQueen=true; break;
			case BLACK_ROOK : blackRook=true; break;
			case WHITE_ROOK : whiteRook=true; break;
			case BLACK_BISHOP : blackMinor++; break;
			case BLACK_KNIGHT : blackMinor++; break;
			case WHITE_BISHOP : whiteMinor++; break;
			case WHITE_KNIGHT : whiteMinor++; break;
		default:
			break;
		}
	}

	public Phase getPhase() {
		if (!blackQueen && !whiteQueen) {
			return Phase.END_GAME;
		}
		if ((blackQueen && (blackRook || blackMinor>1)) || (whiteQueen && (whiteRook || whiteMinor>1))) {
			return Phase.MIDDLE_GAME;
		}
		return Phase.END_GAME;
	}
}
