package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.DefaultEdge;
import no.uib.ii.algo.st8.DefaultVertex;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

public class CutAndBridgeInspector {
	public static DefaultVertex findCutVertex(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> gc = (SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>) graph
				.clone();
		int size = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph).connectedSets().size();
		for (DefaultVertex v : graph.vertexSet()) {
			if (graph.degreeOf(v) < 2)
				continue;
			gc.removeVertex(v);
			int nsize = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
					gc).connectedSets().size();
			if (nsize > size)
				return v;
			gc.addVertex(v);
			for (DefaultVertex u : Neighbors.neighborhood(graph, v)) {
				gc.addEdge(u, v);
			}
		}
		return null;
	}

	public static Set<DefaultVertex> findAllCutVertices(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		Set<DefaultVertex> cuts = new HashSet<DefaultVertex>();
		SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> gc = (SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>) graph
				.clone();
		int size = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph).connectedSets().size();
		for (DefaultVertex v : graph.vertexSet()) {
			if (graph.degreeOf(v) < 2)
				continue;
			gc.removeVertex(v);
			int nsize = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
					gc).connectedSets().size();
			if (nsize > size)
				cuts.add(v);
			gc.addVertex(v);
			for (DefaultVertex u : Neighbors.neighborhood(graph, v)) {
				gc.addEdge(u, v);
			}
		}
		return cuts;
	}

	public static DefaultEdge<DefaultVertex> findBridge(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> gc = (SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>) graph
				.clone();
		int size = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph).connectedSets().size();
		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {

			gc.removeEdge(e);
			int nsize = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
					gc).connectedSets().size();
			if (nsize > size)
				return e;
			gc.addEdge(e.getSource(), e.getTarget());
		}
		return null;
	}

	public static Set<DefaultEdge<DefaultVertex>> findAllBridges(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		Set<DefaultEdge<DefaultVertex>> bridges = new HashSet<DefaultEdge<DefaultVertex>>();
		SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> gc = (SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>) graph
				.clone();
		int size = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph).connectedSets().size();
		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
			gc.removeEdge(e);
			int nsize = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
					gc).connectedSets().size();
			if (nsize > size)
				bridges.add(e);
			gc.addEdge(e.getSource(), e.getTarget());
		}
		return bridges;
	}

}
