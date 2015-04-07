package no.uib.ii.algo.st8.tests;

import no.uib.ii.algo.st8.util.Neighbors;

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
	
	@Override
	public TestGraph clone(){
		TestGraph g = new TestGraph();
		for(Integer k : this.vertexSet()){
			g.addVertex(k);
		}
		for(Integer i : this.vertexSet()){
			for(Integer j : this.edgesOf(i)){
				g.addEdge(i, Neighbors.opposite(this, i, j));
			}
		}
		return g;
	}
	
	public void DotGraph(){
		System.out.println("graph myGraph{");
		for(Integer i : this.vertexSet()){
			for(Integer j : this.edgesOf(i))
				if(Neighbors.opposite(this, i, j) > i)
					System.out.println(i + "--" + Neighbors.opposite(this, i, j) + ";");
		}
		System.out.println("}");
		
	}
}
