package no.uib.ii.algo.st8.algorithms;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.graph.SimpleGraph;

import android.annotation.SuppressLint;
import android.util.SparseArray;

/**
 * 
 * Computes treewidth in XP time, meaning in O(n^k n^2 k) time. It actually uses
 * n choose k and not n^k so in practice it's quite fast. The algorithm works as
 * follows.
 * 
 * It is a dynamic programming procedure which has as state a set B of vertices,
 * of size at most k, and a vertex v. The invariant is that B is a root bag, not
 * containing v, and v is in the unique connected component below B in G - B.
 * 
 * dp[B][v] is true if and only if in R(B,v), the reachability set on the side v
 * is in G-B, the treewidth is at most k.
 * 
 * dp[B][v] is false if |B| > k, true if B union R(B,v) has size at most k (base
 * cases).
 * 
 * dp[B][v] = dp(B-u, v) if u in B & N(u) cap R(B,v) is contained in B
 * 
 * disjunction of u in R(B,v) of conjunction of pi in cc in G-B+u and then
 * dp(B+u, pi)
 * 
 * @author paal, sigve, markus
 * 
 * @param <V>
 *            Any vertex type
 * @param <E>
 *            Any edge type
 */
public class TreewidthInspector<V, E> extends Algorithm<V, E, Integer> {
	private int k;
	private int n;

	/**
	 * The actual DP table, used in the memorization
	 */
	private HashMap<BitSet, SparseArray<Boolean>> dp;

	/**
	 * Memorized reachability: r[B][v] = R(B,v)
	 */
	private HashMap<BitSet, SparseArray<BitSet>> REACH;

	private Map<V, Integer> vertexToInt = new HashMap<V, Integer>();
	SparseArray<V> intToVertex = new SparseArray<V>();

	// private Map<BitSet, Integer> bMap = new HashMap<BitSet, Integer>();
	// private Map<Integer, BitSet> bMapInverse = new HashMap<Integer,
	// BitSet>();

	// know that n choose k grows to max on k=n/2, so if we iterate, we
	// should iterate simultaneously from k=1 to n/2 and k=n-1 to n/2

	public TreewidthInspector(SimpleGraph<V, E> graph) {
		super(graph);
		this.n = graph.vertexSet().size();

		Iterator<V> iter = graph.vertexSet().iterator();
		int id = 0;
		while (iter.hasNext()) {
			V v = iter.next();
			intToVertex.put(id, v);
			vertexToInt.put(v, id);
			++id;
		}

		/*
		 * NChooseKIterator<V> it = new NChooseKIterator<V>(graph.vertexSet(),
		 * k); int bCounter = 0; while (it.hasNext()) { Collection<V> b =
		 * it.next(); BitSet currentSet = new BitSet(n); for (V v : b) {
		 * currentSet.set(vertexToInt.get(v)); } bMap.put(currentSet, bCounter);
		 * bMapInverse.put(bCounter, currentSet); bCounter++; } dp = new
		 * Boolean[bCounter][id]; REACH = new BitSet[bCounter][id];
		 */

		dp = new HashMap<BitSet, SparseArray<Boolean>>();
		REACH = new HashMap<BitSet, SparseArray<BitSet>>();
	}

	/**
	 * Computes the reachability set R(separator, pin), defined as all vertices
	 * reachable from pin in the graph G - separator.
	 * 
	 * @param separator
	 *            a set of vertices that is (or might be) a separator
	 * @param pin
	 *            the vertex we start a search from
	 * @return vertices reachable from pin in G - separator or null if exists
	 *         vertex in R(separator, pin) with id < pin
	 */
	public BitSet reachability(BitSet separator, int pin) {
		if (REACH.get(separator) == null)
			REACH.put(separator, new SparseArray<BitSet>());

		if (REACH.get(separator).get(pin) != null)
			return REACH.get(separator).get(pin);

		HashSet<Integer> reachable = new HashSet<Integer>(n);
		reachable.add(pin);

		Queue<V> q = new LinkedList<V>();
		q.add(intToVertex.get(pin));

		while (!q.isEmpty()) {
			// current vertex
			V v = q.poll();

			Collection<V> Nv = Neighbors.openNeighborhood(graph, v);
			for (V u : Nv) {
				int uid = vertexToInt.get(u);

				// if the neighbor is in the separator, or already seen, we
				// continue
				if (separator.get(uid) || reachable.contains(uid))
					continue;
				else {
					// add vertex to reachability set, will search from this
					// later
					reachable.add(uid);
					q.add(u);
				}
			}
		}

		BitSet reach = new BitSet(n);
		for (int i : reachable)
			reach.set(i);

		for (int i : reachable)
			REACH.get(separator).put(i, reach);

		return reach;
	}

