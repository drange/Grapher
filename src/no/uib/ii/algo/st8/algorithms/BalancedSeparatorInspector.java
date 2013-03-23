package no.uib.ii.algo.st8.algorithms;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

public class BalancedSeparatorInspector<V, E> extends
		Algorithm<V, E, Collection<V>> {

	private float threshold;

	public final static float DEFAULT_THRESHOLD = 0.333334f;

	public BalancedSeparatorInspector(SimpleGraph<V, E> graph) {
		this(graph, DEFAULT_THRESHOLD);
	}

	public BalancedSeparatorInspector(SimpleGraph<V, E> graph, float threshold) {
		super(graph);
		this.threshold = threshold;
	}

	/**
	 * Returns a 1/3-balanced separator, i.e. a set of vertices such whose
	 * removal separates the graph into two components of size a and b and b/3
	 * <= a <= 2b/3.
	 * 
	 * @return balanced separator or null if none exists
	 */
	@Override
	public Collection<V> execute() {
		return getBalancedSeparator();
	}

	/**
	 * Returns a threshold-balanced separator, i.e. a set of vertices such whose
	 * removal separates the graph into two components of size a and b and
	 * b*threshold <= a <= 2*b*threshold.
	 * 
	 * @return balanced separator or null if none exists
	 */
	public Collection<V> getBalancedSeparator() {
		if (threshold <= 0 || threshold > 0.5)
			iae("Balancing threshold must be 0 <= t <= 0.5, got " + threshold);
		Iterator<SimpleGraph<V, E>> it = InducedSubgraph
				.inducedSubgraphIteratorLargeToSmall(graph);

		int graphsize = graph.vertexSet().size();

		while (it.hasNext()) {
			SimpleGraph<V, E> separated = it.next();
			ConnectivityInspector<V, E> ci = new ConnectivityInspector<V, E>(
					separated);

			progress(graphsize - separated.vertexSet().size(), graphsize);
			if (cancelFlag)
				return null;

			if (ci.isGraphConnected())
				continue;

			List<Set<V>> sets = ci.connectedSets();
			if (sets.size() <= 1) {
				continue;
			} else if (sets.size() == 2) {
				int s0 = sets.get(0).size();
				int s1 = sets.get(1).size();
				int a = Math.min(s0, s1);
				int b = Math.max(s0, s1);
				int n = a + b;
				if (a >= threshold * n) {
					return getSeparator(graph, separated);
				}
			} else {
				// do dp
				int[] values = new int[sets.size()];
				for (int i = 0; i < values.length; i++) {
					values[i] = sets.get(i).size();
				}
				if (knapsack(threshold, values))
					return getSeparator(graph, separated);
			}

		}

		return null;
	}

	/**
	 * Simply returns the vertices in graph not in separated.
	 * 
	 * @param graph
	 *            supergraph
	 * @param separated
	 *            subgraph
	 * @return vertices in supergraph not in subgraph
	 */
	private static <V, E> Collection<V> getSeparator(SimpleGraph<V, E> graph,
			SimpleGraph<V, E> separated) {
		HashSet<V> separator = new HashSet<V>(graph.vertexSet().size());
		for (V v : graph.vertexSet()) {
			if (!separated.vertexSet().contains(v)) {
				separator.add(v);
			}
		}
		return separator;
	}

	/**
	 * Knapsack-ish. Runs in sum(values) * len(values) time.
	 * 
	 * @param threshold
	 * @param values
	 * @return true if the values can be partitioned to two sets whose sums of
	 *         values are not more than threshold apart
	 */
	private static boolean knapsack(float threshold, int[] values) {
		if (threshold <= 0 || threshold > 0.5)
			iae("Balancing threshold must be 0 <= t <= 0.5, got " + threshold);
		if (values == null || values.length == 0) {
			iae("Too few values: " + Arrays.toString(values));
		}
		Arrays.sort(values);
		if (values[0] <= 0)
			iae("Values must be positive: " + Arrays.toString(values));

		int n = 0;
		for (int i = 0; i < values.length; i++) {
			n += values[i];
		}

		// dp(i,j) = there is a set of values in values[0 ... j] that sum to i
		boolean[][] dp = new boolean[n + 1][values.length];
		for (int j = 0; j < values.length; j++) {
			dp[values[j]][j] = true;
		}

		for (int i = 1; i <= n; i++) { // start at 1, no value is 0
			for (int j = 0; j < values.length; j++) {
				if (dp[i][j])
					continue; // happens if value of j is i
				if (j == 0)
					continue;
				dp[i][j] = dp[i][j - 1];
				if (dp[i][j])
					continue;
				if (j - values[j] < 0)
					continue;
				dp[i][j] = dp[i - 1][j - values[j]];
			}
		}

		// need to see if there is a way of summing some elements to the value
		// x, where threshold*n <= x <= 2*threshold*n

		int lower = (int) Math.floor(threshold * n);
		int upper = (int) Math.ceil(2 * threshold * n);

		for (int i = lower; i < upper; i++) {
			if (dp[i][values.length - 1]) {
				return true;
			}

		}
		return false;
	}

	private static void iae(String msg) {
		throw new IllegalArgumentException(msg);
	}
}
