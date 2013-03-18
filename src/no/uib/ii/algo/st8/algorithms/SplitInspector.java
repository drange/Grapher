package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.jgrapht.graph.SimpleGraph;

public class SplitInspector {

	/**
	 * Returns a split partition of this graph (a clique s.t. the rest is an
	 * independent set) or null if graph is not split.
	 * 
	 * @param graph
	 *            input
	 * @return the clique part if graph is split or null if graph is not split
	 */
	public <V, E> Collection<V> splitPartition(final SimpleGraph<V, E> graph) {

		ArrayList<V> degseq = new ArrayList<V>(graph.vertexSet().size());
		Collections.sort(degseq, new Comparator<V>() {
			public int compare(V lhs, V rhs) {
				return graph.degreeOf(lhs) - graph.degreeOf(rhs);
			};
		});

		return null;
	}
}
