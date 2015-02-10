package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import no.uib.ii.algo.st8.util.Neighbors;
import no.uib.ii.algo.st8.algorithms.ChromaticNumber;
import org.jgrapht.graph.SimpleGraph;

/**
 * Class for finding an optimal colouring of a graph.
 * Description of howto is found 
 * in the comments of getcoloring method.
 * The main purpose is to return a set of sets,
 * where each set can be assigned
 * a separate colour.
 * @author H�vard Haug
 *
 * @param <V> node type
 * @param <E> edge type
 */
public class OptimalColouring<V,E> extends Algorithm<V,E,Set<Set<V>>>{

	public OptimalColouring(SimpleGraph<V, E> graph) {
		super(graph);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public Set<Set<V>> execute() {
		return getColoring(graph);
	}
	
	
	/**
	 * Given the chromatic number, 
	 * divides graph into sets so that if 
	 * each set has separate colours, 
	 * no two nodes of same colour are adjacent.
	 * @param graph
	 * @return Sets that define a colouring of the graph
	 */
	public <V,E> Set<Set<V>> getColoring(
	SimpleGraph<V,E> newgraph){
		setProgressGoal(newgraph.vertexSet().size());
		SimpleGraph<V,E> graph;
		graph = (SimpleGraph<V, E>) newgraph.clone();
		ChromaticNumber<V,E> chrom;
		chrom = new ChromaticNumber<V,E>(graph);
		int k = chrom.getChromaticNumber(graph);
		Set<Set<V>> divisions = new HashSet<Set<V>>();
		if(newgraph.vertexSet().isEmpty())
			return divisions;
		System.out.println(k);
		//if k = 0,1 all vertices should get the same colour
		if(k == 1 || k == 0){
			divisions.add(graph.vertexSet());
			return divisions;
		}
		//if k = 2, use bipartite as it is much faster
				if(k == 2){
					Set<V> half;
					half = BipartiteInspector.getBipartition(graph);
					
					Set<V> vSetCopy = new HashSet<V>();
					for(V v : graph.vertexSet())
						vSetCopy.add(v);
					vSetCopy.removeAll(half);
					Set<V> otherHalf = vSetCopy;
					divisions.add(half);
					divisions.add(otherHalf);
					return divisions;
				}
		/*
		 * For k > 2 use the following strategy: 
		 * choose a vertex v, make it universal in the graph.
		 * Let k be the old chromatic number, 
		 * k' the chromatic number of the edited graph.
		 * a) If k = k' v must have a different colour
		 *    from the rest of the graph, 
		 *    so put it into its own colour category.
		 *    Remove v and search for k-1 colouring.
		 * b) if k' > k, enumerate all vertices
		 *    not incident to v  
		 *    in the original graph as {u0, u1, u2 ... un}. 
		 *    Let Gi = (V(G), E(G) U {vu0, vu1, .. vui}).
		 *    Want to find the smallest i
		 *    such that chromatic number of 
		 *    Gi = k + 1, as v ui must have the same colour. 
		 *    Then v and ui are merged into one vertex. 
		 * Keep working with the resultant vertex
		 * until k = k' and then set all the merged vertices 
		 * to the same colour and keep working until
		 * all vertices have been removed.
		 */
		Set<V> vertexSet = new HashSet<V>(graph.vertexSet());
		Iterator<V> vIt = vertexSet.iterator();
		V v = null;
		boolean newVertex = true;
		List<V> vertices = new ArrayList<V>();
		Set<V> colourSet = new HashSet<V>();
		while(vIt.hasNext() || newVertex == false){
			increaseProgress();
			if(newVertex){
				v = vIt.next();
			}
			vertices = new ArrayList<V>();
			for(V v2 : vertexSet){
				if(!graph.containsEdge(v, v2) && !v.equals(v2))
					vertices.add(v2);
			}

			SimpleGraph<V,E> dummy;
			dummy = (SimpleGraph<V, E>) graph.clone();
			for(V v2 : vertices){
				dummy.addEdge(v, v2);
			}
			if(chrom.getChromaticNumber(dummy) == k){
				colourSet.add(v);
				divisions.add(new HashSet<V>(colourSet));
				colourSet = new HashSet<V>();
				graph.removeVertex(v);
				k--;
				newVertex = true;
			} else {
				int upper = vertices.size()-1;
				int lower = 0;
				while(upper > lower){
					int mid = (upper+lower) / 2;
					dummy = (SimpleGraph<V, E>) graph.clone();
					for(int j = 0; j<=mid; j++){
						dummy.addEdge(v, vertices.get(j));
					}
					if(chrom.getChromaticNumber(dummy) > k){
						upper = mid;
					} else{
						lower = mid+1;
					}
				}
				colourSet.add(vertices.get(upper));
				mergeVertices(graph, v, vertices.get(upper));
				newVertex = false;
			}
			vertexSet = new HashSet<V>(graph.vertexSet());
			vIt = vertexSet.iterator();
		}
		return divisions;
	}
	
	/**
	 * Merges v1 into v2
	 * @param g graph
	 * @param v1 vertex which is removed
	 * @param v2 vertex where edges of v1 is added
	 */
	private <V,E> void mergeVertices(
	SimpleGraph<V,E> g, V v, V u){
		Set<V> v1neighbours = Neighbors.openNeighborhood(g, u);
		g.removeVertex(u);
		v1neighbours.remove(v);
		for(V v1 : v1neighbours)
			g.addEdge(v, v1);

	}

	
}
