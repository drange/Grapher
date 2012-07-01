package no.uib.ii.algo.st8.algorithms;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.start.UnEdge;
import no.uib.ii.algo.st8.start.UnGraph;
import no.uib.ii.algo.st8.start.UnVertex;

import org.jgrapht.alg.VertexCovers;

/**
 * Warning, works only if vertices is labelled 1 .. n, could possibly fix this
 * by copying the graph, relabelling the copy while keeping a translation map
 * (bijection from old labels to new labels) and returning the pre-image of the
 * vertex-cover. *
 * 
 * @author pdr081
 * 
 */

public class ExactVertexCover extends VertexCovers {
	public static Set<UnVertex> findExactVertexCover(UnGraph g) {
		KVertexCover kvc = new KVertexCover(g);
		return kvc.minVertexCover();
	}
}

class KVertexCover {
	private final UnGraph graph;

	public KVertexCover(UnGraph g) {
		this.graph = g;
	}

	/**
	 * Gives the set of vertices that would not make sense to not keep in a VC.
	 */
	private BitSet necessaryVertices() {
		BitSet bs = new BitSet(graph.vertexSet().size());
		for (UnEdge e : graph.edgeSet()) {
			UnVertex a = e.getSource();
			UnVertex b = e.getTarget();
			if (!isCovered(e, bs)) {
				if (graph.degreeOf(a) == 1) {
					bs.set(b.getLabel());
				} else if (graph.degreeOf(b) == 1) {
					bs.set(a.getLabel());
				}
			}
		}
		return bs;
	}

	/**
	 * Gives the set of vertices that would not make sense to not keep in a VC
	 * for VC size k
	 */
	private BitSet necessaryVertices(BitSet bs, int k) {
		for (UnVertex n : graph.vertexSet()) {
			if (!bs.get(n.getLabel()) && graph.degreeOf(n) > k) {
				bs = add(n, bs);
			}
		}
		return bs;
	}

	private Set<UnVertex> bitSetToNodeSet(BitSet bs) {
		Set<UnVertex> vc = new HashSet<UnVertex>();
		for (UnVertex n : graph.vertexSet()) {
			if (bs.get(n.getLabel())) {
				vc.add(n);
			}
		}
		return vc;
	}

	private boolean isCovered(UnEdge e, BitSet vc) {
		return vc.get(e.getSource().getLabel())
				|| vc.get(e.getTarget().getLabel());
	}

	public Set<UnVertex> kVertexCover(int k) {
		BitSet bs = necessaryVertices();
		BitSet ret = kVertexCover(k, bs);
		if (ret == null) {
			return null;
		}
		return bitSetToNodeSet(ret);
	}

	private BitSet kVertexCover(int k, BitSet vc) {
		// This is slighty faster, with average 1228 vs 1265 over 5 trials.
		if (k <= 0) {
			return null;
		}

		UnEdge e = firstUncovered(vc);
		if (e == null) {
			return vc;
		}
		// try a
		BitSet kvca = kVertexCover(k - 1, add(e.getSource(), vc));
		if (kvca != null) {
			return kvca;
		}
		// try b
		BitSet kvcb = kVertexCover(k - 1, add(e.getTarget(), vc));
		return kvcb;

	}

	private BitSet add(UnVertex n, BitSet set) {
		BitSet r = new BitSet(graph.vertexSet().size());
		// Let this be an or! A for loop is 3 times slower, and using clone() is
		// about 20% slower.
		r.or(set);
		r.set(n.getLabel());
		return r;
	}

	private UnEdge firstUncovered(BitSet vc) {
		for (UnEdge e : graph.edgeSet()) {
			int a = e.getSource().getLabel();
			int b = e.getTarget().getLabel();
			if (!vc.get(a) && !vc.get(b)) {
				return e;
			}
		}
		return null;
	}

	public Set<UnVertex> minVertexCover() {
		BitSet necessary = necessaryVertices();
		for (int i = 1; i < graph.vertexSet().size(); i++) {
			BitSet kNecessary = necessaryVertices(necessary, i);
			BitSet kvc = kVertexCover(i - kNecessary.cardinality(), kNecessary);

			if (kvc != null) {
				return bitSetToNodeSet(kvc);
			}
		}
		return null;
	}

	/*
	 * This version simply prints info on time it takes to find...
	 * 
	 * public Set<UnVertex> minVertexCover() { long stop = 0; long start = 0;
	 * long milli = 1000000L; long ms = 0; BitSet necessary =
	 * necessaryVertices();
	 * 
	 * for (int i = 1; i < graph.vertexSet().size(); i++) { start =
	 * System.nanoTime(); BitSet kNecessary = necessaryVertices(necessary, i);
	 * BitSet kvc = kVertexCover(i - kNecessary.cardinality(), kNecessary); stop
	 * = System.nanoTime(); ms = ((stop - start) / milli); if (kvc != null) {
	 * System.out.println("Found vertex cover of size " + i + " [" + ms +
	 * " ms]"); return bitSetToNodeSet(kvc); } else {
	 * System.out.println("No vertex cover of size " + i + " [" + ms + " ms]");
	 * } } return null; }
	 */
}