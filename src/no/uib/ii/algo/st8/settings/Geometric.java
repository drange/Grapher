package no.uib.ii.algo.st8.settings;

import no.uib.ii.algo.st8.util.Coordinate;

/**
 * Any class which has some 2D center can implement this class. In the case of a
 * vertex, it is the center of the vertex, in the case of an edge, it is the
 * center point of the edge, the endpoints will be located at its vertices
 * endpoints.
 * 
 * @author pgd
 * 
 */
public interface Geometric {

	/**
	 * Gets the coordinate for this object.
	 * 
	 * @return the coordinate, or null if none present
	 */
	Coordinate getCoordinate();

	/**
	 * Sets coordinate.
	 * 
	 * @param coordinate
	 *            new coordinate
	 */
	void setCoordinate(Coordinate coordinate);
}
