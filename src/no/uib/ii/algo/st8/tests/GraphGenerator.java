package no.uib.ii.algo.st8.tests;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;
import no.uib.ii.algo.st8.util.NChooseKIterator;
import no.uib.ii.algo.st8.util.Neighbors;
import android.util.SparseIntArray;

public class GraphGenerator {
	public static final Random RANDOM = new Random();

	public static TestGraph clique(int n) {
		TestGraph c = new TestGraph();
		for (int i = 0; i < n; i++) {
			c.addVertex(i);
		}
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				c.addEdge(i, j);
			}
		}
		return c;
	}

	public static TestGraph path(int n) {
		TestGraph g = new TestGraph();
		g.addVertex(0);
		for (int i = 1; i < n; i++) {
			g.addVertex(i);
			g.addEdge(i - 1, i);
		}
		return g;
	}

	public static TestGraph cycle(int n) {
		TestGraph g = new TestGraph();
		g.addVertex(0);
		for (int i = 1; i < n; i++) {
			g.addVertex(i);
			g.addEdge(i - 1, i);
		}
		g.addEdge(n - 1, 0);
		return g;
	}

	public static TestGraph random(int n, float pEdge) {
		TestGraph g = new TestGraph();
		for (int i = 0; i < n; i++) {
			g.addVertex(i);
		}

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (RANDOM.nextFloat() <= pEdge) {
					g.addEdge(i, j);
				}
			}
		}

		return g;
	}

	
	
	public static TestGraph disjointUnion(TestGraph g1, TestGraph g2) {
		TestGraph g = new TestGraph();
		SparseIntArray m1 = new SparseIntArray();
		SparseIntArray m2 = new SparseIntArray();
		int counter = 1;
		for (Integer v : g1.vertexSet()) {
			m1.put(v, counter);
			g.addVertex(counter);
			counter++;
		}
		for (Integer v : g2.vertexSet()) {
			m2.put(v, counter);
			g.addVertex(counter);
			counter++;
		}
		for (Integer edge : g1.edgeSet()) {
			Integer s = g1.getEdgeSource(edge);
			Integer t = g1.getEdgeTarget(edge);

			g.addEdge(m1.get(s), m1.get(t));
		}
		for (Integer edge : g2.edgeSet()) {
			Integer s = g2.getEdgeSource(edge);
			Integer t = g2.getEdgeTarget(edge);

			g.addEdge(m2.get(s), m2.get(t));
		}
		return g;
	}
	
}
