package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.graph.SimpleGraph;

public class CycleInspector {
	/**
	 * Returns a set of all cycles, where each cycle is
	 * 
	 * @param graph
	 * @return
	 */
	public static <V, E> Collection<List<V>> findAllC4(SimpleGraph<V, E> graph) {
		ArrayList<V> vertices = new ArrayList<V>(graph.vertexSet().size());

		HashSet<List<V>> cycles = new HashSet<List<V>>();
		vertices.addAll(graph.vertexSet());

		if (vertices.size() < 4) {
			return cycles;
		}
		for (int i = 0; i < vertices.size(); i++) {
			V v = vertices.get(i);
			Collection<V> Nv = Neighbors.openNeighborhood(graph, v);

			for (int j = i + 1; j < vertices.size(); j++) {
				V u = vertices.get(j);
				if (graph.containsEdge(v, u)) {
					continue;
				}
				// okey, two non-adjacent vertices, testing if they are opposite
				// corners of C4.
				Collection<V> Nu = Neighbors.openNeighborhood(graph, u);
				ArrayList<V> common = new ArrayList<V>(Nu.size());
				for (V x : Nu) {
					if (Nv.contains(x))
						common.add(x);
				}
				for (int k = 0; k < common.size(); k++) {
					V x = common.get(k);

					for (int l = k + 1; l < common.size(); l++) {
						V y = common.get(l);
						if (!graph.containsEdge(x, y)) {
							ArrayList<V> cycle = new ArrayList<V>(4);
							cycle.add(v);
							cycle.add(x);
							cycle.add(u);
							cycle.add(y);
							cycles.add(cycle);
						}
					}
				}
			}
		}

		return cycles;
	}
}
