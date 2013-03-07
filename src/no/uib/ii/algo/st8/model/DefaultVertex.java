package no.uib.ii.algo.st8.model;

import java.io.Serializable;

import no.uib.ii.algo.st8.settings.Colorful;
import no.uib.ii.algo.st8.settings.Geometric;
import no.uib.ii.algo.st8.settings.Labelled;
import no.uib.ii.algo.st8.settings.Sized;
import no.uib.ii.algo.st8.util.Coordinate;
import android.graphics.Color;

public class DefaultVertex implements Colorful, Geometric, Labelled, Sized, Serializable {
	private static final long serialVersionUID = 1L;

	/** The counter for id's for DefaultVertices made */
	private volatile static int CURRENT_ID = 1;

	// TODO has always been 10!
	public final static float DEFAULT_SIZE = 15;

	private int color;
	private Coordinate coordinate;
	private String label;
	private float size;

	private final int id;

	// This is a hack that allows reset of counter on re-launch of app
	public static synchronized void resetCounter() {
		CURRENT_ID = 1;
	}

	public DefaultVertex(Coordinate coordinate) {
		this(Color.rgb(200, 0, 0), coordinate, DEFAULT_SIZE);
	}

	public DefaultVertex(int color, Coordinate coordinate, float size) {
		this(color, coordinate, size, "");
	}

	public DefaultVertex(int color, Coordinate coordinate, float size, String label) {
		this.id = CURRENT_ID++;
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

	public int getId() {
		return id;
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

	// @Override
	// public String toString() {
	// return "DefaultVertex [color=" + color + ", coordinate=" + coordinate
	// + ", label=" + label + ", size=" + size + "]";
	// }
	@Override
	public String toString() {
		return "dv" + id;
	}
}
