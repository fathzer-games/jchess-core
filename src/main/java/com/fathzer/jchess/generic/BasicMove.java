package com.fathzer.jchess.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

import com.fathzer.jchess.Move;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
class BasicMove implements Move {
	//TODO This class is not thread safe because of its unsafe moves variable that is concurrently populated
	private static final ArrayList<ArrayList<Move>> moves = new ArrayList<>();
	private final int from;
	private final int to;
	
	static Move get(int origin, int destination) {
		final ArrayList<Move> oList = getElement(moves, origin, ArrayList::new);
		return getElement(oList, destination, () -> new BasicMove(origin, destination));
	}

	private static <T> T getElement(ArrayList<T> lst, int index, Supplier<T> elementBuilder) {
		if (index>=lst.size()) {
			// Expand the list
			lst.addAll(Collections.nCopies(index-lst.size()+1, null));
		}
		T result = lst.get(index);
		if (result==null) {
			result = elementBuilder.get();
			lst.ensureCapacity(index+1);
			lst.set(index, result);
		}
		return result;
	}
}