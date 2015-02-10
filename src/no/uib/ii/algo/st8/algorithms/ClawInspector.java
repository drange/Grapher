package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.graph.SimpleGraph;

import android.util.Pair;

public class ClawInspector {

	private static <V, E> Claw<V, E> findClaw(SimpleGraph<V, E> graph) {
		ArrayList<V> vertices = new ArrayList<V>(graph.vertexSet().size());
		vertices.addAll(graph.vertexSet());
		int n = vertices.size();
		if (n < 4) {
			return null;
		}

		for (int i = 0; i < n; i++) {
			V v = vertices.get(i);
			Collection<V> Nv = Neighbors.openNeighborhood(graph, v);

			// THIS IS CENTER VERTEX, vertices[j] = u
			for (V u : Nv) {
				if (u == v)
					continue;
				// u is the center of a star in which v is an arm?
				Collection<V> Nu = Neighbors.openNeighborhood(graph, u);
				Nu.removeAll(Nv);
				Nu.remove(v);

				// in Nu - Nv
				for (V x : Nu) {
					for (V y : Nu) {
						if (x == y)
							continue;
						if (graph.containsEdge(x, y))
							continue;
						return new Claw<V, E>(graph, u, v, x, y);
					}
				}

			}
		}
		return null;
	}

	public static <V, E> Collection<E> minimalClawDeletionSet(
			SimpleGraph<V, E> graph) {

		if (getClaws(graph).getCenters().isEmpty())
			return new HashSet<E>();

		SimpleGraph<V, E> copy = graph;
		int m = copy.edgeSet().size();
		for (int i = 1; i < m; i++) {
			Collection<E> solution = minimalClawDeletionSet(copy, i);
			if (solution != null) {

				return solution;
			}
		}
		System.err
				.println("Should never come here!  Did not find solution set");
		return null;
	}

	private static <V, E> Collection<E> minimalClawDeletionSet(
			SimpleGraph<V, E> graph, int k) {

		Claw<V, E> claw = findClaw(graph);

		if (claw == null)
			return new HashSet<E>();

		if (k <= 0) {
			return null;
		}

		for (E edge : claw.getEdges()) {
			V v = graph.getEdgeSource(edge);
			V u = graph.getEdgeTarget(edge);

			graph.removeEdge(v, u);
			Collection<E> res = minimalClawDeletionSet(graph, k - 1);
			graph.addEdge(v, u, edge);
			if (res != null) {
				res.add(edge);
				return res;
			}
		}
		return null;
	}

	public static <V, E> ClawCollection<V> getClaws(SimpleGraph<V, E> graph) {
		ClawCollection<V> col = new ClawCollection<V>();

		ArrayList<V> vertices = new ArrayList<V>(graph.vertexSet().size());
		vertices.addAll(graph.vertexSet());
		int n = vertices.size();
		if (n < 4) {
			return col;
		}

		for (int i = 0; i < n; i++) {
			V v = vertices.get(i);
			Collection<V> Nv = Neighbors.openNeighborhood(graph, v);

			// THIS IS CENTER VERTEX, vertices[j] = u
			for (int j = 0; j < n; j++) {
				if (j == i)
					continue;
				V u = vertices.get(j);
				if (!Nv.contains(u))
					continue;
				// u is the center of a star in which v is an arm?
				Collection<V> Nu = Neighbors.openNeighborhood(graph, u);
				Nu.removeAll(Nv);
				Nu.remove(v);

				for (V x : Nu) {
					for (V y : Nu) {
						if (x == y)
							continue;
						if (graph.containsEdge(x, y))
							continue;
						col.addClaw(graph, u, v, x, y);
					}
				}

			}
		}
		return col;
	}

	public static class ClawCollection<U> {
		private final Collection<U> centers = new HashSet<U>();
		private final Collection<Pair<U, U>> arms = new HashSet<Pair<U, U>>();

		public boolean addClaw(SimpleGraph<U, ?> graph, U center, U v1, U v2,
				U v3) {

			centers.add(center);
			arms.add(new Pair<U, U>(center, v1));
			arms.add(new Pair<U, U>(center, v2));
			arms.add(new Pair<U, U>(center, v3));

			return true;
		}

		public Collection<U> getCenters() {
			return centers;
		}

		public Collection<Pair<U, U>> getArms() {
			return arms;
		}
	}

	public static class Claw<X, Y> {
		private final X center;
		private final X v1;
		private final X v2;
		private final X v3;
		private final SimpleGraph<X, Y> graph;
		private final Collection<Y> edges;

		public Claw(SimpleGraph<X, Y> graph, X center, X v1, X v2, X v3) {
			this.graph = graph;
			this.center = center;
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			edges = new HashSet<Y>(3);
			edges.add(graph.getEdge(center, v1));
			edges.add(graph.getEdge(center, v2));
			edges.add(graph.getEdge(center, v3));
		}

		public X getCenter() {
			return center;
		}

		public X getV1() {
			return v1;
		}

		public X getV2() {
			return v2;
		}

		public X getV3() {
			return v3;
		}

		public SimpleGraph<X, Y> getGraph() {
			return graph;
		}

		public Collection<Y> getEdges() {
			return edges;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((center == null) ? 0 : center.hashCode());
			result = prime * result + ((v1 == null) ? 0 : v1.hashCode());
			result = prime * result + ((v2 == null) ? 0 : v2.hashCode());
			result = prime * result + ((v3 == null) ? 0 : v3.hashCode());
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
			@SuppressWarnings("rawtypes")
			Claw other = (Claw) obj;
			if (center == null) {
				if (other.center != null)
					return false;
			} else if (!center.equals(other.center))
				return false;
			if (v1 == null) {
				if (other.v1 != null)
					return false;
			} else if (!v1.equals(other.v1))
				return false;
			if (v2 == null) {
				if (other.v2 != null)
					return false;
			} else if (!v2.equals(other.v2))
				return false;
			if (v3 == null) {
				if (other.v3 != null)
					return false;
			} else if (!v3.equals(other.v3))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Claw " + edges;
		}
	}
}
