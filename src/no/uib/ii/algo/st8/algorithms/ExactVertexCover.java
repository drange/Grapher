package no.uib.ii.algo.st8.algorithms;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultVertex;

import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.VertexCovers;
import org.jgrapht.graph.SimpleGraph;

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

	public static Set<DefaultVertex> findExactVertexCover(SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		KVertexCover kvc = new KVertexCover(graph);
		return kvc.minVertexCover();
	}

}

class KVertexCover {
	private final SimpleGraph<VcVertex, VcEdge> graph;

	public KVertexCover(SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> input) {
		graph = new SimpleGraph<KVertexCover.VcVertex, KVertexCover.VcEdge>(new EdgeFactory<VcVertex, VcEdge>() {
			public VcEdge createEdge(VcVertex v, VcVertex u) {
				return new VcEdge(v, u);
			}
		});

		HashMap<DefaultVertex, VcVertex> map = new HashMap<DefaultVertex, KVertexCover.VcVertex>(input.vertexSet().size());
		for (DefaultVertex v : input.vertexSet()) {
			VcVertex nv = new VcVertex(v);
			graph.addVertex(nv);
			map.put(v, nv);
		}

		for (DefaultEdge<DefaultVertex> e : input.edgeSet()) {
			VcVertex v1 = map.get(e.getSource());
			VcVertex v2 = map.get(e.getTarget());

			graph.addEdge(v1, v2, new VcEdge(v1, v2));
		}
	}

	/**
	 * Gives the set of vertices that would not make sense to not keep in a VC.
	 */
	private BitSet necessaryVertices() {
		BitSet bs = new BitSet(graph.vertexSet().size());
		for (VcEdge e : graph.edgeSet()) {
			VcVertex a = e.source;
			VcVertex b = e.target;
			if (!isCovered(e, bs)) {
				if (graph.degreeOf(a) == 1) {
					bs.set(b.label);
				} else if (graph.degreeOf(b) == 1) {
					bs.set(a.label);
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
		for (VcVertex n : graph.vertexSet()) {
			if (!bs.get(n.label) && graph.degreeOf(n) > k) {
				bs = add(n, bs);
			}
		}
		return bs;
	}

	private Set<DefaultVertex> bitSetToNodeSet(BitSet bs) {
		Set<DefaultVertex> vc = new HashSet<DefaultVertex>();
		for (VcVertex n : graph.vertexSet()) {
			if (bs.get(n.label)) {
				vc.add(n.vertex);
			}
		}
		return vc;
	}

	private boolean isCovered(VcEdge e, BitSet vc) {
		return vc.get(e.source.label) || vc.get(e.target.label);
	}

	public Set<DefaultVertex> kVertexCover(int k) {
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

		VcEdge e = firstUncovered(vc);
		if (e == null) {
			return vc;
		}
		// try a
		BitSet kvca = kVertexCover(k - 1, add(e.source, vc));
		if (kvca != null) {
			return kvca;
		}
		// try b
		BitSet kvcb = kVertexCover(k - 1, add(e.target, vc));
		return kvcb;

	}

	private BitSet add(VcVertex n, BitSet set) {
		BitSet r = new BitSet(graph.vertexSet().size());
		// Let this be an or! A for loop is 3 times slower, and using clone() is
		// about 20% slower.
		r.or(set);
		r.set(n.label);
		return r;
	}

	private VcEdge firstUncovered(BitSet vc) {
		for (VcEdge e : graph.edgeSet()) {
			int a = e.source.label;
			int b = e.target.label;
			if (!vc.get(a) && !vc.get(b)) {
				return e;
			}
		}
		return null;
	}

	public Set<DefaultVertex> minVertexCover() {
		BitSet necessary = necessaryVertices();
		for (int i = 1; i <= graph.vertexSet().size(); i++) {
			BitSet kNecessary = necessaryVertices(necessary, i);
			BitSet kvc = kVertexCover(i - kNecessary.cardinality(), kNecessary);

			if (kvc != null) {
				return bitSetToNodeSet(kvc);
			}
		}
		throw new IllegalStateException("Could not compute vertex cover: " + graph.toString());
	}

	static class VcVertex {
		private DefaultVertex vertex;
		private int label;
		private static int GLOBAL_LABEL = 1;

		public VcVertex(DefaultVertex vertex) {
			this.vertex = vertex;
			label = GLOBAL_LABEL++;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + label;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VcVertex other = (VcVertex) obj;
			if (label != other.label)
				return false;
			return true;
		}

	}

	static class VcEdge {
		final VcVertex source;
		final VcVertex target;

		public VcEdge(VcVertex v1, VcVertex v2) {
			this.source = v1;
			this.target = v2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			result = prime * result + ((target == null) ? 0 : target.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VcEdge other = (VcEdge) obj;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}

	}
}