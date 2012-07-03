package no.uib.ii.algo.st8.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.DefaultEdge;
import no.uib.ii.algo.st8.DefaultVertex;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleGraph;

public class GirthInspector {
	public static int girth(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		int girth = graph.vertexSet().size() + 1;
		SimpleGraph<PathVertex, DefaultEdge<PathVertex>> copy = new SimpleGraph<PathVertex, DefaultEdge<PathVertex>>(
				new EdgeFactory<PathVertex, DefaultEdge<PathVertex>>() {
					public DefaultEdge<PathVertex> createEdge(
							PathVertex source, PathVertex target) {
						return new DefaultEdge<PathVertex>(source, target);
					};
				});

		HashMap<DefaultVertex, PathVertex> map = new HashMap<DefaultVertex, PathVertex>();

		for (DefaultVertex v : graph.vertexSet()) {
			PathVertex pv = new PathVertex(v);
			copy.addVertex(pv);
			map.put(v, pv);
		}

		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
			PathVertex v1 = map.get(e.getSource());
			PathVertex v2 = map.get(e.getTarget());
			copy.addEdge(v1, v2);
		}

		Set<PathVertex> S = new HashSet<PathVertex>();
		Set<PathVertex> R = new HashSet<PathVertex>();

		for (PathVertex v : copy.vertexSet()) {
			S.clear();

			R.add(v);

			v.parent = null;
			v.dist = 0;

			while (!R.isEmpty()) {
				PathVertex x = R.iterator().next();
				S.add(x);
				R.remove(x);
				for (PathVertex y : Neighbors.neighborhood(copy, x)) {
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

	static class PathVertex {
		PathVertex parent;
		int dist;
		DefaultVertex original;

		public PathVertex(DefaultVertex original) {
			this.original = original;
		}
	}

}
