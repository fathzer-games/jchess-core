package com.fathzer.jchess.generic;

import static com.fathzer.games.Color.*;
import static com.fathzer.jchess.PieceKind.*;

import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

/** A class that can detect insufficient material draws.
 */
public class InsufficientMaterialDetector {
	private int victoryHopePiecesCounter;
	private int whiteMinorPiecesCounter;
	private int blackMinorPiecesCounter;

	/** Adds a piece (typically called during board construction for every piece of the board).
	 * @param piece The piece to add
	 */
	public void add(Piece piece) {
		take(piece, 1);
	}

	/** Removes a piece (typically called when a move is a piece catch).
	 * @param piece The piece to remove
	 */
	public void remove(Piece piece) {
		take(piece, -1);
	}

	private void take(Piece piece, int increment) {
		final PieceKind kind = piece.getKind();
		if (kind==BISHOP || kind==KNIGHT) {
			if (piece.getColor()==WHITE) {
				whiteMinorPiecesCounter = whiteMinorPiecesCounter + increment;
			} else {
				blackMinorPiecesCounter = blackMinorPiecesCounter + increment;
			}
		} else if (kind!=KING) {
			victoryHopePiecesCounter = victoryHopePiecesCounter+increment;
		}
	}

	/** Tests whether there no more enough material to win. 
	 * @return true if there's no more enough material to win.
	 */
	public boolean isInsufficient() {
		return victoryHopePiecesCounter==0 && whiteMinorPiecesCounter<=1 && blackMinorPiecesCounter<=1;
	}
	
	/** Copy another detector in this.
	 * @param other The other insufficient material detector
	 */
	public void copy(InsufficientMaterialDetector other) {
		this.blackMinorPiecesCounter = other.blackMinorPiecesCounter;
		this.whiteMinorPiecesCounter = other.whiteMinorPiecesCounter;
		this.victoryHopePiecesCounter = other.victoryHopePiecesCounter;
	}
}
