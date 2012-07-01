package no.uib.ii.algo.st8.algorithms;

import no.uib.ii.algo.st8.start.UnGraph;
import no.uib.ii.algo.st8.start.UnVertex;

public class GraphInformation {

	public static int maxDegree(UnGraph graph) {
		// TODO What to do on empty graphs?

		int d = 0;

		for (UnVertex v : graph.vertexSet()) {
			d = Math.max(d, graph.degreeOf(v));
		}

		return d;
	}

	public static int minDegree(UnGraph graph) {
		// TODO What to do on empty graphs?
		int d = graph.vertexSet().size();

		for (UnVertex v : graph.vertexSet()) {
			d = Math.min(d, graph.degreeOf(v));
		}

		return d;
	}

	public static int getNumberOfConnectedComponents(UnGraph graph) {
		// TODO implement or check if already implemented
		return 0;
	}
}
