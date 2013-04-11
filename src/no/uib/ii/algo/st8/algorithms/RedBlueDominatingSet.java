package no.uib.ii.algo.st8.algorithms;

import java.util.Collection;
import java.util.HashSet;
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
		for (int i = 0; i <= g.vertexSet().size(); i++) {
			progress(i + totalDominated, graph.vertexSet().size());
			HashSet<V> sol = compute(g, i, new HashSet<V>());

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

		for (V v : undom) {
			// put v to solution
			dominators.add(v);
			HashSet<V> sol1 = compute(g, k - 1, dominators);
			if (sol1 != null)
				return sol1;
			dominators.remove(v);

			// TODO sort neighborhood by degree?
			for (V neighbor : Neighbors.orderedOpenNeighborhood(g, v, true)) {
				dominators.add(neighbor);
				HashSet<V> sol2 = compute(g, k - 1, dominators);
				if (sol2 != null)
					return sol2;
				dominators.remove(neighbor);
			}
		}
		return null;
	}
}
