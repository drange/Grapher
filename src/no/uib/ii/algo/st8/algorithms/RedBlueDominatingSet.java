package no.uib.ii.algo.st8.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;
import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

public class RedBlueDominatingSet<V, E> extends Algorithm<V, E, Collection<V>> {

	private int totalDominated = 0;

	public RedBlueDominatingSet(SimpleGraph<V, E> g) {
		super(g);
	}

	@Override
	public Collection<V> execute() {
		totalDominated = 0;
		Collection<V> solution = new HashSet<V>();
		ConnectivityInspector<V, E> ci = new ConnectivityInspector<V, E>(graph);
		progress(totalDominated, graph.vertexSet().size());
		for (Set<V> vertices : ci.connectedSets()) {
			System.out.println("Connected component " + vertices);
			Collection<V> ccSol = execute(InducedSubgraph.inducedSubgraphOf(
					graph, vertices));
			totalDominated += ccSol.size();
			progress(totalDominated, graph.vertexSet().size());

			solution.addAll(ccSol);
			if (cancelFlag)
				return null;
		}

		// Testing correctness of algorithm
		HashSet<V> dominated = new HashSet<V>();
		for (V v : solution) {
			dominated.add(v);
			dominated.addAll(Neighbors.openNeighborhood(graph, v));
		}
		if (dominated.size() != graph.vertexSet().size()) {
			System.err.println("Not a dominating set: " + solution);
		} else {
			System.out.println("Is a dominating set: " + solution);
		}
		return solution;

	}

	private Collection<V> execute(SimpleGraph<V, E> g) {
		// set of vertices that are in any minimal dominating set (leaves?)
		HashSet<V> vital = new HashSet<V>();
		if (g.edgeSet().size() == 0) {
			return g.vertexSet();
		}

		// TODO LEAFS

		// else {
		// for (V lf : g.vertexSet()) {
		// if (g.degreeOf(lf) == 1) {
		// V lfn = Neighbors.getNeighbor(g, lf);
		// if (!vital.contains(lfn)) {
		// vital.add(lfn);
		// }
		// }
		// }
		// }
		//

		for (int i = 0; i <= g.vertexSet().size(); i++) {
			progress(i + totalDominated, graph.vertexSet().size());
			HashSet<V> sol = redblue(g, i, vital);

			System.out
					.println("Recursive (" + i + ") function returned " + sol);

			if (sol != null)
				return sol;
			if (cancelFlag)
				return null;
		}
		return null;
	}

	/**
	 * Returns a set
	 */
	private HashSet<V> redblue(SimpleGraph<V, E> g, int k, HashSet<V> dominated) {
		if (k < 0)
			return null;
		if (dominated.containsAll(g.vertexSet()))
			return new HashSet<V>(0);
		if (k == 0 || cancelFlag) {
			return null;
		}

		for (V v : g.vertexSet()) {
			Collection<V> Nv = Neighbors.openNeighborhood(g, v);
			Collection<V> N2v = Neighbors.closedNeighborhood(g, Nv);
			N2v.removeAll(Nv);
			HashSet<V> n1 = new HashSet<V>();
			for (V nv : Nv) {
				if (N2v.containsAll(Neighbors.openNeighborhood(g, nv))) {
					n1.add(nv);
				}
			}

			HashSet<V> n2 = new HashSet<V>();
			for (V nv : Nv) {
				if (n1.containsAll(Neighbors.openNeighborhood(g, nv))) {
					n2.add(nv);
				}
			}
			Nv.removeAll(n1);
			Nv.removeAll(n2);

			// TODO never happens?
			if (!Nv.isEmpty()) {
				System.out.println(v + " is vital");
				// v must be dominator
				dominated.addAll(n1);
				dominated.addAll(n2);
				dominated.addAll(Nv);
				g.removeVertex(v);
				HashSet<V> res = redblue(g, k - 1, dominated);
				if (res == null)
					return res;
				res.add(v);
				return res;
			}
		}

		SimpleGraph<V, E> gclone = (SimpleGraph<V, E>) g.clone();

		// branching
		for (V v : gclone.vertexSet()) {
			// put v to solution
			Collection<V> nv = Neighbors.openNeighborhood(g, v);

			HashSet<V> oldDom = new HashSet<V>();
			oldDom.addAll(dominated);
			dominated.addAll(nv);

			g.removeVertex(v);

			HashSet<V> sol1 = redblue(g, k - 1, dominated);
			if (sol1 != null) {
				sol1.add(v);
				return sol1;
			}

			dominated.clear();
			dominated.addAll(oldDom);

			g.addVertex(v);
			for (V u : nv)
				g.addEdge(v, u);

			for (V neighbor : nv) {
				Collection<V> nv2 = Neighbors.openNeighborhood(g, neighbor);
				g.removeVertex(neighbor);
				dominated.addAll(nv2);
				HashSet<V> sol2 = redblue(g, k - 1, dominated);
				if (sol2 != null) {
					sol2.add(neighbor);
					return sol2;
				}

				g.addVertex(neighbor);
				for (V nnn : nv2)
					g.addEdge(neighbor, nnn);
				dominated.clear();
				dominated.addAll(oldDom);

			}
		}
		return null;
	}
}
