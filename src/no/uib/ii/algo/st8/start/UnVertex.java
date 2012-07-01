package no.uib.ii.algo.st8.start;

public class UnVertex {
	private final int id;
	private int label;

	public UnVertex(int id) {
		this.id = id;
		label = id;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int l) {
		label = l;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + label;
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
		UnVertex other = (UnVertex) obj;
		if (id != other.id)
			return false;
		if (label != other.label)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass() + ":" + label + " (" + id + ")";
	}

}
