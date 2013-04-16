package no.uib.ii.algo.st8.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;

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
		} else {
			for (V lf : g.vertexSet()) {
				if (g.degreeOf(lf) == 1) {
					V lfn = Neighbors.getNeighbor(g, lf);
					if (!vital.contains(lfn)) {
						vital.add(lfn);
					}
				}
			}
		}

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
	private HashSet<V> compute(SimpleGraph<V, E> g, int k, HashSet<V> dominators) {
		if (k < 0)
			return null;
		HashSet<V> dominated = new HashSet<V>();
		dominated.addAll(Neighbors.closedNeighborhood(g, dominators));
		HashSet<V> undom = new HashSet<V>();
		for (V v : g.vertexSet()) {
			if (!dominated.contains(v)) {
				undom.add(v);
			}
		}
		if (undom.size() == 0) {
			return dominators;
		}
		if (k == 0 || cancelFlag) {
			return null;
		}

		// for (V lf : g.vertexSet()) {
		// if (g.degreeOf(lf) == 1) {
		// V lfn = Neighbors.getNeighbor(g, lf);
		// if (!vital.contains(lfn)) {
		// vital.add(lfn);
		// }
		// }
		// }

		for (V v : undom) {
			// put v to solution
			dominators.add(v);
			HashSet<V> sol1 = compute(g, k - 1, dominators);
			if (sol1 != null)
				return sol1;
			dominators.remove(v);

			List<V> lst = Neighbors.orderedOpenNeighborhood(g, v, true);

			// TODO sort neighborhood by degree?
			for (V neighbor : lst) {
				dominators.add(neighbor);
				HashSet<V> sol2 = compute(g, k - 1, dominators);
				if (sol2 != null)
					return sol2;
				dominators.remove(neighbor);
			}
		}
		return null;
	}

	/**
	 * Returns a set
	 */
	private HashSet<V> redblue(SimpleGraph<V, E> g, int k, HashSet<V> dominated) {
		if (k < 0)
			return null;
		if (dominated.size() == g.vertexSet().size())
			return new HashSet<V>(0);
		if (k == 0 || cancelFlag) {
			return null;
		}

		for (V v : g.vertexSet()) {
			Collection<V> Nv = Neighbors.openNeighborhood(g, v);
			Collection<V> N2v = Neighbors.openNeighborhood(g, Nv);
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

			if (!Nv.isEmpty()) {
				// v must be dominator
				dominated.addAll(n1);
				dominated.addAll(n2);
				dominated.addAll(Nv);
				g.removeVertex(v);
				return redblue(g, k - 1, dominated);
			}
		}

		// branching
		for (V v : g.vertexSet()) {
			// put v to solution
			Collection<V> nv = Neighbors.openNeighborhood(g, v);

			HashSet<V> oldDom = new HashSet<V>();
			oldDom.addAll(dominated);
			dominated.addAll(nv);

			g.removeVertex(v);

			HashSet<V> sol1 = compute(g, k - 1, dominated);
			if (sol1 != null)
				return sol1;

			dominated.clear();
			dominated.addAll(oldDom);

			g.addVertex(v);
			for (V u : nv)
				g.addEdge(v, u);

			for (V neighbor : nv) {
				Collection<V> nv2 = Neighbors.openNeighborhood(g, neighbor);
				g.removeVertex(neighbor);
				dominated.addAll(nv2);
				HashSet<V> sol2 = compute(g, k - 1, dominated);
				if (sol2 != null)
					return sol2;

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
