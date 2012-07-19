package no.uib.ii.algo.st8.util;

import no.uib.ii.algo.st8.start.Coordinate;

public class VectorSpaceBasis {
	private Coordinate origo = Coordinate.ORIGO;
	private Coordinate xVector = new Coordinate(1, 0);
	private Coordinate yVector = new Coordinate(0, 1);

	private Coordinate xVectorInverse = new Coordinate(1, 0);
	private Coordinate yVectorInverse = new Coordinate(0, 1);

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
	
	public Coordinate getOrigo(){
		return origo;
	}

	public Coordinate transform(Coordinate c) {
		if (c == null)
			return null;
		float x = c.getX() * (xVector.getX() + yVector.getX());
		float y = c.getY() * (xVector.getY() + yVector.getY());

		return origo.add(new Coordinate(x, y));
	}

	public Coordinate antiTransform(Coordinate c) {
		if (c == null)
			return null;

		float x = c.getX() * (xVectorInverse.getX() + yVectorInverse.getX());
		float y = c.getY() * (xVectorInverse.getY() + yVectorInverse.getY());

		return new Coordinate(x, y).subtract(origo);
	}

	@Override
	public String toString() {
		return "VectorSpaceBasis [origo=" + origo + ", xVector=" + xVector
				+ ", yVector=" + yVector + "]";
	}

}
