package no.uib.ii.algo.st8.util;

import no.uib.ii.algo.st8.start.Coordinate;

public class VectorSpaceBasis {
	private Coordinate origo = Coordinate.ORIGO;
	private Coordinate xVector = new Coordinate(1, 0);
	private Coordinate yVector = new Coordinate(0, 1);

	public VectorSpaceBasis() {
	}

	public Coordinate moveVectorSpaceRelative(Coordinate movement) {
		origo = origo.add(movement);
		return origo;
	}

	public Coordinate setOrigo(Coordinate origo) {
		this.origo = origo;
		return origo;
	}

	public Coordinate transform(Coordinate c) {
		float x = c.getX() * (xVector.getX() + yVector.getX());
		float y = c.getY() * (xVector.getY() + yVector.getY());

		return origo.add(new Coordinate(x, y));
	}
}
