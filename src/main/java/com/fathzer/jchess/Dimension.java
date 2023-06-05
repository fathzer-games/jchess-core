package com.fathzer.jchess;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(exclude = {"size","zobristKeyBuilder"})
public class Dimension {
	public static final Dimension STANDARD = new Dimension(8,8);
	
	private final int width;
	private final int height;
	private final int size;
	private final ZobristKeyBuilder zobristKeyBuilder;

	public Dimension(int width, int heigth) {
		this.width = width;
		this.height = heigth;
		this.size = width*heigth;
		this.zobristKeyBuilder = new ZobristKeyBuilder(this);
	}
}
