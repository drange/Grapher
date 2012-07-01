package no.uib.ii.algo.st8;

import no.uib.ii.algo.st8.start.Coordinate;
import android.graphics.Color;

public class StandardVertexConfiguration {
	private Coordinate coordinate;
	private int color;
	private String label;
	private int id;
	private float size;

	public StandardVertexConfiguration() {
		this(null);
	}

	public StandardVertexConfiguration(Coordinate coor) {
		this.coordinate = coor;
		color = Color.RED;
		size = 15;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + color;
		result = prime * result
				+ ((coordinate == null) ? 0 : coordinate.hashCode());
		result = prime * result + id;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + Float.floatToIntBits(size);
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
		StandardVertexConfiguration other = (StandardVertexConfiguration) obj;
		if (color != other.color)
			return false;
		if (coordinate == null) {
			if (other.coordinate != null)
				return false;
		} else if (!coordinate.equals(other.coordinate))
			return false;
		if (id != other.id)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (Float.floatToIntBits(size) != Float.floatToIntBits(other.size))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StandardVertexConfiguration [coordinate=" + coordinate
				+ ", color=" + color + ", label=" + label + ", id=" + id + "]";
	}

}
