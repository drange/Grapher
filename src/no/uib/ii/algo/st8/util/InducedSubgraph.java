package no.uib.ii.algo.st8.util;

import java.util.Collection;
import java.util.Iterator;

import org.jgrapht.graph.SimpleGraph;

public class InducedSubgraph {
	/**
	 * Returns the induced subgraph by given vertices. Reuses the vertex
	 * objects, but makes new edges.
	 * 
	 * @param graph
	 * @param vertices
	 * @return induced graph graph[vertices]
	 */
	public static <V, E> SimpleGraph<V, E> inducedSubgraphOf(
			SimpleGraph<V, E> graph, Collection<V> vertices) {
		SimpleGraph<V, E> h = new SimpleGraph<V, E>(graph.getEdgeFactory());

		for (V v : vertices) {
			h.addVertex(v);
		}

		for (E e : graph.edgeSet()) {
			V s = graph.getEdgeSource(e);
			V t = graph.getEdgeTarget(e);
			if (h.containsVertex(s) && h.containsVertex(t)) {
				h.addEdge(s, t);
			}
		}

		return h;
	}

	public static <V, E> Iterator<SimpleGraph<V, E>> inducedSubgraphIterator(
			final SimpleGraph<V, E> graph) {
		return new Iterator<SimpleGraph<V, E>>() {
			PowersetIterator<V> subsets = new PowersetIterator<V>(
					graph.vertexSet());

			public boolean hasNext() {
				return subsets.hasNext();
			}

			public SimpleGraph<V, E> next() {
				Collection<V> vertices = subsets.next();
				return inducedSubgraphOf(graph, vertices);
			}

			public void remove() {
				throw new UnsupportedOperationException(
						"Cannot remove a set using this iterator");
			}
		};
	}

	public static <V, E> Iterator<SimpleGraph<V, E>> inducedSubgraphIteratorLargeToSmall(
			final SimpleGraph<V, E> graph) {
		return new Iterator<SimpleGraph<V, E>>() {
			Iterator<Collection<V>> subsets = new PowersetIterator.PowersetIteratorDescending<V>(
					graph.vertexSet());

			public boolean hasNext() {
				return subsets.hasNext();
			}

			public SimpleGraph<V, E> next() {
				Collection<V> vertices = subsets.next();
				return inducedSubgraphOf(graph, vertices);
			}

			public void remove() {
				throw new UnsupportedOperationException(
						"Cannot remove a set using this iterator");
			}
		};
	}
}
