package no.uib.ii.algo.st8.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import no.uib.ii.algo.st8.util.PowersetIterator;

/**
 * 
 * @author Johan Alexander Nordstrand Rusvik
 * 
 * @param <T>
 */
public class TwoPartitionsIterator<T> implements Iterator<Collection<T>> {

	private PowersetIterator<T> superIterator;
	private int n;
	private int partitions;
	private Collection<T> set;
	private Collection<T> firstPart;
	private Collection<T> secondPart;

	public TwoPartitionsIterator(Collection<T> input) {
		superIterator = new PowersetIterator<T>(input);
		n = (PowersetIterator.twoPower(input.size())) / 2;
		partitions = 0;
		set = input;
	}

	@Override
	public boolean hasNext() {
		return partitions <= n;
	}

	@Override
	public Collection<T> next() {
		firstPart = superIterator.next();
		partitions++;
		// if(firstPart.isEmpty()){// || firstPart.size() == 1) {
		// next();
		// }
		calculateComplementary();
		return firstPart;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("TwoPartitionsIterator.remove() is not implemented!");
	}

	public Collection<T> currentSecondPart() {
		return secondPart;
	}

	private void calculateComplementary() {
		secondPart = new HashSet<T>();
		secondPart.addAll(set);
		secondPart.removeAll(firstPart);
	}

}
