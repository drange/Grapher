package no.uib.ii.algo.st8.algorithms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;


import no.uib.ii.algo.st8.util.Neighbors;


import org.jgrapht.graph.SimpleGraph;

/**
 * I think this is a maximal clique enumerator, could possibly be improved, but
 * hardly.
 * 
 * @author pdr081
 * 
 */
public class MaximalClique<V, E> extends Algorithm<V, E, Collection<V>> {

	public MaximalClique(SimpleGraph<V, E> graph) {
		super(graph);
	}

	public Collection<V> findExactMaximumClique(SimpleGraph<V, E> graph) {
		if (graph == null || graphSize() == 0)
			return Collections.emptySet();

		Collection<V> maximum = null;
		int maxSize = graph.vertexSet().size();

		MaximalClique<V, E> mc = new MaximalClique<V, E>(graph);
		for (Collection<V> maxCliq : mc.maxCliques()) {
			if (cancelFlag)
				return null;
			if (maximum == null || maxCliq.size() > maximum.size()) {
				maximum = maxCliq;
				progress(maximum.size(), maxSize);
			}
		}

		return maximum;
	}

	public Collection<Collection<V>> maxCliques() {
		Collection<V> s = new HashSet<V>();
		int maxNumber = Math.min((int) Math.pow(3, graph.vertexSet().size() / 3), 1000000);
		Collection<Collection<V>> cliques = maxCliques(s, graph.vertexSet(), s, new HashSet<Collection<V>>(maxNumber));
		return cliques;
	}

	private Collection<Collection<V>> maxCliques(Collection<V> r, Collection<V> p, Collection<V> x,
			Collection<Collection<V>> accumulator) {
		if (p.isEmpty() && x.isEmpty()) {
			accumulator.add(r);
			return accumulator;
		}
		Collection<V> px = union(p, x);
		V u = px.iterator().next();
		Collection<V> pMinusNu = setMinus(p, Neighbors.openNeighborhood(graph, u));
		if (cancelFlag)
			return null;
		for (V v : pMinusNu) {
			maxCliques(union(r, v), intersection(p, Neighbors.openNeighborhood(graph, v)),
					intersection(x, Neighbors.openNeighborhood(graph, v)), accumulator);
			p = setMinus(p, v);
			x = union(x, v);
		}
		return accumulator;
	}

	private static <V> Collection<V> intersection(Collection<V> x, Collection<V> y) {
		int xSize = x.size();
		int ySize = y.size();
		Collection<V> intersection = new HashSet<V>(Math.min(xSize, ySize));

		if (xSize < ySize) {
			for (V n : x) {
				if (y.contains(n)) {
					intersection.add(n);
				}
			}
		} else {
			for (V n : y) {
				if (x.contains(n)) {
					intersection.add(n);
				}
			}
		}
		return intersection;
	}

	/*
	 * 
	 * 
	 * UNION
	 */
	private static <V> Collection<V> union(Collection<V> x, Collection<V> y) {
		Collection<V> union = new HashSet<V>(x.size() + y.size());
		union.addAll(x);
		union.addAll(y);
		return union;
	}

	private static <V> Collection<V> union(Collection<V> set, V v) {
		Collection<V> union = new HashSet<V>(set.size() + 1);
		for (V u : set) {
			union.add(u);
		}
		union.add(v);
		return union;
	}

	/*
	 * 
	 * SET MINUS
	 */

	private static <V> Collection<V> setMinus(Collection<V> x, Collection<V> y) {
		Collection<V> minus = new HashSet<V>(x.size());
		for (V n : x) {
			if (!y.contains(n)) {
				minus.add(n);
			}
		}
		return minus;
	}

	private static <V> Collection<V> setMinus(Collection<V> set, V v) {
		Collection<V> minus = new HashSet<V>(set.size());
		for (V u : set) {
			if (!u.equals(v)) {
				minus.add(u);
			}
		}
		return minus;
	}

	@Override
	public Collection<V> execute() {
		return findExactMaximumClique(graph);
	}

}
