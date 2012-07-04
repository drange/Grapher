package no.uib.ii.algo.st8.algorithms;

import no.uib.ii.algo.st8.DefaultEdge;
import no.uib.ii.algo.st8.DefaultVertex;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

public class GraphInformation {

	public static String graphInfo(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		int vertexCount = graph.vertexSet().size();
		if (vertexCount == 0) {
			return "The empty graph";
		}
		int edgeCount = graph.edgeSet().size();
		if (edgeCount == 0) {
			if (vertexCount == 1) {
				return "K1";
			} else {
				return "The trivial graph on " + vertexCount + " vertices";
			}
		}

		ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>> inspector = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph);

		boolean isConnected = inspector.isGraphConnected();
		int nc = 1;
		if (!isConnected) {
			nc = inspector.connectedSets().size();
		}

		boolean acyclic = GirthInspector.isAcyclic(graph);

		int maxDegree = maxDegree(graph);
		int minDegree = minDegree(graph);
		String s = "";
		if (isConnected) {
			s += (acyclic ? "Tree" : "Connected graph");
		} else {
			s += (acyclic ? "Forest" : "Disconnected graph");
			s += " (" + nc + " components)";
		}
		s += " on " + vertexCount + " vertices";
		s += " and " + edgeCount + " edges.";
		if (maxDegree == minDegree) {
			if (maxDegree == vertexCount - 1) {
				s += " Complete, K_" + vertexCount;
			} else {
				s += " " + maxDegree + "-regular";
			}
		} else {
			s += " Max degree " + maxDegree + ", min degree " + minDegree;
		}
		return s;
	}

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
