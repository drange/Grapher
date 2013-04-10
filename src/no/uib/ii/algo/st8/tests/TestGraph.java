package no.uib.ii.algo.st8.tests;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleGraph;

public class TestGraph extends SimpleGraph<Integer, Integer> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -421L;

	public TestGraph() {
		super(new EdgeFactory<Integer, Integer>() {
			@Override
			public Integer createEdge(Integer arg0, Integer arg1) {
				return (int) Math.pow(2, arg0) * (int) Math.pow(3, arg1);
			};
		});
	}
}
