package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

/**
 * I think this is a maximal clique enumerator, could possibly be improved, but
 * hardly.
 * 
 * @author pdr081
 * 
 */
public class MaximalClique<V, E> extends Algorithm<V, E, Set<V>> {

	public MaximalClique(SimpleGraph<V, E> graph) {
		super(graph);
	}

	public Set<V> findExactMaximumClique(SimpleGraph<V, E> graph) {
		Set<V> maximum = null;
		int maxSize = graph.vertexSet().size();

		MaximalClique<V, E> mc = new MaximalClique<V, E>(graph);
		for (Set<V> maxCliq : mc.maxCliques()) {
			if (cancelFlag)
				return null;
			if (maximum == null || maxCliq.size() > maximum.size()) {
				maximum = maxCliq;
				progress(maximum.size(), maxSize);
			}
		}

		return maximum;
	}

	public Set<Set<V>> maxCliques() {
		Set<V> s = new HashSet<V>();
		int maxNumber = Math.min(
				(int) Math.pow(3, graph.vertexSet().size() / 3), 1000000);
		Set<Set<V>> cliques = maxCliques(s, graph.vertexSet(), s,
				new HashSet<Set<V>>(maxNumber));
		return cliques;
	}

	private Set<Set<V>> maxCliques(Set<V> r, Set<V> p, Set<V> x,
			Set<Set<V>> accumulator) {
		if (p.isEmpty() && x.isEmpty()) {
			accumulator.add(r);
			return accumulator;
		}
		Set<V> px = union(p, x);
		V u = px.iterator().next();
		Set<V> pMinusNu = setMinus(p, Neighbors.openNeighborhood(graph, u));
		for (V v : pMinusNu) {
			maxCliques(union(r, v),
					intersection(p, Neighbors.openNeighborhood(graph, v)),
					intersection(x, Neighbors.openNeighborhood(graph, v)),
					accumulator);
			p = setMinus(p, v);
			x = union(x, v);
		}
		return accumulator;
	}

	private static <V> Set<V> intersection(Set<V> x, Set<V> y) {
		int xSize = x.size();
		int ySize = y.size();
		Set<V> intersection = new HashSet<V>(Math.min(xSize, ySize));

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
	private static <V> Set<V> union(Set<V> x, Set<V> y) {
		Set<V> union = new HashSet<V>(x.size() + y.size());
		union.addAll(x);
		union.addAll(y);
		return union;
	}

	private static <V> Set<V> union(Set<V> set, V v) {
		Set<V> union = new HashSet<V>(set.size() + 1);
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

	private static <V> Set<V> setMinus(Set<V> x, Set<V> y) {
		Set<V> minus = new HashSet<V>(x.size());
		for (V n : x) {
			if (!y.contains(n)) {
				minus.add(n);
			}
		}
		return minus;
	}

	private static <V> Set<V> setMinus(Set<V> set, V v) {
		Set<V> minus = new HashSet<V>(set.size());
		for (V u : set) {
			if (!u.equals(v)) {
				minus.add(u);
			}
		}
		return minus;
	}

	@Override
	public Set<V> execute() {
		return findExactMaximumClique(graph);
	}

}
