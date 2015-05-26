package no.uib.ii.algo.st8;

import no.uib.ii.algo.st8.interval.Interval;
import no.uib.ii.algo.st8.util.Coordinate;

/**
 * Wrapper class for Interval used in the controller. A drawable Interval is an
 * interval, but additionally it contains coordinates for drawing and drawing
 * under movement.
 * 
 * @author Olav Wegner Eide
 *
 */
public class DrawableInterval {
	private Interval i;
	private float y;
	private float scale = 100;
	private Coordinate move = null;

	public Interval getInterval() {
		return i;
	}

	public void setMoveCoordinate(Coordinate c) {
		move = c;
	}

	public Coordinate getMoveCoordinate() {
		return move;
	}

	public void setInterval(Interval i) {
		this.i = i;
	}

	public DrawableInterval(Interval i, float y) {
		this.i = i;
		this.y = y;
	}

	/**
	 * @return coordinate for the left side of the Interval
	 */
	protected Coordinate getLeft() {
		return new Coordinate(i.getLeft() * scale, y);
	}

	/**
	 * @return coordinate for the right side of the Interval
	 */
	protected Coordinate getRight() {
		return new Coordinate(i.getRight() * scale, y);
	}

	public void setY(float y) {
		this.y = y;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DrawableInterval))
			return false;

		return this.getInterval().equals(((DrawableInterval) o).getInterval())
				&& this.y == ((DrawableInterval) o).getLeft().getY();
	}
}