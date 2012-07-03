package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.DefaultEdge;
import no.uib.ii.algo.st8.DefaultVertex;

import org.jgrapht.graph.SimpleGraph;

/**
 * I think this is a maximal clique enumerator, could possibly be improved, but
 * hardly.
 * 
 * @author pdr081
 * 
 */
public class MaximalClique {

	private final SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph;

	public MaximalClique(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		this.graph = graph;
	}

	public static Set<DefaultVertex> findExactMaximumClique(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		Set<DefaultVertex> maximum = null;

		MaximalClique mc = new MaximalClique(graph);
		for (Set<DefaultVertex> maxCliq : mc.maxCliques()) {
			if (maximum == null || maxCliq.size() > maximum.size()) {
				maximum = maxCliq;
			}
		}

		return maximum;
	}

	public Set<Set<DefaultVertex>> maxCliques() {
		Set<DefaultVertex> s = new HashSet<DefaultVertex>();
		int maxNumber = Math.min(
				(int) Math.pow(3, graph.vertexSet().size() / 3), 1000000);
		Set<Set<DefaultVertex>> cliques = maxCliques(s, graph.vertexSet(), s,
				new HashSet<Set<DefaultVertex>>(maxNumber));
		return cliques;
	}

	private Set<Set<DefaultVertex>> maxCliques(Set<DefaultVertex> r,
			Set<DefaultVertex> p, Set<DefaultVertex> x,
			Set<Set<DefaultVertex>> accumulator) {
		if (p.isEmpty() && x.isEmpty()) {
			accumulator.add(r);
			return accumulator;
		}
		Set<DefaultVertex> px = union(p, x);
		DefaultVertex u = px.iterator().next();
		Set<DefaultVertex> pMinusNu = setMinus(p,
				Neighbors.neighborhood(graph, u));
		for (DefaultVertex v : pMinusNu) {
			maxCliques(union(r, v),
					intersection(p, Neighbors.neighborhood(graph, v)),
					intersection(x, Neighbors.neighborhood(graph, v)),
					accumulator);
			p = setMinus(p, v);
			x = union(x, v);
		}
		return accumulator;
	}

	private static Set<DefaultVertex> intersection(Set<DefaultVertex> x,
			Set<DefaultVertex> y) {
		int xSize = x.size();
		int ySize = y.size();
		Set<DefaultVertex> intersection = new HashSet<DefaultVertex>(Math.min(
				xSize, ySize));

		if (xSize < ySize) {
			for (DefaultVertex n : x) {
				if (y.contains(n)) {
					intersection.add(n);
				}
			}
		} else {
			for (DefaultVertex n : y) {
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
	private static Set<DefaultVertex> union(Set<DefaultVertex> x,
			Set<DefaultVertex> y) {
		Set<DefaultVertex> union = new HashSet<DefaultVertex>(x.size()
				+ y.size());
		union.addAll(x);
		union.addAll(y);
		return union;
	}

	private static Set<DefaultVertex> union(Set<DefaultVertex> set,
			DefaultVertex v) {
		Set<DefaultVertex> union = new HashSet<DefaultVertex>(set.size() + 1);
		for (DefaultVertex u : set) {
			union.add(u);
		}
		union.add(v);
		return union;
	}

	/*
	 * 
	 * SET MINUS
	 */

	private static Set<DefaultVertex> setMinus(Set<DefaultVertex> x,
			Set<DefaultVertex> y) {
		Set<DefaultVertex> minus = new HashSet<DefaultVertex>(x.size());
		for (DefaultVertex n : x) {
			if (!y.contains(n)) {
				minus.add(n);
			}
		}
		return minus;
	}

	private static Set<DefaultVertex> setMinus(Set<DefaultVertex> set,
			DefaultVertex v) {
		Set<DefaultVertex> minus = new HashSet<DefaultVertex>(set.size());
		for (DefaultVertex u : set) {
			if (!u.equals(v)) {
				minus.add(u);
			}
		}
		return minus;
	}

}
