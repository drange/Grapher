package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.start.UnEdge;
import no.uib.ii.algo.st8.start.UnGraph;
import no.uib.ii.algo.st8.start.UnVertex;

import org.jgrapht.alg.ConnectivityInspector;

public class CutAndBridgeInspector {
	public static UnVertex findCutVertex(UnGraph graph) {
		UnGraph gc = graph.shallowCopy();
		int size = new ConnectivityInspector<UnVertex, UnEdge>(graph)
				.connectedSets().size();
		for (UnVertex v : graph.vertexSet()) {
			if (graph.degreeOf(v) < 2)
				continue;
			gc.removeVertex(v);
			int nsize = new ConnectivityInspector<UnVertex, UnEdge>(gc)
					.connectedSets().size();
			if (nsize > size)
				return v;
			gc.addVertex(v);
			for (UnVertex u : graph.getNeighbourhood(v)) {
				gc.addEdge(u, v);
			}
		}
		return null;
	}

	public static Set<UnVertex> findAllCutVertices(UnGraph graph) {
		Set<UnVertex> cuts = new HashSet<UnVertex>();
		UnGraph gc = graph.shallowCopy();
		int size = new ConnectivityInspector<UnVertex, UnEdge>(graph)
				.connectedSets().size();
		for (UnVertex v : graph.vertexSet()) {
			if (graph.degreeOf(v) < 2)
				continue;
			gc.removeVertex(v);
			int nsize = new ConnectivityInspector<UnVertex, UnEdge>(gc)
					.connectedSets().size();
			if (nsize > size)
				cuts.add(v);
			gc.addVertex(v);
			for (UnVertex u : graph.getNeighbourhood(v)) {
				gc.addEdge(u, v);
			}
		}
		return cuts;
	}

	public static UnEdge findBridge(UnGraph graph) {
		UnGraph gc = graph.shallowCopy();
		int size = new ConnectivityInspector<UnVertex, UnEdge>(graph)
				.connectedSets().size();
		for (UnEdge e : graph.edgeSet()) {

			gc.removeEdge(e);
			int nsize = new ConnectivityInspector<UnVertex, UnEdge>(gc)
					.connectedSets().size();
			if (nsize > size)
				return e;
			gc.addEdge(e.getSource(), e.getTarget());
		}
		return null;
	}

	public static Set<UnEdge> findAllBridges(UnGraph graph) {
		Set<UnEdge> bridges = new HashSet<UnEdge>();
		UnGraph gc = graph.shallowCopy();
		int size = new ConnectivityInspector<UnVertex, UnEdge>(graph)
				.connectedSets().size();
		for (UnEdge e : graph.edgeSet()) {
			gc.removeEdge(e);
			int nsize = new ConnectivityInspector<UnVertex, UnEdge>(gc)
					.connectedSets().size();
			if (nsize > size)
				bridges.add(e);
			gc.addEdge(e.getSource(), e.getTarget());
		}
		return bridges;
	}

}
