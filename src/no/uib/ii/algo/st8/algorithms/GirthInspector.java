package no.uib.ii.algo.st8.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleGraph;

public class GirthInspector {

	/**
	 * Returns true if and only if the graph contains no cycles.
	 * 
	 * @param graph
	 *            input graph
	 * @return whether graph has a cycle
	 */
	public static <V, E> boolean isAcyclic(SimpleGraph<V, E> graph) {
		return girth(graph) < 0;
	}

	/**
	 * Returns the girth (length of shortest cycle) of the graph, or -1 if no
	 * cycles.
	 * 
	 * @param graph
	 *            input graph
	 * @return girth of graph or -1 if acyclic
	 */
	public static <V, E> int girth(SimpleGraph<V, E> graph) {
		int girth = graph.vertexSet().size() + 1;
		SimpleGraph<PathVertex<V>, DefaultEdge<PathVertex<V>>> copy = new SimpleGraph<PathVertex<V>, DefaultEdge<PathVertex<V>>>(
				new EdgeFactory<PathVertex<V>, DefaultEdge<PathVertex<V>>>() {
					public DefaultEdge<PathVertex<V>> createEdge(
							PathVertex<V> source, PathVertex<V> target) {
						return new DefaultEdge<PathVertex<V>>(source, target);
					};
				});

		HashMap<V, PathVertex<V>> map = new HashMap<V, PathVertex<V>>();

		for (V v : graph.vertexSet()) {
			PathVertex<V> pv = new PathVertex<V>(v);
			copy.addVertex(pv);
			map.put(v, pv);
		}

		for (E e : graph.edgeSet()) {
			PathVertex<V> v1 = map.get(graph.getEdgeSource(e));
			PathVertex<V> v2 = map.get(graph.getEdgeTarget(e));
			copy.addEdge(v1, v2);
		}

		Set<PathVertex<V>> S = new HashSet<PathVertex<V>>();
		Set<PathVertex<V>> R = new HashSet<PathVertex<V>>();

		for (PathVertex<V> v : copy.vertexSet()) {
			S.clear();

			R.add(v);

			v.parent = null;
			v.dist = 0;

			while (!R.isEmpty()) {
				PathVertex<V> x = R.iterator().next();
				S.add(x);
				R.remove(x);
				for (PathVertex<V> y : Neighbors.openNeighborhood(copy, x)) {
					if (y == x.parent)
						continue;
					if (!S.contains(y)) {
						y.parent = x;
						y.dist = x.dist + 1;
						R.add(y);

					} else {
						// girth = min (girth, dist(x) + dist(y) + 1
						int dx = x.dist;
						int dy = y.dist;

						girth = Math.min(girth, dx + dy + 1);
					}
				}
			}
		}
		if (girth > graph.vertexSet().size())
			return -1;
		return girth;
	}

	static class PathVertex<V> {
		PathVertex<V> parent;
		int dist;
		V original;

		public PathVertex(V original) {
			this.original = original;
		}
	}
}
