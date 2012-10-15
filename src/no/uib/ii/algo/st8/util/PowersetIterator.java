package no.uib.ii.algo.st8.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author pgd
 * 
 * @param <T>
 */
public class PowersetIterator<T> implements Iterator<Collection<T>> {

	private final int n;
	private int k = 0;
	private final List<T> set;

	private NChooseKIterator<T> currentIterator;

	public PowersetIterator(Set<T> input) {
		this.set = new ArrayList<T>(input.size());
		this.set.addAll(input);
		n = set.size();
		currentIterator = new NChooseKIterator<T>(set, k);
	}

	public boolean hasNext() {
		return k < n || currentIterator.hasNext();
	}

	public Collection<T> next() {
		if (currentIterator.hasNext())
			return currentIterator.next();
		currentIterator = new NChooseKIterator<T>(set, ++k);
		return currentIterator.next();
	}

	public void remove() {
		throw new UnsupportedOperationException(
				"Cannot remove a set using this iterator");
	}

	public static class PowersetIteratorDescending<S> implements
			Iterator<Collection<S>> {

		private final int n;
		private int k = 0;
		private final List<S> set;

		private NChooseKIterator<S> currentIterator;

		public PowersetIteratorDescending(Set<S> input) {
			this.set = new ArrayList<S>(input.size());
			this.set.addAll(input);
			n = set.size();
			k = n;
			currentIterator = new NChooseKIterator<S>(set, k);
		}

		public boolean hasNext() {
			return k > 0 || currentIterator.hasNext();
		}

		public Collection<S> next() {
			if (currentIterator.hasNext())
				return currentIterator.next();
			currentIterator = new NChooseKIterator<S>(set, --k);
			return currentIterator.next();
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"Cannot remove a set using this iterator");
		}
	}
}