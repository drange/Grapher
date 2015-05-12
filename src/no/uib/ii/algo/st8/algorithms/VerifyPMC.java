package no.uib.ii.algo.st8.algorithms;

import java.util.*;

import no.uib.ii.algo.st8.tests.TestGraph;
import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.graph.SimpleGraph;


/**
 * Class with functions to determine if a vertex set Omega is a potential maximal clique
 * in a graph G.
 * Uses these two properties:
 * - G\Omega has no full component associated to Omega
 * - Completing each S in Delta(Omega)(minimal separators in Omega) into a clique will make
 *   G[Omega] into a complete graph.
 * @author h�vard
 *
 */
public class VerifyPMC {

	/**
	 * Tests if a vertex set is a minimal separator in G
	 * @param G Graph
	 * @param V vertex set to be tested
	 * @return true if minimal separator in G, false otherwise
	 */
	public static boolean isMinimal(ArrayList<ArrayList<Integer>> G, ArrayList<Integer> V){
		ArrayList<ArrayList<Integer>> comps = connectedComponents(G,V);
        if(comps.size() <= 1)
            return false;
        for(int i= 0; i<comps.size(); i++){
			Set<Integer> neighbours = oNeighbourhood(G,comps.get(i));
            if(neighbours.containsAll(V)) {
                return true;
            }
		}
		return false;
	}

	/**
	 * returns true if completing each minimal separator in 
	 * V into a clique makes the whole graph complete
	 * @param inG Graph
	 * @param V Separator
	 * @return True if graph becomes complete, false otherwise
	 */
	public static boolean completeGraph(ArrayList<ArrayList<Integer>> inG, ArrayList<Integer> V){
		ArrayList<ArrayList<Integer>> G = new ArrayList<ArrayList<Integer>>(inG.size());
        for(int i = 0; i<inG.size(); i++){
            G.add(new ArrayList<Integer>(inG.get(i)));
        }
		Integer[][] Vmat = new Integer[G.size()][G.size()];
		for(int i = 0; i<G.size(); i++){
			for(int j = 0; j<G.size(); j++){
				Vmat[i][j] = 0;
			}
		}
		for(int i = 0; i<G.size(); i++){
			for(Integer j : G.get(i))
				Vmat[i][j] = 1;
		}



        Integer[] comp = new Integer[G.size()];
        int currComp = 0;
        for(int i = 0; i<G.size(); i++){
            comp[i] = -1;
            if(V.contains(i))
                comp[i] = -2;
        }

        for(int i = 0; i<G.size(); i++){
            if(comp[i] != -1)
                continue;
            Queue<Integer> q = new LinkedList<Integer>();
            q.add(i);
            comp[i] = currComp;
            while(!q.isEmpty()){
                Integer k = q.poll();
                for(int j : G.get(k)){
                    if(comp[j] == -1) {
                        comp[j] = currComp;
                        q.add(j);
                    }
                }
            }
            currComp++;

        }
        ArrayList<ArrayList<Integer>> components = new ArrayList<ArrayList<Integer>>(currComp);
        for(int i = 0; i<currComp; i++){
            components.add(new ArrayList<Integer>());
        }
        for(int i = 0; i<G.size(); i++){
            if(comp[i] != -2)
                components.get(comp[i]).add(i);
        }



		/*
		 * For each component C of G\V, find the open neighbourhood of C,
		 * this will be a minimal separator in V. 
		 * Then repeat the discovery process of the open neighbourhood by a dfs,
		 * completing each node discovered to the corresponding open neighbourhood as they are discovered.
		 */
		for(int i = 0; i<components.size(); i++){
			Set<Integer> oN = oNeighbourhood(G,components.get(i));
			Set<Integer> Vset = new HashSet<Integer>(V);
			Stack<Integer> stack = new Stack<Integer>();
			Boolean[] visited = new Boolean[G.size()];
			for(int j = 0; j<G.size(); j++){
				visited[j] = false;
			}
			List<Integer> cc = components.get(i);
			for(int j = 0; j<cc.size(); j++){
				if(visited[cc.get(j)])
					continue;
				Integer k = cc.get(j);
				stack.add(k);
                visited[k] = true;
				while(!stack.isEmpty()){
					k = stack.pop();
					for(Integer l : G.get(k)){
						if(!visited[l]){
                            visited[l] = true;
							if(Vset.contains(l)){
								for(Integer ng : oN){
									Vmat[l][ng] = 1;
                                    Vmat[ng][l] = 1;
								}
							} else {
								stack.add(l);

							}
						}
					}

				}
			}
			
		}
		Boolean complete = true;
		for(int i = 0; i<V.size(); i++){
			for(int j = 0; j<V.size(); j++){
				if( V.get(i) != V.get(j) && Vmat[V.get(i)][V.get(j)] == 0)
					complete = false;
			}
		}
        if(V.size() == 1)
            complete = true;
		/*for(int i = 0; i<G.size(); i++){
			System.out.print(i + " : ");
			for(int j = 0; j<G.size(); j++){
				System.out.print(" "  + Vmat[i][j]);
			}
			System.out.println();
		}*/
		return complete;
	}

