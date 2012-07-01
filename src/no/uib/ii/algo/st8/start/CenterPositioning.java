package no.uib.ii.algo.st8.start;

import java.util.HashSet;
import java.util.Set;

public class CenterPositioning {
	public final static double PI2 = 2D * Math.PI;
	public final static double DEFAULT_ROTATE = -Math.PI / 2;
	private int points;
	private double rotate = DEFAULT_ROTATE;

	final double arcDistance;

	public static void main(String[] args) {
		int points = 5;
		if (args != null && args.length > 0) {
			points = Integer.parseInt(args[0]);
		}
		CenterPositioning cp = new CenterPositioning(points);
		System.out.println("Creating " + points + " points:");
		for (Coordinate c : cp.getPoints()) {
			System.out.println("\t" + c);
		}
	}

	public CenterPositioning(int points) {
		this(points, DEFAULT_ROTATE);
	}

	public CenterPositioning(int points, double rotate) {
		this.points = points;
		this.rotate = rotate;
		this.arcDistance = PI2 / points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	/**
	 * should be in [0,PI2], though it doesn't hurt to bring it outside, as it
	 * will be moded in again
	 */
	public void setRotate(double rotate) {
		this.rotate = rotate;
	}

	public double getRotate() {
		return rotate;
	}

	/**
	 * Returns the set of points in the star.
	 */
	public Set<Coordinate> getPoints() {
		Set<Coordinate> set = new HashSet<Coordinate>(points);
		for (int i = 0; i < points; i++) {
			set.add(getCoordinate(i));
		}
		return set;
	}

	private Coordinate getCoordinate(int n) {
		if (n >= points) {
			throw new IllegalArgumentException("There are only " + points
					+ " <= " + n + " points");
		}

		double distance = arcDistance * n;
		return new Coordinate(Math.cos(distance + rotate), Math.sin(distance
				+ rotate));
	}
}