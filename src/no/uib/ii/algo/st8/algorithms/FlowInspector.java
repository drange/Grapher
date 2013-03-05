package no.uib.ii.algo.st8.algorithms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.SimpleGraph;

public class FlowInspector {

	public static <V, E> int findFlow(SimpleGraph<V, E> graph, V s, V t) {
		DirectedGraph<FlowVertex<V>, FlowEdge<E>> dgraph;

		FlowEdgeFactory<V, E> factory;
		factory = new FlowEdgeFactory<V, E>(graph.getEdgeFactory());

		dgraph = new DirectedWeightedMultigraph<FlowVertex<V>, FlowEdge<E>>(factory);

		Map<V, Pair<FlowVertex<V>>> map = new HashMap<V, Pair<FlowVertex<V>>>();

		for (V v : graph.vertexSet()) {
			FlowVertex<V> v1 = new FlowVertex<V>(v, 1);
			dgraph.addVertex(v1);

			FlowVertex<V> v2 = new FlowVertex<V>(v, 2);
			dgraph.addVertex(v2);

			dgraph.addEdge(v1, v2);

			map.put(v, new Pair<FlowVertex<V>>(v1, v2));
		}

		for (E e : graph.edgeSet()) {
			Pair<FlowVertex<V>> source = map.get(graph.getEdgeSource(e));
			Pair<FlowVertex<V>> target = map.get(graph.getEdgeTarget(e));

			dgraph.addEdge(source.getX2(), target.getX1());
			dgraph.addEdge(target.getX2(), source.getX1());
		}

		FlowVertex<V> source = map.get(s).getX2();
		FlowVertex<V> target = map.get(t).getX1();
		int flow = 0;
		while (flowIncreasingPath(dgraph, source, target))
			++flow;

		return flow;
	}

	private static <V, E> boolean flowIncreasingPath(DirectedGraph<FlowVertex<V>, FlowEdge<E>> dgraph, FlowVertex<V> source,
			FlowVertex<V> target) {

		Map<FlowVertex<V>, FlowVertex<V>> prev = new HashMap<FlowInspector.FlowVertex<V>, FlowInspector.FlowVertex<V>>();
		Queue<FlowVertex<V>> next = new LinkedList<FlowInspector.FlowVertex<V>>();
		next.add(source);
		prev.put(source, source);

		while (!next.isEmpty()) {
			FlowVertex<V> v = next.poll();

			if (v == target)
				break;

			for (FlowEdge<E> e : dgraph.outgoingEdgesOf(v)) {
				FlowVertex<V> n = dgraph.getEdgeTarget(e);
				if (!prev.containsKey(n)) {
					next.add(n);
					prev.put(n, v);
				}
			}
		}

		if (prev.containsKey(target)) {
			FlowVertex<V> v = prev.get(target);
			while (v != source) {
				dgraph.addEdge(v, prev.get(v));
				dgraph.removeEdge(prev.get(v), v);
				v = prev.get(v);
			}

			return true;
		} else {
			return false;
		}
	}

	public static class FlowVertex<V> {
		private final V original;
		private final int id;

		public FlowVertex(V original, int id) {
			this.original = original;
			this.id = id;
		}

		public V getOriginal() {
			return original;
		}

		public int getId() {
			return id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			result = prime * result + ((original == null) ? 0 : original.hashCode());
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
			@SuppressWarnings("rawtypes")
			FlowVertex other = (FlowVertex) obj;
			if (id != other.id)
				return false;
			if (original == null) {
				if (other.original != null)
					return false;
			} else if (!original.equals(other.original))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "FlowVertex: " + original + " (" + id + ")";
		}
	}

	public static class FlowEdge<E> {
		private final E edge;

		public FlowEdge(E edge) {
			this.edge = edge;
		}

		public E getEdge() {
			return edge;
		}
	}

	public static class FlowEdgeFactory<V, E> implements EdgeFactory<FlowVertex<V>, FlowEdge<E>> {

		EdgeFactory<V, E> factory;

		public FlowEdgeFactory(EdgeFactory<V, E> factory) {
			this.factory = factory;
		}

		public FlowEdge<E> createEdge(FlowVertex<V> arg0, FlowVertex<V> arg1) {
			return new FlowEdge<E>(factory.createEdge(arg0.original, arg1.original));
		}
	}

	public static class Pair<X> {
		private final X x1;
		private final X x2;

		public Pair(X x1, X x2) {
			this.x1 = x1;
			this.x2 = x2;
		}

		public X getX1() {
			return x1;
		}

		public X getX2() {
			return x2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((x1 == null) ? 0 : x1.hashCode());
			result = prime * result + ((x2 == null) ? 0 : x2.hashCode());
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
			@SuppressWarnings("rawtypes")
			Pair other = (Pair) obj;
			if (x1 == null) {
				if (other.x1 != null)
					return false;
			} else if (!x1.equals(other.x1))
				return false;
			if (x2 == null) {
				if (other.x2 != null)
					return false;
			} else if (!x2.equals(other.x2))
				return false;
			return true;
		}
	}
}
