package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collections;

import org.jgrapht.EdgeFactory;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.SimpleGraph;

public class CubicExpanderGenerator {

	/**
	 * Constructs a random subcubic expander, assumes n to be positive even
	 * integer.
	 * 
	 * @param n
	 *            size of graph, must be positive even integer
	 * @return a subcubic random graph
	 */
	public static <V, E> SimpleGraph<V, E> constructCubicExpander(int n, VertexFactory<V> vertexFactory,
			EdgeFactory<V, E> edgeFactory) {
		if (n <= 1 || (n % 2 == 1)) {
			return null;
		}
		SimpleGraph<V, E> g = new SimpleGraph<V, E>(edgeFactory);
		ArrayList<V> vertices = new ArrayList<V>();
		for (int i = 0; i < n; i++) {
			V v = vertexFactory.createVertex();
			g.addVertex(v);
			vertices.add(v);
		}
		for (int i = 0; i < 3; i++) {
			Collections.shuffle(vertices);
			for (int j = 1; j < vertices.size(); j++) {
				V v = vertices.get(j - 1);
				V u = vertices.get(j);
				if (!g.containsEdge(v, u))
					g.addEdge(v, u);
			}
		}
		return g;
	}
}
