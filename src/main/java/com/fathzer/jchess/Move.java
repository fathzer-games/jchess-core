package com.fathzer.jchess;

public interface Move {
	int getFrom();
	int getTo();
	default Piece promotedTo() {
		return null;
	}
}
