package com.fathzer.jchess;

import java.util.Random;

import com.fathzer.games.Color;

import lombok.Getter;

public class ZobristKeyBuilder {
	private static int lastSize=-1;
	private static ZobristKeyBuilder lastBuilder;
	
	private final int boardSize;
	private final long[][] piecesTable;
	private final long[] enPassantKeys;
	private final long[] castlingKeys;
	@Getter
	private final long turnKey;

	public static ZobristKeyBuilder get(int boardSize) {
		if (lastSize!=boardSize) {
			lastBuilder = new ZobristKeyBuilder(boardSize);
			lastSize = boardSize;
		}
		return lastBuilder;
	}
	
	private ZobristKeyBuilder(int boardSize) {
		this.boardSize = boardSize;
		Random random = new Random(0);
		piecesTable = new long[boardSize][Piece.values().length];
		populatePiecesTable(random);
		enPassantKeys = new long[boardSize];
		populate(enPassantKeys, random);
		castlingKeys = new long[Castling.values().length];
		populate(castlingKeys, random);
		turnKey = random.nextLong();
	}

	public long get(Board<?> board) {
		long result = 0;
		for (int pos = 0; pos < boardSize; pos++) {
			final Piece piece = board.getPiece(pos);
			if (piece != null) {
				result ^= piecesTable[pos][piece.ordinal()];
			}
		}
		if (board.getEnPassant() >= 0) {
			result ^= enPassantKeys[board.getEnPassant()];
		}
		if (Color.BLACK.equals(board.getActiveColor())) {
			result ^= turnKey;
		}
		for (Castling castling : Castling.ALL) {
			if (board.hasCastling(castling)) {
				result ^= castlingKeys[castling.ordinal()];
			}
		}
		return result;
	}

	private void populatePiecesTable(Random random) {
		for (int i = 0; i < piecesTable.length; i++) {
			for (int j = 0; j < piecesTable[i].length; j++) {
				piecesTable[i][j] = random.nextLong();
			}
		}
	}

	private static void populate(long[] array, Random random) {
		for (int i = 0; i < array.length; i++) {
			array[i] = random.nextLong();
		}
	}

	public long getKey(int pos, Piece piece) {
		return piecesTable[pos][piece.ordinal()];
	}

	public long getKey(Castling castling) {
		return this.castlingKeys[castling.ordinal()];
	}
	
	public long getKey(int enPassant) {
		return this.enPassantKeys[enPassant];
	}
}
