package no.uib.ii.algo.st8.algorithms;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;

public class DiameterInspector {

	public static <V, E> int diameter(SimpleGraph<V, E> g) {

		GraphPath<V, E> diamPath = diameterPath(g);
		if (diamPath == null)
			return -1;
		return diamPath.getEdgeList().size() + 1;
	}

	public static <V, E> GraphPath<V, E> diameterPath(SimpleGraph<V, E> g) {

		DijkstraShortestPath<V, E> d;

		ConnectivityInspector<V, E> ci = new ConnectivityInspector<V, E>(g);
		if (!ci.isGraphConnected())
			return null;

		GraphPath<V, E> longestPath = null;
		for (V v : g.vertexSet()) {
			for (V u : g.vertexSet()) {
				if (v != u) {
					d = new DijkstraShortestPath<V, E>(g, v, u);

					GraphPath<V, E> currentPath = d.getPath();

					if (longestPath == null
							|| longestPath.getEdgeList().size() < currentPath
									.getEdgeList().size()) {
						longestPath = currentPath;
					}
				}
			}
		}
		return longestPath;
	}
}
