package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

/**
 * Finds bridges cut vertices
 * 
 * @author drange
 * 
 */
public class CutAndBridgeInspector {

	/**
	 * Returns a vertex v of type V which is a cut vertex in given graph or null
	 * if none exists.
	 * 
	 * @param graph
	 *            Graph to find a cut vertex
	 * @return a cut vertex or null if none exists.
	 */
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

	/**
	 * Finds the set of all cut vertices in a graph. This returns an empty set
	 * if and only if findCutVertex returns null if and only if there are no cut
	 * vertices in graph if and only if the graph is biconnected.
	 * 
	 * @param graph
	 *            input graph to locate cut vertices
	 * @return a possibly empty set of cut vertices
	 */
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

	/**
	 * Finds and return a bridge (isthmus) if and only if the graph has a
	 * bridge, returns null otherwise.
	 * 
	 * @param graph
	 *            input graph to find bridge
	 * @return a bridge or null if none exists
	 */
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

	/**
	 * Finds and returns the set of bridges (isthmuses). Returns the empty set
	 * if and only if the graph has no bridge if and only if findBridge returns
	 * null.
	 * 
	 * @param graph
	 *            input graph to find bridge
	 * @return a (possibly empty) set of bridges
	 */
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
