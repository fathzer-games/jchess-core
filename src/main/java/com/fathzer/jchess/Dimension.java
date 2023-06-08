package com.fathzer.jchess;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class Dimension {
	public static final Dimension STANDARD = new Dimension(8,8);
	
	@Getter
	private final int width;
	@Getter
	private final int height;

	public Dimension(int width, int heigth) {
		this.width = width;
		this.height = heigth;
	}
}
