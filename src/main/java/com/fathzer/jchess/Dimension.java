package com.fathzer.jchess;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(exclude = {"size"})
public class Dimension {
	public static final Dimension STANDARD = new Dimension(8,8);
	
	private final int width;
	private final int height;
	private final int size;

	public Dimension(int width, int heigth) {
		this.width = width;
		this.height = heigth;
		this.size = width*heigth;
	}
}
