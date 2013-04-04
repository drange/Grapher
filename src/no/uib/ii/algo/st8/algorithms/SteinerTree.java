package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jgrapht.graph.SimpleGraph;

/**
 * @author Markus Sortland Dregi
 * 
 */
public class SteinerTree<V, E> extends Algorithm<V, E, Collection<E>> {

	private final int[] terminals;
	private final boolean[] isTerminal;
	private final int[] terminalId;

	private final int N;
	private final int T;

	private final boolean[][] touched;
	private final boolean[][] gettingComputed;
	private final int[][] solutionWeight;

	private final HashMap<Integer, V> mapIdToVertex;
	private final HashMap<V, Integer> mapVertexToId;

	@Override
	public Collection<E> execute() {
		SteinerTree<V, E> steinerTree = new SteinerTree<V, E>(graph, terminals);
		return steinerTree.getSteinerTree();
	}

	public SteinerTree(SimpleGraph<V, E> graph, int[] terminals) {
		super(graph);
		mapIdToVertex = new HashMap<Integer, V>();
		mapVertexToId = new HashMap<V, Integer>();
		int counter = 0;
		for (V v : graph.vertexSet()) {
			mapIdToVertex.put(counter, v);
			mapVertexToId.put(v, counter);
			counter++;
		}

		this.terminals = terminals;
		this.N = getMaxId() + 1;
		this.T = terminals.length;
		// assert this.T <= 30;

		this.touched = new boolean[1 << this.T][this.N];
		this.gettingComputed = new boolean[1 << this.T][this.N];
		this.solutionWeight = new int[1 << this.T][this.N];

		this.isTerminal = new boolean[this.N];
		this.terminalId = new int[this.N];
		for (int id = 0; id < terminals.length; ++id) {
			this.isTerminal[terminals[id]] = true;
			this.terminalId[terminals[id]] = id;
		}
	}

	private int compute(int s, int v) {
		V sVertex = mapIdToVertex.get(s);
		V vVertex = mapIdToVertex.get(v);

		if (this.isTerminal[v] && (s & (1 << this.terminalId[v])) != 0) {
			s ^= (1 << this.terminalId[v]);
		}

		if (s == 0) {
			return 0;
		}

		if (!this.touched[s][v]) {
			this.touched[s][v] = true;
			this.gettingComputed[s][v] = true;

			this.solutionWeight[s][v] = Integer.MAX_VALUE;
			List<Integer> subsets = generateSubsets(s);
			for (int subset : subsets) {
				if (subset != s && s != 0) {
					this.solutionWeight[s][v] = Math.min(
							this.solutionWeight[s][v], compute(subset, v)
									+ compute(s ^ subset, v));
				}
			}

			for (V vNghbr : Neighbors.openNeighborhood(graph, vVertex)) {
				int nghbr = mapVertexToId.get(vNghbr);
				this.solutionWeight[s][v] = Math.min(this.solutionWeight[s][v],
						compute(s, nghbr) + 1);
				// replace 1 with cost(v, nghbr) if weighted
			}

			this.gettingComputed[s][v] = false;
		}

		return this.gettingComputed[s][v] ? 1 << 30 : this.solutionWeight[s][v];
	}

	private HashSet<E> constructSolution(int s, int v) {
		V sVertex = mapIdToVertex.get(s);
		V vVertex = mapIdToVertex.get(v);

		for (V vNghbr : Neighbors.openNeighborhood(graph, vVertex)) {
			int nghbr = mapVertexToId.get(vNghbr);
			// replace 1 with cost for weighted graph
			if (this.solutionWeight[s][nghbr] == this.solutionWeight[s][v] + 1) {
				HashSet<E> solution = constructSolution(s, nghbr);
				solution.add(graph.getEdge(vVertex, vNghbr));
				return solution;
			}
		}

		List<Integer> subsets = generateSubsets(s);
		for (int subset : subsets) {
			if (subset != 0
					&& subset != s
					&& this.solutionWeight[s][v] == this.solutionWeight[subset][v]
							+ this.solutionWeight[s ^ subset][v]) {
				HashSet<E> solution = constructSolution(subset, v);
				solution.addAll(constructSolution(s ^ subset, v));
				return solution;
			}
		}

		throw new IllegalStateException(
				"Something wrong happened when constructing the steiner tree.");
	}

	private List<Integer> generateSubsets(int set) {
		ArrayList<Integer> subsets = new ArrayList<Integer>();

		subsets.add(0);
		for (int i = 0; (1 << i) <= set; ++i) {
			int oldSize = subsets.size();
			for (int k = 0; k < oldSize; ++k) {
				subsets.add((subsets.get(k) ^ (1 << i)));
			}
		}

		return subsets;
	}

	private int getMaxId() {
		int n = 0;
		for (V vId : graph.vertexSet()) {
			int id = mapVertexToId.get(vId);
			if (id > n) {
				n = id;
			}
		}

		return n;
	}

	private HashSet<E> getSteinerTree() {
		System.out.println("Initializing steiner tree algorithm");
		initialize();
		System.out.println("Computing streiner tree");
		compute((1 << this.T) - 1, this.terminals[0]);
		System.out.println("Returning steiner tree");
		return constructSolution((1 << this.T) - 1, this.terminals[0]);
	}

	private void initialize() {
		for (int s = 0; s < this.touched.length; ++s) {
			for (int v = 0; v < this.touched[s].length; ++v) {
				this.touched[s][v] = false;
			}
		}
	}
}