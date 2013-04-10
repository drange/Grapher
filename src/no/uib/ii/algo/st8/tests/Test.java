package no.uib.ii.algo.st8.tests;

import no.uib.ii.algo.st8.algorithms.ExactDominatingSet;
import no.uib.ii.algo.st8.algorithms.ExactVertexCover;
import no.uib.ii.algo.st8.algorithms.MaximalClique;

public class Test {
	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			TestGraph g = GraphGenerator.clique(i);
			testMaxClique(g, i);
			testVertexCover(g, i);
			testMinDominatingSet(g, 1);
		}
	}

	public static void testMaxClique(TestGraph tg, int size) {
		assert new MaximalClique<Integer, Integer>(tg).execute().size() == size;
	}

	public static void testVertexCover(TestGraph tg, int size) {
		assert new ExactVertexCover<Integer, Integer>(tg).execute().size() == size;
	}

	public static void testMinDominatingSet(TestGraph tg, int size) {
		assert new ExactDominatingSet<Integer, Integer>(tg).execute().size() == size;
	}

}
