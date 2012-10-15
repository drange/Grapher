package no.uib.ii.algo.st8.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultVertex;
import no.uib.ii.algo.st8.util.PowersetIterator;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleGraph;

/**
 * 2 to the n implementation of this W[2]-hard problem.
 * 
 * Extremely slow implementation, many easy improvements, do connected
 * components individually, try sets in order of increasing size, adding
 * vertices of degree at least that size etc.
 * 
 * 
 * @author pgd
 * 
 */
public class ExactDominatingSet {

	public static Collection<DefaultVertex> exactDominatingSet(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		SimpleGraph<VertexDominated, EdgeDominated> g = new SimpleGraph<VertexDominated, EdgeDominated>(
				new EdgeFactory<VertexDominated, EdgeDominated>() {
					public EdgeDominated createEdge(VertexDominated arg0,
							VertexDominated arg1) {
						return new EdgeDominated();
					}
				});

		HashMap<DefaultVertex, VertexDominated> map = new HashMap<DefaultVertex, ExactDominatingSet.VertexDominated>();

		for (DefaultVertex v : graph.vertexSet()) {
			VertexDominated vd = new VertexDominated(v);
			map.put(v, vd);
			g.addVertex(vd);
		}

		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
			g.addEdge(map.get(e.getSource()), map.get(e.getTarget()),
					new EdgeDominated());
		}

		PowersetIterator<VertexDominated> pi = new PowersetIterator<VertexDominated>(
				g.vertexSet());
		Collection<VertexDominated> domset = null;
		while (pi.hasNext()) {
			Collection<VertexDominated> current = pi.next();

			// if domset is a smaller dom. set, we continue searching
			if (domset != null && current.size() >= domset.size()) {
				continue;
			}

			// test if current is a d.s.
			if (isDominatingSet(g, current)) {
				domset = current;
			}
		}

		Collection<DefaultVertex> res = new HashSet<DefaultVertex>(
				domset.size());
		for (VertexDominated vd : domset) {
			res.add(vd.vertex);
		}

		return res;
	}

	private static boolean isDominatingSet(
			SimpleGraph<VertexDominated, EdgeDominated> graph,
			Collection<VertexDominated> set) {
		for (VertexDominated v : graph.vertexSet()) {
			v.dominated = false;
		}

		for (VertexDominated dominator : set) {
			dominator.dominated = true;
			for (VertexDominated other : Neighbors.neighborhood(graph,
					dominator)) {
				other.dominated = true;
			}
		}

		for (VertexDominated v : graph.vertexSet()) {
			if (!v.dominated)
				return false;
		}

		return true;
	}

	static class VertexDominated {
		boolean dominated = false;
		DefaultVertex vertex = null;

		public VertexDominated(DefaultVertex vertex) {
			this.vertex = vertex;
		}
	}

	static class EdgeDominated {
		boolean dominated = false;
	}

}
