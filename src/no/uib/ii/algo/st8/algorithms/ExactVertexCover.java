package no.uib.ii.algo.st8.algorithms;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.EdgeFactory;
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

public class ExactVertexCover<V, E> extends Algorithm<V, E, Collection<V>> {

	public ExactVertexCover(SimpleGraph<V, E> graph) {
		super(graph);
	}

	@Override
	public Collection<V> execute() {
		VertexCoverListener listener = new VertexCoverListener(this, progressListener);

		if (graph == null || graphSize() == 0 || graphEdgeSize() == 0)
			return Collections.emptySet();

		KVertexCover<V, E> kvc = new KVertexCover<V, E>(graph, listener);
		return kvc.minVertexCover();
	}
}

class VertexCoverListener {
	private final Algorithm<?, ?, ?> alg;
	private final ProgressListener listener;

	public VertexCoverListener(Algorithm<?, ?, ?> alg, ProgressListener listener) {
		this.alg = alg;
		this.listener = listener;
	}

	public void progress(int k, int n) {
		listener.progress(k, n);
	}

	public boolean isCancelled() {
		return alg.cancelFlag;
	}

}

class KVertexCover<U, F> {
	private final SimpleGraph<VcVertex<U>, VcEdge<U>> internalGraph;

	private final VertexCoverListener listener;

	public KVertexCover(SimpleGraph<U, F> input, VertexCoverListener listener) {
		internalGraph = new SimpleGraph<VcVertex<U>, VcEdge<U>>(new EdgeFactory<VcVertex<U>, VcEdge<U>>() {
			public VcEdge<U> createEdge(VcVertex<U> v, VcVertex<U> u) {
				return new VcEdge<U>(v, u);
			}
		});
		this.listener = listener;
		listener.progress(0, internalGraph.vertexSet().size());
		HashMap<U, VcVertex<U>> map = new HashMap<U, VcVertex<U>>(input.vertexSet().size());
		for (U v : input.vertexSet()) {
			VcVertex<U> nv = new VcVertex<U>(v);
			internalGraph.addVertex(nv);
			map.put(v, nv);
		}

		for (F e : input.edgeSet()) {
			VcVertex<U> v1 = map.get(input.getEdgeSource(e));
			VcVertex<U> v2 = map.get(input.getEdgeTarget(e));

			internalGraph.addEdge(v1, v2, new VcEdge<U>(v1, v2));
		}
	}

	/**
	 * Gives the set of vertices that would not make sense to not keep in a VC.
	 */
	private BitSet necessaryVertices() {
		BitSet bs = new BitSet(internalGraph.vertexSet().size());
		for (VcEdge<U> e : internalGraph.edgeSet()) {
			VcVertex<U> a = e.source;
			VcVertex<U> b = e.target;
			if (!isCovered(e, bs)) {
				if (internalGraph.degreeOf(a) == 1) {
					bs.set(b.label);
				} else if (internalGraph.degreeOf(b) == 1) {
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
		for (VcVertex<U> n : internalGraph.vertexSet()) {
			if (!bs.get(n.label) && internalGraph.degreeOf(n) > k) {
				bs = add(n, bs);
			}
		}
		return bs;
	}

	private Set<U> bitSetToNodeSet(BitSet bs) {
		Set<U> vc = new HashSet<U>();
		for (VcVertex<U> n : internalGraph.vertexSet()) {
			if (bs.get(n.label)) {
				vc.add(n.vertex);
			}
		}
		return vc;
	}

	private boolean isCovered(VcEdge<U> e, BitSet vc) {
		return vc.get(e.source.label) || vc.get(e.target.label);
	}

	public Set<U> kVertexCover(int k) {
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

		if (listener.isCancelled())
			return null;

		VcEdge<U> e = firstUncovered(vc);
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

	private BitSet add(VcVertex<U> n, BitSet set) {
		BitSet r = new BitSet(internalGraph.vertexSet().size());
		// Let this be an or! A for loop is 3 times slower, and using clone() is
		// about 20% slower.
		r.or(set);
		r.set(n.label);
		return r;
	}

	private VcEdge<U> firstUncovered(BitSet vc) {
		for (VcEdge<U> e : internalGraph.edgeSet()) {
			int a = e.source.label;
			int b = e.target.label;
			if (!vc.get(a) && !vc.get(b)) {
				return e;
			}
		}
		return null;
	}

	public Set<U> minVertexCover() {
		BitSet necessary = necessaryVertices();
		for (int i = 1; i <= internalGraph.vertexSet().size(); i++) {
			if (listener.isCancelled())
				return null;

			listener.progress(i, internalGraph.vertexSet().size());

			BitSet kNecessary = necessaryVertices(necessary, i);
			BitSet kvc = kVertexCover(i - kNecessary.cardinality(), kNecessary);

			if (kvc != null) {
				return bitSetToNodeSet(kvc);
			}
		}
		throw new IllegalStateException("Could not compute vertex cover: " + internalGraph.toString());
	}

	static class VcVertex<W> {
		private W vertex;
		private int label;
		private static int GLOBAL_LABEL = 1;

		public VcVertex(W vertex) {
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

	static class VcEdge<X> {
		final VcVertex<X> source;
		final VcVertex<X> target;

		public VcEdge(VcVertex<X> v1, VcVertex<X> v2) {
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