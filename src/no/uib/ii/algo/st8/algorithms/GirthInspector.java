package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.start.UnEdge;
import no.uib.ii.algo.st8.start.UnGraph;
import no.uib.ii.algo.st8.start.UnVertex;
import no.uib.ii.algo.st8.start.VisualGraph;

public class GirthInspector {
	public static int girth(UnGraph graph) {
		int girth = graph.vertexSet().size() + 1;
		VisualGraph<PathVertex, PathEdge> g = new VisualGraph<PathVertex, PathEdge>();

		for (UnVertex v : graph.vertexSet()) {
			g.addVertex(new PathVertex(), v);
		}

		for (UnEdge e : graph.edgeSet()) {
			g.createEdge(e.getSource(), e.getTarget(), new PathEdge());
		}

		Set<UnVertex> S = new HashSet<UnVertex>();
		Set<UnVertex> R = new HashSet<UnVertex>();

		for (UnVertex v : g.getVertices()) {
			S.clear();

			R.add(v);

			g.getVertexConfiguration(v).parent = null;
			g.getVertexConfiguration(v).dist = 0;

			while (!R.isEmpty()) {
				UnVertex x = R.iterator().next();
				S.add(x);
				R.remove(x);
				for (UnVertex y : g.getNeighbourhood(x)) {
					if (y == g.getVertexConfiguration(x).parent)
						continue;
					if (!S.contains(y)) {
						g.getVertexConfiguration(y).parent = x;
						g.getVertexConfiguration(y).dist = g
								.getVertexConfiguration(x).dist + 1;
						R.add(y);

					} else {
						// girth = min (girth, dist(x) + dist(y) + 1
						int dx = g.getVertexConfiguration(x).dist;
						int dy = g.getVertexConfiguration(y).dist;

						girth = Math.min(girth, dx + dy + 1);
					}
				}
			}
		}
		if (girth > graph.vertexSet().size())
			return -1;
		return girth;
	}

	static class PathVertex {
		UnVertex parent;
		int dist;
	}

	static class PathEdge {

	}

}
