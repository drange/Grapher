package no.uib.ii.algo.st8;

import java.io.Serializable;

import no.uib.ii.algo.st8.settings.Colorful;
import no.uib.ii.algo.st8.settings.Geometric;
import no.uib.ii.algo.st8.settings.Labelled;
import no.uib.ii.algo.st8.settings.Sized;
import no.uib.ii.algo.st8.start.Coordinate;
import android.graphics.Color;

public class DefaultVertex implements Colorful, Geometric, Labelled, Sized,
		Serializable {
	private static final long serialVersionUID = 1L;

	public final static float DEFAULT_SIZE = 10;

	private int color;
	private Coordinate coordinate;
	private String label;
	private float size;

	public DefaultVertex(Coordinate coordinate) {
		this(Color.RED, coordinate, DEFAULT_SIZE);
	}

	public DefaultVertex(int color, Coordinate coordinate, float size) {
		this(color, coordinate, size, "");
	}

	public DefaultVertex(int color, Coordinate coordinate, float size,
			String label) {
		this.color = color;
		this.coordinate = coordinate;
		this.size = size;
		this.label = label;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return "DefaultVertex [color=" + color + ", coordinate=" + coordinate
				+ ", label=" + label + ", size=" + size + "]";
	}

}
