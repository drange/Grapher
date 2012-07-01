package no.uib.ii.algo.st8.algorithms;

import java.util.Set;

import no.uib.ii.algo.st8.start.UnEdge;
import no.uib.ii.algo.st8.start.UnGraph;
import no.uib.ii.algo.st8.start.UnVertex;
import no.uib.ii.algo.st8.start.VisualGraph;
import no.uib.ii.algo.st8.util.PowersetIterator;

/**
 * 2 to the n implementation of this W[2]-hard problem.
 * 
 * Extremely slow implementation, many easy improvements, do connected
 * components individually, try sets in order of increasing size, adding
 * vertices of degree at least that size etc.
 * 
 * Fomin et al have 1.53^n running in poly space or exp space if we want.
 * Implementable?
 * 
 * @author pgd
 * 
 */
public class ExactDominatingSet {

	public static Set<UnVertex> exactDominatingSet(UnGraph graph) {
		VisualGraph<VertexDominated, EdgeDominated> g = new VisualGraph<VertexDominated, EdgeDominated>();

		for (UnVertex v : graph.vertexSet()) {
			g.addVertex(new VertexDominated(), v);
		}

		for (UnEdge e : graph.edgeSet()) {
			g.createEdge(e.getSource(), e.getTarget(), new EdgeDominated());
		}

		PowersetIterator<UnVertex> pi = new PowersetIterator<UnVertex>(
				g.getVertices());
		Set<UnVertex> domset = null;
		while (pi.hasNext()) {
			Set<UnVertex> current = pi.next();

			// if domset is a smaller dom. set, we continue searching
			if (domset != null && current.size() >= domset.size()) {
				continue;
			}

			// test if current is a d.s.
			if (isDominatingSet(g, current)) {
				domset = current;
			}

		}

		return domset;
	}

	private static boolean isDominatingSet(
			VisualGraph<VertexDominated, EdgeDominated> graph, Set<UnVertex> set) {
		for (UnVertex v : graph.getVertices()) {
			graph.getVertexConfiguration(v).dominated = false;
		}

		for (UnVertex dominator : set) {
			graph.getVertexConfiguration(dominator).dominated = true;
			for (UnVertex other : graph.getNeighbourhood(dominator)) {
				graph.getVertexConfiguration(other).dominated = true;
			}
		}

		for (UnVertex v : graph.getVertices()) {
			if (!graph.getVertexConfiguration(v).dominated)
				return false;
		}

		return true;
	}

	static class VertexDominated {
		boolean dominated = false;
	}

	static class EdgeDominated {
		boolean dominated = false;
	}

}
