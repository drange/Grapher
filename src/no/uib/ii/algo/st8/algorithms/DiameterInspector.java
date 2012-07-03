package no.uib.ii.algo.st8.algorithms;

import no.uib.ii.algo.st8.DefaultEdge;
import no.uib.ii.algo.st8.DefaultVertex;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;

public class DiameterInspector {

	public static int diameter(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> g) {

		GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> diamPath = diameterPath(g);
		if (diamPath == null)
			return -1;
		return diamPath.getEdgeList().size() + 1;
	}

	public static GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> diameterPath(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> g) {

		DijkstraShortestPath<DefaultVertex, DefaultEdge<DefaultVertex>> d;

		ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>> ci = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				g);
		if (!ci.isGraphConnected())
			return null;

		GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> longestPath = null;
		for (DefaultVertex v : g.vertexSet()) {
			for (DefaultVertex u : g.vertexSet()) {
				if (v != u) {
					d = new DijkstraShortestPath<DefaultVertex, DefaultEdge<DefaultVertex>>(
							g, v, u);

					GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> currentPath = d
							.getPath();

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
