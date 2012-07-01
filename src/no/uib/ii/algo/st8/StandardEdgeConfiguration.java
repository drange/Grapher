package no.uib.ii.algo.st8;

public class StandardEdgeConfiguration {
	private EdgeStyle style;
	private int color;
	private String label;
	private float weight;

	public EdgeStyle getStyle() {
		return style;
	}

	public void setStyle(EdgeStyle style) {
		this.style = style;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + color;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((style == null) ? 0 : style.hashCode());
		result = prime * result + Float.floatToIntBits(weight);
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
		StandardEdgeConfiguration other = (StandardEdgeConfiguration) obj;
		if (color != other.color)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (style != other.style)
			return false;
		if (Float.floatToIntBits(weight) != Float.floatToIntBits(other.weight))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StandardEdgeConfiguration [style=" + style + ", color=" + color
				+ ", label=" + label + ", weight=" + weight + "]";
	}

}
