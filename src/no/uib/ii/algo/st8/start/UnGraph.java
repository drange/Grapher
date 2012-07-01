package no.uib.ii.algo.st8.start;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleGraph;

public class UnGraph extends SimpleGraph<UnVertex, UnEdge> {
	private static final long serialVersionUID = 1L;

	public UnGraph() {
		super(new EdgeFactory<UnVertex, UnEdge>() {
			public UnEdge createEdge(UnVertex u, UnVertex v) {
				return new UnEdge(u, v);
			}
		});
	}

	public Set<UnVertex> getNeighbourhood(UnVertex vertex) {
		Set<UnVertex> ret = new HashSet<UnVertex>();
		for (UnEdge e : edgeSet()) {
			UnVertex u = e.getOpposite(vertex);
			if (u != null)
				ret.add(u);
		}
		assert ret.size() == degreeOf(vertex);
		return ret;
	}

	public UnVertex createVertex() {
		int id = 1;
		for (UnVertex u : vertexSet()) {
			id = Math.max(u.getLabel() + 1, id);
		}
		UnVertex v = new UnVertex(id);
		addVertex(v);
		return v;
	}

	public UnEdge createEdge() {
		UnVertex u = createVertex();
		UnVertex v = createVertex();
		return addEdge(u, v);
	}
}