	/**
	 * lists minmal separators in v by first finding connected componenets in G\V, then taking their neighbourhoods in V.
	 * @param G graph
	 * @param V separator
	 * @return ArrayList where each entry is an arraylist of nodes which make a minimal separator
	 */
	private static ArrayList<ArrayList<Integer>> minimalSeparators(ArrayList<ArrayList<Integer>> G, ArrayList<Integer> V) {
		ArrayList<ArrayList<Integer>> comps = connectedComponents(G,V);
		ArrayList<ArrayList<Integer>> minSeps = new ArrayList<ArrayList<Integer>>();
		for(ArrayList<Integer> CC : comps){
			minSeps.add(new ArrayList<Integer>(oNeighbourhood(G, CC)));
		}
		return minSeps;
	}

	/**
	 * Main function of the class that tests if V is a pmc in G
	 * @param inG Graph
	 * @param V Vertex set to be tested
	 * @return True if V is a pmc, false otherwise
	 */
	public static boolean isPMC(SimpleGraph<Integer, Integer> inG, ArrayList<Integer> V) {
        ArrayList<ArrayList<Integer>> G = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < inG.vertexSet().size(); i++) {
            G.add(new ArrayList<Integer>());
        }
        for (Integer v : inG.vertexSet()) {
            ArrayList<Integer> Edges = new ArrayList<Integer>();
            for (Integer e : inG.edgesOf(v)) {
                Edges.add(Neighbors.opposite(inG, v, e));
            }
            G.set(v, Edges);
        }
        if (isMinimal(G, V)) {
            System.out.println("minimal");
            return false;
        }
        if (!completeGraph(G, V)) {
            System.out.println("not complete");
            return false;
        }
		return true;
	}


	/**
	 * Finds the open neighbourhood of a vertex set V in graph G in time O(m) using dfs.
	 * @param G Graph 
	 * @param V Vertex Set
	 * @return open neighbourhood of V in G
	 */
	public static Set<Integer> oNeighbourhood(ArrayList<ArrayList<Integer>> G , ArrayList<Integer> V){
		Set<Integer> Vset = new HashSet<Integer>(V);
		Set<Integer> neigh = new HashSet<Integer>();
		Stack<Integer> stack = new Stack<Integer>();
		Boolean[] visited = new Boolean[G.size()];
		for(int i = 0; i<G.size(); i++){
			visited[i] = false;
		}
		for(int i = 0; i<V.size(); i++){
			if(visited[V.get(i)])
				continue;
			Integer k = V.get(i);
			stack.add(k);
            visited[k] = true;
			while(!stack.isEmpty()){
				k = stack.pop();
				for(Integer j : G.get(k)){
					if(!visited[j]){
                        visited[j] = true;
						if(Vset.contains(j)){
							stack.add(j);
						} else {
							neigh.add(j);
						}
					}
				}

			}
		}
		return neigh;
	}

	/**
	 * Finds all the connected components of G\V
	 * @param G Graph
	 * @param V Separator
	 * @return list of connected components
	 */
	public static ArrayList<ArrayList<Integer>> connectedComponents(ArrayList<ArrayList<Integer>> G, ArrayList<Integer> V){
		Integer[] comp = new Integer[G.size()];
		int currComp = 0;
		for(int i = 0; i<G.size(); i++){
			comp[i] = -1;
			if(V.contains(i))
				comp[i] = -2;
		}

		for(int i = 0; i<G.size(); i++){
			if(comp[i] != -1)
				continue;
			Queue<Integer> q = new LinkedList<Integer>();
			q.add(i);
            comp[i] = currComp;
			while(!q.isEmpty()){
				Integer k = q.poll();
				for(int j : G.get(k)){
					if(comp[j] == -1) {
                        comp[j] = currComp;
                        q.add(j);
                    }
				}
			}
			currComp++;

		}
		ArrayList<ArrayList<Integer>> components = new ArrayList<ArrayList<Integer>>(currComp);
		for(int i = 0; i<currComp; i++){
			components.add(new ArrayList<Integer>());
		}
		for(int i = 0; i<G.size(); i++){
			if(comp[i] != -2)
				components.get(comp[i]).add(i);
		}
		return components;
	}

}
