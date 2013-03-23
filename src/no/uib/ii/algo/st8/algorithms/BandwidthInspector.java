package no.uib.ii.algo.st8.algorithms;

import java.util.List;

import no.uib.ii.algo.st8.util.PermutationIterator;

import org.jgrapht.graph.SimpleGraph;

public class BandwidthInspector<V, E> extends Algorithm<V, E, Integer> {

	public BandwidthInspector(SimpleGraph<V, E> graph) {
		super(graph);
	}

	@Override
	public Integer execute() {
		PermutationIterator<V> perm = new PermutationIterator<V>(
				graph.vertexSet());

		int nfac = fac(graph.vertexSet().size());
		int counter = 0;

		int bandwidth = graph.vertexSet().size();
		while (perm.hasNext()) {
			List<V> ordering = perm.next();
			progress(counter++, nfac);
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

	private int fac(int x) {
		if (x <= 0)
			return 1;
		return x * fac(x - 1);
	}

}
