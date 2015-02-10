package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.graph.SimpleGraph;

public class SimplicialInspector {

	public static <V, E> Collection<V> getSimplicialVertices(SimpleGraph<V, E> graph) {
		HashSet<V> simpls = new HashSet<V>();
		for (V v : graph.vertexSet()) {
			ArrayList<V> neighs = new ArrayList<V>();
			neighs.addAll(Neighbors.openNeighborhood(graph, v));

			boolean isSimplicial = true;

			for (int i = 0; i < neighs.size(); i++) {
				for (int j = i + 1; j < neighs.size(); j++) {
					if (!graph.containsEdge(neighs.get(i), neighs.get(j))) {
						isSimplicial = false;
						break;
					}
				}
				if (!isSimplicial)
					break;
			}
			if (isSimplicial)
				simpls.add(v);
		}
		return simpls;
	}

	@SuppressWarnings("unchecked")
	public static <V, E> boolean isChordal(SimpleGraph<V, E> graph) {
		SimpleGraph<V, E> gg = (SimpleGraph<V, E>) graph.clone();
		while (gg.vertexSet().size() > 3) {
			Collection<V> simpls = getSimplicialVertices(gg);
			if (simpls.size() > 1) {
				gg.removeAllVertices(simpls);
			} else {
				return false;
			}
		}
		return true;
	}
}
