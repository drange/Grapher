package no.uib.ii.algo.st8.util;

import java.io.Serializable;

public class Coordinate implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;

	private final float x;
	private final float y;

	public static final Coordinate ORIGO = new Coordinate(0, 0);
	public static final Coordinate ZERO = ORIGO;
	public static final Coordinate UNIT_X = new Coordinate(1, 0);
	public static final Coordinate UNIT_Y = new Coordinate(0, 1);

	public Coordinate(Coordinate c) {
		x = c.x;
		y = c.y;
	}

	public Coordinate(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Coordinate(double x, double y) {
		this.x = (float) x;
		this.y = (float) y;
	}

	@Override
	protected Coordinate clone() {
		return new Coordinate(this);
	}

	public Coordinate rounded() {
		return new Coordinate(Math.round(x), Math.round(y));
	}

	public Coordinate normalize() {
		float length = length();
		float nx = x / length;
		float ny = y / length;
		return new Coordinate(nx, ny);
	}

	public float angle() {
		float angle = (float) Math.atan2(y, x);
		return angle * (float) (180 / Math.PI);
	}
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y);
	}

	public Coordinate add(Coordinate c) {
		return new Coordinate(x + c.x, y + c.y);
	}

	public Coordinate multiply(float scalar) {
		return new Coordinate(x * scalar, y * scalar);
	}

	public Coordinate subtract(Coordinate c) {
		return new Coordinate(x - c.x, y - c.y);
	}

	private static float sq(float a) {
		return a * a;
	}

	public float distance(Coordinate c) {
		return (float) Math.sqrt(sq(x - c.x) + sq(y - c.y));
	}

	public Coordinate moveVector(Coordinate c) {
		float cx = c.x;
		float cy = c.y;

		return new Coordinate(cx - x, cy - y);
	}

	public Coordinate inverse() {
		return new Coordinate(-x, -y);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinate other = (Coordinate) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[x=" + x + ", y=" + y + "]";
	}

}
