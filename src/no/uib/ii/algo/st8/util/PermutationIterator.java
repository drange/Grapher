package no.uib.ii.algo.st8.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PermutationIterator<T> implements Iterator<Set<T>> {

	private final List<T> set;

	public PermutationIterator(Set<T> input) {
		this.set = new ArrayList<T>(input.size());
		this.set.addAll(input);
	}

	public boolean hasNext() {
		// TODO Markus
		return false;
	}

	public Set<T> next() {
		// TODO Markus
		return null;
	}

	public void remove() {
		throw new IllegalArgumentException();
	}
}