	@Override
	public Integer execute() {
		for (int i = 1; i <= n / 2 + 1; i++) {
			if (cancelFlag)
				return -2;

			// this is necessary, but why?
			dp.clear();
			REACH.clear();

			progress(i - 1, n / 2);

			this.k = i;
			if (hasTreewidth()) {
				return i - 1;
			}

			// in case tw is larger than n/2:
			dp.clear();
			REACH.clear();

			this.k = n - i + 1;
			if (!hasTreewidth()) {
				return k;
			}
		}
		return -1; // should never be reached
	}

	public boolean hasTreewidth() {
		// TODO: bitset of k ones, connected?

		BitSet b = new BitSet(n);

		// V v = CutAndBridgeInspector.findCutVertex(graph);
		// if (v != null) {
		// int cut = vertexToInt.get(v);
		// b.set(cut);
		// }

		// return hasTreewidth(b, startingVertex);
		boolean hasTreewidthAllComponents = true;
		for (V v : graph.vertexSet()) {
			if (!hasTreewidth(b, vertexToInt.get(v))) {
				hasTreewidthAllComponents = false;
				break;
			}
		}

		return hasTreewidthAllComponents;
	}

	/**
	 * Checks if the graph G[B \cup R(B,vertex)] admits a tree decomposition
	 * with B as a root bag.
	 * 
	 * @param B
	 *            BitSet containing info on separator
	 * @param vertex
	 *            the pin
	 * @return true if and only if above is true.
	 */
	@SuppressLint("UseSparseArrays")
	private boolean hasTreewidth(BitSet B, int vertex) {
		if (dp.get(B) == null) {
			dp.put(B, new SparseArray<Boolean>());
		}
		if (dp.get(B).get(vertex) != null)
			return dp.get(B).get(vertex);

		boolean returnValue = false;
		BitSet S = reachability(B, vertex);

		if (S.cardinality() + B.cardinality() <= k) {
			returnValue = true;
		} else if (B.cardinality() == k) {
			int fromIndex = 0;
			while (B.nextSetBit(fromIndex) >= 0) {
				fromIndex = B.nextSetBit(fromIndex);
				V v = intToVertex.get(fromIndex);
				boolean hasNeighborhoodInParent = true;
				for (V u : Neighbors.openNeighborhood(graph, v)) {
					if (S.get(vertexToInt.get(u))) {
						hasNeighborhoodInParent = false;
						break;
					}
				}

				if (hasNeighborhoodInParent) {
					BitSet Bu = (BitSet) B.clone();
					Bu.clear(fromIndex);
					if (hasTreewidth(Bu, vertex)) {
						returnValue = true;
						break;
					}
				}

				fromIndex++;

			}
		} else {
			for (int newBagVertex = S.nextSetBit(0); newBagVertex >= 0; newBagVertex = S
					.nextSetBit(newBagVertex + 1)) {
				BitSet Bprime = (BitSet) B.clone();
				Bprime.set(newBagVertex);

				boolean valid = true;
				for (int pin = S.nextSetBit(0); pin >= 0; pin = S
						.nextSetBit(pin + 1)) {
					if (pin == newBagVertex)
						continue;

					// valid &= hasTreewidth(Bprime, pin); altered to:
					if (!hasTreewidth(Bprime, pin)) {
						valid = false;
						break; // <-- now we stop the loop earlier
					}
				}

				if (valid) {
					returnValue = true;
					break;
				}
			}
		}

		for (int pin = S.nextSetBit(0); pin >= 0; pin = S.nextSetBit(pin + 1))
			dp.get(B).put(pin, returnValue);

		return returnValue;
	}
}