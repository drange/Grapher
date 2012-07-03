package no.uib.ii.algo.st8.algorithms;

import no.uib.ii.algo.st8.DefaultEdge;
import no.uib.ii.algo.st8.DefaultVertex;

import org.jgrapht.graph.SimpleGraph;

public class GraphInformation {

	public static int maxDegree(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		// TODO What to do on empty graphs?

		int d = 0;

		for (DefaultVertex v : graph.vertexSet()) {
			d = Math.max(d, graph.degreeOf(v));
		}

		return d;
	}

	public static int minDegree(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		// TODO What to do on empty graphs?
		int d = graph.vertexSet().size();

		for (DefaultVertex v : graph.vertexSet()) {
			d = Math.min(d, graph.degreeOf(v));
		}

		return d;
	}
}
