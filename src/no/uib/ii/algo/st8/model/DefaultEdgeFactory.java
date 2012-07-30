package no.uib.ii.algo.st8.model;

import java.io.Serializable;

import org.jgrapht.EdgeFactory;

public class DefaultEdgeFactory<V> implements EdgeFactory<V, DefaultEdge<V>>,
		Serializable {
	private static final long serialVersionUID = 1L;
	
	public DefaultEdge<V> createEdge(V source, V target) {
		return new DefaultEdge<V>(source, target);
	}
}
