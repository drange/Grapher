package no.uib.ii.algo.st8.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class NChooseKIterator<T> implements Iterator<Collection<T>> {

	private final ArrayList<T> elements;
	private final boolean[] characteristic;
	private final int k;
	private final int n;

	private boolean hasnext = true;

	public NChooseKIterator(Collection<T> elts, int k) {
		n = elts.size();
		this.k = k;
		if (this.n < this.k)
			throw new IllegalArgumentException(
					"n choose k demands to have n >= k, you had n="
							+ elts.size() + ", k=" + k);
		elements = new ArrayList<T>(elts.size());
		elements.addAll(elts);
		characteristic = new boolean[n];
		for (int i = 0; i < k; i++) {
			characteristic[i] = true;
		}
	}

	public boolean hasNext() {
		// if all the true's are in the k last positions, there are no next!
		return hasnext;
	}

	public Collection<T> next() {
		HashSet<T> ret = new HashSet<T>(k);
		for (int i = 0; i < n; i++) {
			if (characteristic[i])
				ret.add(elements.get(i));
		}

		if (hasNext())
			donext();

		return ret;
	}

	private void donext() {
		// find first 0 (from right)
		// // find first 1 after this
		// // // move to right
		// // // take every 1 right of this all the way to left (but after 1)
		int firstFromRightFalse = n;
		int numberOfTruesToRightOfFirstFalse = 0;
		for (int i = n - 1; i >= 0; i--) {
			if (!characteristic[i]) {
				firstFromRightFalse = i;
				break;
			} else {
				numberOfTruesToRightOfFirstFalse++;
				characteristic[i] = false;
			}
		}

		if (firstFromRightFalse == n) {
			// only happens if there are only 1's, i.e. n == k
			hasnext = false;
			return;
		}

		int firstFromRightTrue = n;
		for (int i = firstFromRightFalse; i >= 0; i--) {
			if (characteristic[i]) {
				firstFromRightTrue = i;
				break;
			}
		}
		if (firstFromRightTrue == n) {
			hasnext = false;
			return;
		}
		characteristic[firstFromRightTrue] = false;
		characteristic[firstFromRightTrue + 1] = true;
		for (int i = 0; i < numberOfTruesToRightOfFirstFalse; i++) {
			characteristic[firstFromRightTrue + 2 + i] = true;
		}
	}

	public void remove() {
	}

	public static int nChooseK(int n, int k) {
		if (k > n)
			throw new IllegalArgumentException("N Choose K must have k <= n: "
					+ k + " " + n);
		int numerator = PermutationIterator.factorial(n);
		int denomerator = PermutationIterator.factorial(n - k);
		return numerator / denomerator;
	}

	public static BigInteger nChooseK(BigInteger n, BigInteger k) {
		if (k.compareTo(n) > 0)
			throw new IllegalArgumentException("N Choose K must have k <= n: "
					+ k + " " + n);
		BigInteger numerator = PermutationIterator.factorial(n);
		BigInteger denomerator = PermutationIterator.factorial(n.subtract(k));
		return numerator.divide(denomerator);
	}

}
