package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

public class CutAndBridgeInspector {
	public static <V, E> V findCutVertex(SimpleGraph<V, E> graph) {
		@SuppressWarnings("unchecked")
		SimpleGraph<V, E> gc = (SimpleGraph<V, E>) graph.clone();
		int size = new ConnectivityInspector<V, E>(graph).connectedSets()
				.size();
		for (V v : graph.vertexSet()) {
			if (graph.degreeOf(v) < 2)
				continue;
			gc.removeVertex(v);
			int nsize = new ConnectivityInspector<V, E>(gc).connectedSets()
					.size();
			if (nsize > size)
				return v;
			gc.addVertex(v);
			for (V u : Neighbors.openNeighborhood(graph, v)) {
				gc.addEdge(u, v);
			}
		}
		return null;
	}

	public static <V, E> Set<V> findAllCutVertices(SimpleGraph<V, E> graph) {
		Set<V> cuts = new HashSet<V>();
		@SuppressWarnings("unchecked")
		SimpleGraph<V, E> gc = (SimpleGraph<V, E>) graph.clone();
		int size = new ConnectivityInspector<V, E>(graph).connectedSets()
				.size();
		for (V v : graph.vertexSet()) {
			if (graph.degreeOf(v) < 2)
				continue;
			gc.removeVertex(v);
			int nsize = new ConnectivityInspector<V, E>(gc).connectedSets()
					.size();
			if (nsize > size)
				cuts.add(v);
			gc.addVertex(v);
			for (V u : Neighbors.openNeighborhood(graph, v)) {
				gc.addEdge(u, v);
			}
		}
		return cuts;
	}

	public static <V, E> E findBridge(SimpleGraph<V, E> graph) {
		@SuppressWarnings("unchecked")
		SimpleGraph<V, E> gc = (SimpleGraph<V, E>) graph.clone();
		int size = new ConnectivityInspector<V, E>(graph).connectedSets()
				.size();
		for (E e : graph.edgeSet()) {

			gc.removeEdge(e);
			int nsize = new ConnectivityInspector<V, E>(gc).connectedSets()
					.size();
			if (nsize > size)
				return e;
			V source = graph.getEdgeSource(e);
			V target = graph.getEdgeTarget(e);
			gc.addEdge(source, target);
		}
		return null;
	}

	public static <V, E> Set<E> findAllBridges(SimpleGraph<V, E> graph) {
		Set<E> bridges = new HashSet<E>();
		@SuppressWarnings("unchecked")
		SimpleGraph<V, E> gc = (SimpleGraph<V, E>) graph.clone();
		int size = new ConnectivityInspector<V, E>(graph).connectedSets()
				.size();
		for (E e : graph.edgeSet()) {
			gc.removeEdge(e);
			int nsize = new ConnectivityInspector<V, E>(gc).connectedSets()
					.size();
			if (nsize > size)
				bridges.add(e);
			V source = graph.getEdgeSource(e);
			V target = graph.getEdgeTarget(e);
			gc.addEdge(source, target);
		}
		return bridges;
	}

}
