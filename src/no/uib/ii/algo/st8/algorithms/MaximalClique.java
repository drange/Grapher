package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.start.UnGraph;
import no.uib.ii.algo.st8.start.UnVertex;

/**
 * I think this is a maximal clique enumerator, could possibly be improved, but
 * hardly.
 * 
 * @author pdr081
 * 
 */
public class MaximalClique {

	private final UnGraph graph;

	public MaximalClique(UnGraph graph) {
		this.graph = graph;
	}

	public static Set<UnVertex> findExactMaximumClique(UnGraph graph) {
		Set<UnVertex> maximum = null;

		MaximalClique mc = new MaximalClique(graph);
		for (Set<UnVertex> maxCliq : mc.maxCliques()) {
			if (maximum == null || maxCliq.size() > maximum.size()) {
				maximum = maxCliq;
			}
		}

		return maximum;
	}

	public Set<Set<UnVertex>> maxCliques() {
		Set<UnVertex> s = new HashSet<UnVertex>();
		int maxNumber = Math.min(
				(int) Math.pow(3, graph.vertexSet().size() / 3), 1000000);
		Set<Set<UnVertex>> cliques = maxCliques(s, graph.vertexSet(), s,
				new HashSet<Set<UnVertex>>(maxNumber));
		return cliques;
	}

	private Set<Set<UnVertex>> maxCliques(Set<UnVertex> r, Set<UnVertex> p,
			Set<UnVertex> x, Set<Set<UnVertex>> accumulator) {
		if (p.isEmpty() && x.isEmpty()) {
			accumulator.add(r);
			return accumulator;
		}
		Set<UnVertex> px = union(p, x);
		UnVertex u = px.iterator().next();
		Set<UnVertex> pMinusNu = setMinus(p, graph.getNeighbourhood(u));
		for (UnVertex v : pMinusNu) {
			maxCliques(union(r, v), intersection(p, graph.getNeighbourhood(v)),
					intersection(x, graph.getNeighbourhood(v)), accumulator);
			p = setMinus(p, v);
			x = union(x, v);
		}
		return accumulator;
	}

	private static Set<UnVertex> intersection(Set<UnVertex> x, Set<UnVertex> y) {
		int xSize = x.size();
		int ySize = y.size();
		Set<UnVertex> intersection = new HashSet<UnVertex>(Math.min(xSize,
				ySize));

		if (xSize < ySize) {
			for (UnVertex n : x) {
				if (y.contains(n)) {
					intersection.add(n);
				}
			}
		} else {
			for (UnVertex n : y) {
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
	private static Set<UnVertex> union(Set<UnVertex> x, Set<UnVertex> y) {
		Set<UnVertex> union = new HashSet<UnVertex>(x.size() + y.size());
		union.addAll(x);
		union.addAll(y);
		return union;
	}

	private static Set<UnVertex> union(Set<UnVertex> set, UnVertex v) {
		Set<UnVertex> union = new HashSet<UnVertex>(set.size() + 1);
		for (UnVertex u : set) {
			union.add(u);
		}
		union.add(v);
		return union;
	}

	/*
	 * 
	 * SET MINUS
	 */

	private static Set<UnVertex> setMinus(Set<UnVertex> x, Set<UnVertex> y) {
		Set<UnVertex> minus = new HashSet<UnVertex>(x.size());
		for (UnVertex n : x) {
			if (!y.contains(n)) {
				minus.add(n);
			}
		}
		return minus;
	}

	private static Set<UnVertex> setMinus(Set<UnVertex> set, UnVertex v) {
		Set<UnVertex> minus = new HashSet<UnVertex>(set.size());
		for (UnVertex u : set) {
			if (!u.equals(v)) {
				minus.add(u);
			}
		}
		return minus;
	}

}
