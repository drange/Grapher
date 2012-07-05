package no.uib.ii.algo.st8.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PowersetIterator<T> implements Iterator<Collection<T>> {

	private BigInteger charactheristic = BigInteger.ZERO;

	private final BigInteger size;

	private final List<T> set;

	public PowersetIterator(Set<T> input) {
		this.set = new ArrayList<T>(input.size());
		this.set.addAll(input);
		size = new BigInteger("" + ((long) Math.pow(2, set.size())) + "");
	}

	public boolean hasNext() {
		return charactheristic.compareTo(size) < 0;
	}

	public Set<T> next() {
		Set<T> ret = new HashSet<T>();
		for (int i = 0; i < set.size(); i++) {
			if (charactheristic.testBit(i))
				ret.add(set.get(i));
		}
		charactheristic = charactheristic.add(BigInteger.ONE);
		return ret;
	}

	public void remove() {
		throw new IllegalArgumentException();
	}
}
