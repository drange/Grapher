package no.uib.ii.algo.st8.start;

import no.uib.ii.algo.st8.algorithms.ExactVertexCover;
import no.uib.ii.algo.st8.algorithms.MaximalClique;

public class Start {

	{
		System.out.println("Start constructor");
	}

	public static void main(String[] args) {
		System.out.println("Start.main");
		UnGraph house = new UnGraph();

		// The House Graph
		// ..........
		// .....5....
		// .../...\..
		// ..3-----4..
		// ..|.....|..
		// ..1-----2..
		// ...........

		UnVertex floor1 = house.createVertex();
		UnVertex floor2 = house.createVertex();
		UnVertex ceiling1 = house.createVertex();
		UnVertex ceiling2 = house.createVertex();
		UnVertex roof = house.createVertex();

		house.addEdge(floor1, floor2);
		house.addEdge(floor1, ceiling1);
		house.addEdge(floor2, ceiling2);
		house.addEdge(ceiling1, ceiling2);
		house.addEdge(ceiling1, roof);
		house.addEdge(ceiling2, roof);

		System.out.println("Vertex Cover:\n\t"
				+ ExactVertexCover.findExactVertexCover(house));

		System.out.println("\nMax Clique:\n\t"
				+ MaximalClique.findExactMaximumClique(house));
	}
}
