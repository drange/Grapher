package no.uib.ii.algo.st8.model;

import java.io.Serializable;

import no.uib.ii.algo.st8.settings.Colorful;
import no.uib.ii.algo.st8.settings.Geometric;
import no.uib.ii.algo.st8.util.Coordinate;

public class DefaultEdge<V> implements Colorful, Geometric, Serializable {
	private static final long serialVersionUID = 1L;

	private EdgeStyle style;
	private int color;

	private Coordinate coordinate;

	private final V source;
	private final V target;

	public DefaultEdge(V source, V target) {
		this.source = source;
		this.target = target;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	public EdgeStyle getStyle() {
		return style;
	}

	public void setStyle(EdgeStyle style) {
		this.style = style;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isIncident(V vertex) {
		return source.equals(vertex) || target.equals(vertex);
	}

	public V getOpposite(V vertex) {
		if (source.equals(vertex))
			return target;
		if (target.equals(vertex))
			return source;
		return null;
	}

	public V getSource() {
		return source;
	}

	public V getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return "DefaultEdge: " + source + " -- " + target;
	}

}
