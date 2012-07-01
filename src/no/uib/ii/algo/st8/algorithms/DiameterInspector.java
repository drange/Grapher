package no.uib.ii.algo.st8.algorithms;

import no.uib.ii.algo.st8.start.UnEdge;
import no.uib.ii.algo.st8.start.UnGraph;
import no.uib.ii.algo.st8.start.UnVertex;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;

public class DiameterInspector {

	public static int diameter(UnGraph g) {
		GraphPath<UnVertex, UnEdge> diamPath = diameterPath(g);
		if (diamPath == null)
			return -1;
		return diamPath.getEdgeList().size() + 1;
	}

	public static GraphPath<UnVertex, UnEdge> diameterPath(UnGraph g) {

		DijkstraShortestPath<UnVertex, UnEdge> d;

		ConnectivityInspector<UnVertex, UnEdge> ci = new ConnectivityInspector<UnVertex, UnEdge>(
				g);
		if (!ci.isGraphConnected())
			return null;

		GraphPath<UnVertex, UnEdge> longestPath = null;
		for (UnVertex v : g.vertexSet()) {
			for (UnVertex u : g.vertexSet()) {
				if (v != u) {
					d = new DijkstraShortestPath<UnVertex, UnEdge>(g, v, u);

					GraphPath<UnVertex, UnEdge> currentPath = d.getPath();

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
