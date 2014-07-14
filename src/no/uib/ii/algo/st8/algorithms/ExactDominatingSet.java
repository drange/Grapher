package no.uib.ii.algo.st8.algorithms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;
import no.uib.ii.algo.st8.util.Neighbors;
import no.uib.ii.algo.st8.util.PowersetIterator;

import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.ConnectivityInspector;
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
public class ExactDominatingSet<V, E> extends Algorithm<V, E, Collection<V>> {

	public ExactDominatingSet(SimpleGraph<V, E> graph) {
		super(graph);
	}

	public Collection<V> execute() {
		if (graph == null || graphSize() == 0)
			return Collections.emptySet();
		if (graphEdgeSize() == 0)
			return graph.vertexSet();

		Collection<V> solution = new HashSet<V>();
		ConnectivityInspector<V, E> ci = new ConnectivityInspector<V, E>(graph);
		for (Set<V> vertices : ci.connectedSets()) {
			solution.addAll(execute(InducedSubgraph.inducedSubgraphOf(graph, vertices)));
			if (cancelFlag)
				return null;
		}
		return solution;
	}

	public Collection<V> execute(SimpleGraph<V, E> cc) {
		SimpleGraph<VertexDominated<V>, EdgeDominated> g = new SimpleGraph<VertexDominated<V>, EdgeDominated>(
				new EdgeFactory<VertexDominated<V>, EdgeDominated>() {
					public EdgeDominated createEdge(VertexDominated<V> arg0, VertexDominated<V> arg1) {
						return new EdgeDominated();
					}
				});

		HashMap<V, VertexDominated<V>> map = new HashMap<V, VertexDominated<V>>();

		for (V v : cc.vertexSet()) {
			VertexDominated<V> vd = new VertexDominated<V>(v);
			map.put(v, vd);
			g.addVertex(vd);
		}

		for (E e : cc.edgeSet()) {
			g.addEdge(map.get(cc.getEdgeSource(e)), map.get(cc.getEdgeTarget(e)), new EdgeDominated());
		}

		PowersetIterator<VertexDominated<V>> pi = new PowersetIterator<VertexDominated<V>>(g.vertexSet());
		Collection<VertexDominated<V>> domset = null;

		progress(0, cc.vertexSet().size()); // TODO doesn't work on >1 #cc

		while (pi.hasNext()) {
			Collection<VertexDominated<V>> current = pi.next();

			// if domset is a smaller dom. set, we continue searching
			if (domset != null && current.size() >= domset.size()) {
				continue;
			}

			if (cancelFlag)
				return null;

			progress(current.size(), cc.vertexSet().size());

			// test if current is a d.s.
			if (isDominatingSet(g, current)) {
				domset = current;
			}
		}

		Collection<V> res = new HashSet<V>(domset.size());
		for (VertexDominated<V> vd : domset) {
			res.add(vd.vertex);
		}

		return res;
	}

	private boolean isDominatingSet(SimpleGraph<VertexDominated<V>, EdgeDominated> graph, Collection<VertexDominated<V>> set) {
		for (VertexDominated<V> v : graph.vertexSet()) {
			v.dominated = false;
		}

		for (VertexDominated<V> dominator : set) {
			dominator.dominated = true;
			for (VertexDominated<V> other : Neighbors.openNeighborhood(graph, dominator)) {
				other.dominated = true;
			}
		}

		for (VertexDominated<V> v : graph.vertexSet()) {
			if (!v.dominated)
				return false;
		}

		return true;
	}

	static class VertexDominated<V> {
		boolean dominated = false;
		V vertex = null;

		public VertexDominated(V vertex) {
			this.vertex = vertex;
		}
	}

	static class EdgeDominated {
		boolean dominated = false;
	}

}
