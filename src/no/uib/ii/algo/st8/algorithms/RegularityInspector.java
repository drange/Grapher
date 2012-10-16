package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;

import org.jgrapht.graph.SimpleGraph;

public class RegularityInspector {
	public static <V, E> boolean isRegular(SimpleGraph<V, E> g) {
		if (g == null)
			throw new NullPointerException("Input was null");
		if (g.vertexSet().size() == 0)
			return true;
		int deg = g.degreeOf(g.vertexSet().iterator().next());
		for (V v : g.vertexSet()) {
			if (g.degreeOf(v) != deg) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns -1 if non-regular, otherwise degree of all vertices.
	 * 
	 * @param g
	 * @return degree or -1 if non-regular
	 */
	public static <V, E> int getRegularity(SimpleGraph<V, E> g) {
		if (g == null)
			throw new NullPointerException("Input was null");
		if (g.vertexSet().size() == 0)
			return 0;
		int deg = g.degreeOf(g.vertexSet().iterator().next());
		for (V v : g.vertexSet()) {
			if (g.degreeOf(v) != deg) {
				return -1;
			}
		}
		return deg;
	}

	public static <V, E> Set<V> regularDeletionSet(SimpleGraph<V, E> graph) {

		Iterator<SimpleGraph<V, E>> i = InducedSubgraph
				.inducedSubgraphIteratorLargeToSmall(graph);

		while (i.hasNext()) {
			SimpleGraph<V, E> h = i.next();
			if (isRegular(h)) {
				Set<V> vertices = new HashSet<V>();
				vertices.addAll(graph.vertexSet());
				vertices.removeAll(h.vertexSet());
				return vertices;
			}
		}
		System.err.println("Should never come here!");
		return new HashSet<V>(0);

	}

	/**
	 * Returns a deletion set for obtaining a degree-regular graph. Returns null
	 * if and only if there is no induced subgraph with given regularity degree.
	 * 
	 * @param graph
	 * @param degree
	 * @return
	 */
	public static <V, E> Set<V> regularDeletionSet(SimpleGraph<V, E> graph,
			int degree) {
		Iterator<SimpleGraph<V, E>> i = InducedSubgraph
				.inducedSubgraphIteratorLargeToSmall(graph);

		while (i.hasNext()) {
			SimpleGraph<V, E> h = i.next();
			if (getRegularity(h) == degree) {
				Set<V> vertices = new HashSet<V>();
				vertices.addAll(graph.vertexSet());
				vertices.removeAll(h.vertexSet());
				return vertices;
			}
		}

		return null;
	}

	public static <V, E> StronglyRegularWitness isStronglyRegular(
			SimpleGraph<V, E> graph) {
		int n = graph.vertexSet().size();

		// C_5 is the least srg
		if (n <= 4)
			return null;

		int degree = graph.degreeOf(graph.vertexSet().iterator().next());

		ArrayList<V> vertices = new ArrayList<V>(n);
		for (V v : graph.vertexSet()) {
			vertices.add(v);
			if (graph.degreeOf(v) != degree)
				return null;
		}

		int commonAdj = -1;
		int commonNonAdj = -1;

		// need common neighbors of adjacent and non-adjacent.
		for (int i = 0; i < n; i++) {
			V v = vertices.get(i);
			Collection<V> vn = Neighbors.openNeighborhood(graph, v);
			for (int j = i + 1; j < n; j++) {
				V u = vertices.get(j);
				Collection<V> un = Neighbors.openNeighborhood(graph, u);
				if (graph.containsEdge(v, u)) {
					// lambda: adjacent common neighbors
					int counter = 0;
					for (V nabo : vn) {
						if (un.contains(nabo))
							counter++;
					}
					if (commonAdj < 0) {
						commonAdj = counter;
					} else if (commonAdj != counter) {
						return null;
					}

				} else {
					// mu: non-adjacent common neighbors
					int counter = 0;
					for (V nabo : vn) {
						if (un.contains(nabo))
							counter++;
					}
					if (commonNonAdj < 0) {
						commonNonAdj = counter;
					} else if (commonNonAdj != counter) {
						return null;
					}

				}
			}
		}

		return new StronglyRegularWitness(n, degree, commonAdj, commonNonAdj);
	}

	public static class StronglyRegularWitness {

		private final int nu;
		private final int kappa;

		private final int lambda;
		private final int mu;

		public StronglyRegularWitness(int v, int k, int l, int m) {
			this.nu = v;
			this.kappa = k;
			this.lambda = l;
			this.mu = m;
		}

		/**
		 * Order of the graph V(G) (number of vertices).
		 * 
		 * @return order of graph
		 */
		public int getNu() {
			return nu;
		}

		/**
		 * Degree of the vertices in G
		 * 
		 * @return
		 */
		public int getKappa() {
			return kappa;
		}

		/**
		 * Every two adjacent vertices have λ common neighbors.
		 * 
		 * @return lambda, number of common neighbors
		 */
		public int getLambda() {
			return lambda;
		}

		/**
		 * Every two non-adjacent vertices have μ common neighbors.
		 * 
		 * @return mu, number of common neighbors
		 */
		public int getMu() {
			return mu;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + kappa;
			result = prime * result + lambda;
			result = prime * result + mu;
			result = prime * result + nu;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StronglyRegularWitness other = (StronglyRegularWitness) obj;
			if (kappa != other.kappa)
				return false;
			if (lambda != other.lambda)
				return false;
			if (mu != other.mu)
				return false;
			if (nu != other.nu)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "srg(" + nu + ", " + kappa + ", " + lambda + ", " + mu + ")";
		}

	}

}
