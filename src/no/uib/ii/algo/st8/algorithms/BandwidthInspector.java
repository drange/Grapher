package no.uib.ii.algo.st8.algorithms;

import java.util.List;

import no.uib.ii.algo.st8.util.PermutationIterator;

import org.jgrapht.UndirectedGraph;

public class BandwidthInspector {
	public static <V, E> int computeBandwidth(UndirectedGraph<V, E> graph) {
		PermutationIterator<V> perm = new PermutationIterator<V>(
				graph.vertexSet());
		int bandwidth = graph.vertexSet().size();
		while (perm.hasNext()) {
			List<V> ordering = perm.next();
			int current = 0;
			for (int i = 0; i < ordering.size(); i++) {
				V v = ordering.get(i);
				for (int j = i + 1; j < ordering.size(); j++) {
					V u = ordering.get(j);
					if (graph.containsEdge(v, u)) {
						current = Math.max(current, j - i);
					}
				}
			}

			// here we could also store the current list if minimizing, to
			// obtain a witness
			bandwidth = Math.min(current, bandwidth);
		}

		return bandwidth;
	}
}
