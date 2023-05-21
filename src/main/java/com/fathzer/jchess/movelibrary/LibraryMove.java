package com.fathzer.jchess.movelibrary;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** A move proposed by a library.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LibraryMove {
	private String coord;
	private int count;
	
	/** Constructor.
	 * @param coord The uci encoded move coordinates
	 * @param count The number of time this move was played according to the library
	 */
	public LibraryMove(String coord, int count) {
		super();
		this.coord = coord;
		this.count = count;
	}
	
	/** The uci encoded move coordinates
	 * @return The move coordinates
	 */
	public String getCoord() {
		return coord;
	}
	/** The number of time this move was played according to the library
	 * @return a positive int
	 */
	public int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return coord + "/" + count;
	}
}
