package no.uib.ii.algo.st8.algorithms;

import java.util.List;

import no.uib.ii.algo.st8.util.PermutationIterator;

import org.jgrapht.graph.SimpleGraph;

public class BandwidthInspector<V, E> extends Algorithm<V, E, Integer> {

	public BandwidthInspector(SimpleGraph<V, E> graph) {
		super(graph);
	}

	/**
	 * Executes BandwidthInspector
	 * 
	 * @return Bandwidth or -1 if it cannot possibly compute it
	 */
	@Override
	public Integer execute() {
		PermutationIterator<V> perm = new PermutationIterator<V>(
				graph.vertexSet());

		int nfac = PermutationIterator.factorial(graph.vertexSet().size());
		if (nfac < 0)
			return -1;
		int counter = 0;

		progress(counter, nfac);

		int bandwidth = graph.vertexSet().size();
		while (perm.hasNext()) {
			List<V> ordering = perm.next();

			counter++;
			progress(counter, nfac);

			if (cancelFlag)
				return -2;
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
