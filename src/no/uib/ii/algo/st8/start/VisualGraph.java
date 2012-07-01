package no.uib.ii.algo.st8.start;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import no.uib.ii.algo.st8.algorithms.GraphInformation;

import org.jgrapht.alg.ConnectivityInspector;

public class VisualGraph<V, E> {
	private final UnGraph graph;
	private final Map<UnVertex, V> vertexConfiguration;
	private final Map<UnEdge, E> edgeConfiguration;

	public VisualGraph() {
		graph = new UnGraph();
		edgeConfiguration = new HashMap<UnEdge, E>();
		vertexConfiguration = new HashMap<UnVertex, V>();
	}

	public V getVertexConfiguration(UnVertex v) {
		return vertexConfiguration.get(v);
	}

	public E getEdgeConfiguration(UnEdge e) {
		return edgeConfiguration.get(e);
	}

	public UnGraph getGraph() {
		return graph;
	}

	public UnVertex createVertex(V config) {
		UnVertex v = graph.createVertex();
		vertexConfiguration.put(v, config);
		return v;
	}

	public UnVertex addVertex(V config, UnVertex vertex) {
		graph.addVertex(vertex);
		vertexConfiguration.put(vertex, config);
		return vertex;
	}

	public UnEdge createEdge(UnVertex u, UnVertex v, E edgeConfig) {
		UnEdge edge = graph.addEdge(u, v);
		edgeConfiguration.put(edge, edgeConfig);
		return edge;
	}

	public boolean removeVertex(UnVertex v) {
		vertexConfiguration.remove(v);
		boolean res = graph.removeVertex(v);
		if (res) {
			System.out.println("Successfully deleted " + v);
		} else {
			System.out.println("ERROR on delete " + v);
		}
		return res;
	}

	public boolean removeEdge(UnEdge e) {
		edgeConfiguration.remove(e);
		return graph.removeEdge(e);
	}

	public boolean removeEdge(UnVertex u, UnVertex v) {
		UnEdge e = graph.getEdge(v, u);
		if (e == null)
			return false;
		edgeConfiguration.remove(e);
		return graph.removeEdge(e);
	}

	public Set<UnVertex> getVertices() {
		return graph.vertexSet();
	}

	public Set<UnVertex> getNeighbourhood(UnVertex v) {
		return graph.getNeighbourhood(v);
	}

	public Set<UnEdge> getEdges() {
		return graph.edgeSet();
	}

	public Set<UnVertex> filter(V config, VertexFilter<V> filter) {
		Set<UnVertex> set = new HashSet<UnVertex>();
		for (Entry<UnVertex, V> entry : vertexConfiguration.entrySet()) {
			if (filter.filter(entry.getValue(), config)) {
				set.add(entry.getKey());
			}
		}
		return set;
	}

	public String graphInfo() {
		int vertexCount = getVertices().size();
		if (vertexCount == 0) {
			return "The empty graph";
		}
		int edgeCount = getEdges().size();
		if (edgeCount == 0) {
			if (vertexCount == 1) {
				return "K1";
			} else {
				return "The trivial graph";
			}
		}

		ConnectivityInspector<UnVertex, UnEdge> inspector = new ConnectivityInspector<UnVertex, UnEdge>(
				getGraph());

		boolean isConnected = inspector.isGraphConnected();
		int nc = 1;
		if (!isConnected) {
			nc = inspector.connectedSets().size();
		}
		int maxDegree = GraphInformation.maxDegree(getGraph());
		int minDegree = GraphInformation.minDegree(getGraph());
		String s = "";
		s += (isConnected ? "Connected" : "Disconnected (" + nc
				+ " components)");
		s += " graph on " + vertexCount + " vertices";
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

}
