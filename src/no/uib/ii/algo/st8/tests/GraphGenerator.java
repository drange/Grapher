package no.uib.ii.algo.st8.tests;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;
import no.uib.ii.algo.st8.util.NChooseKIterator;
import no.uib.ii.algo.st8.util.Neighbors;
import android.util.SparseIntArray;

public class GraphGenerator {
	public static final Random RANDOM = new Random();

	public static TestGraph clique(int n) {
		TestGraph c = new TestGraph();
		for (int i = 0; i < n; i++) {
			c.addVertex(i);
		}
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				c.addEdge(i, j);
			}
		}
		return c;
	}

	public static TestGraph path(int n) {
		TestGraph g = new TestGraph();
		g.addVertex(0);
		for (int i = 1; i < n; i++) {
			g.addVertex(i);
			g.addEdge(i - 1, i);
		}
		return g;
	}

	public static TestGraph cycle(int n) {
		TestGraph g = new TestGraph();
		g.addVertex(0);
		for (int i = 1; i < n; i++) {
			g.addVertex(i);
			g.addEdge(i - 1, i);
		}
		g.addEdge(n - 1, 0);
		return g;
	}

	public static TestGraph random(int n, float pEdge) {
		TestGraph g = new TestGraph();
		for (int i = 0; i < n; i++) {
			g.addVertex(i);
		}

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (RANDOM.nextFloat() <= pEdge) {
					g.addEdge(i, j);
				}
			}
		}

		return g;
	}

	public static TestGraph randP5Free(int n, float pEdge){
		TestGraph g = new TestGraph();
		for(int i = 0; i<n; i++){
			g.addVertex(i);
		}

		for(int i = 0; i<n; i++){
			for(int j = i+1; j<n; j++){
				if(RANDOM.nextFloat() <= pEdge){
					g.addEdge(i, j);
					if(!isP5Free(g)){
						g.removeEdge(i, j);
					}
				}

			}
		}
		return g;
	}

	/**
	 * Method to find and return an induced P5 in a given graph
	 * @param g Graph to test in
	 * @return empty list if P5-free, otherwise returns a P5.
	 */
	public static List<Integer> findP5(TestGraph g){
		List<Integer> retList = new ArrayList<Integer>();
		for(Integer v : g.vertexSet()){
			for(Integer e : g.edgesOf(v)){
				int u = Neighbors.opposite(g, v, e);
				int a = u;
				Set<Integer> Nv = Neighbors.openNeighborhood(g, v);
				Set<Integer> B = new HashSet<Integer>(g.vertexSet());
				B.removeAll(Nv);
				Set<Integer> Na = Neighbors.openNeighborhood(g, a);
				Set<Integer> Bp = new HashSet<Integer>(B);
				Bp.removeAll(Na);
				List<HashSet<Integer>> C = listComponents(g, Bp);
				Map<Integer, Integer> compMap = componentMapping(g, Bp);
				Integer c[] = new Integer[C.size()];
				for(int i = 0; i<C.size(); i++){
					c[i] = 0;
				}
				List<Integer> L = new ArrayList<Integer>();

				Set<Integer> NaB = new HashSet<Integer>(Na);
				NaB.retainAll(B);
				for(Integer b : NaB ){
					Set<Integer> NbBp = new HashSet<Integer>(Neighbors.openNeighborhood(g, b));
					NbBp.retainAll(Bp);
					for(Integer tmpc : NbBp){
						int j = compMap.get(tmpc);
						c[j]++;
						if(c[j] == 1)
							L.add(j);
					}
					List<Integer> Ldone = new ArrayList<Integer>();
					for(Integer j : L){
						if(c[j] < C.get(j).size()){
							Set<Integer> X = Neighbors.openNeighborhood(g, b);
							X.retainAll(C.get(j));
							Set<Integer> Y = new HashSet<Integer>(C.get(j));
							Y.removeAll(X);
							for(Integer x : X){
								for(Integer xEdge : g.edgesOf(x)){
									int xEdgeOpposite = Neighbors.opposite(g, x, xEdge);
									if(Y.contains(xEdgeOpposite)){
										retList.add(v);
										retList.add(a);
										retList.add(b);
										retList.add(x);
										retList.add(xEdgeOpposite);
										return retList;
									}

								}
							}
						}
						c[j] = 0;
						Ldone.add(j);
					}
					L.removeAll(Ldone);
				}
			}
		}
		return retList;
	}


	public static Map<Integer, Integer> componentMapping(TestGraph g, Set<Integer> Bp){
		Map<Integer, Integer> comp = new HashMap<Integer,Integer>();
		int currComp = 0;
		for(Integer i : Bp){
			comp.put(i, -1);
		}
		for(Integer i : Bp){
			if(comp.get(i) != -1)
				continue;
			Queue<Integer> q = new LinkedList<Integer>();
			q.add(i);
			while(!q.isEmpty()){
				Integer k = q.poll();
				comp.put(k, currComp);
				for(int e : g.edgesOf(k)){
					int kOp = Neighbors.opposite(g, k, e);
					if(Bp.contains(kOp) && comp.get(kOp) == -1)
						q.add(kOp);
				}
			}
			currComp++;
		}
		return comp;

	}

	public static List<HashSet<Integer>> listComponents(TestGraph g, Set<Integer> Bp){
		Map<Integer, Integer> comp = new HashMap<Integer,Integer>();
		int currComp = 0;
		for(Integer i : Bp){
			comp.put(i, -1);
		}
		for(Integer i : Bp){
			if(comp.get(i) != -1)
				continue;
			Queue<Integer> q = new LinkedList<Integer>();
			q.add(i);
			while(!q.isEmpty()){
				Integer k = q.poll();
				comp.put(k, currComp);
				for(int e : g.edgesOf(k)){
					int kOp = Neighbors.opposite(g, k, e);
					if(Bp.contains(kOp) && comp.get(kOp) == -1)
						q.add(kOp);
				}
			}
			currComp++;
		}
		List<HashSet<Integer>> C = new ArrayList<HashSet<Integer>>();
		for(Integer i : Bp)
			C.add(new HashSet<Integer>());
		for(Integer key : comp.keySet()){
			C.get(comp.get(key)).add(key);
		}
		return C;

	}

	public static boolean isP5Free(TestGraph g){
		List<Integer> P5 = findP5(g);
		if(P5.size() == 0)
			return true;
		else
			return false;
	}

	
	
	public static TestGraph disjointUnion(TestGraph g1, TestGraph g2) {
		TestGraph g = new TestGraph();
		SparseIntArray m1 = new SparseIntArray();
		SparseIntArray m2 = new SparseIntArray();
		int counter = 1;
		for (Integer v : g1.vertexSet()) {
			m1.put(v, counter);
			g.addVertex(counter);
			counter++;
		}
		for (Integer v : g2.vertexSet()) {
			m2.put(v, counter);
			g.addVertex(counter);
			counter++;
		}
		for (Integer edge : g1.edgeSet()) {
			Integer s = g1.getEdgeSource(edge);
			Integer t = g1.getEdgeTarget(edge);

			g.addEdge(m1.get(s), m1.get(t));
		}
		for (Integer edge : g2.edgeSet()) {
			Integer s = g2.getEdgeSource(edge);
			Integer t = g2.getEdgeTarget(edge);

			g.addEdge(m2.get(s), m2.get(t));
		}
		return g;
	}
	
}
