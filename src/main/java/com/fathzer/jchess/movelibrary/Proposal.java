package com.fathzer.jchess.movelibrary;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A proposal of the library, typically, the possible moves for an opening.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Proposal {
	private String name;
	private int count;
	private List<LibraryMove> moves;
}
