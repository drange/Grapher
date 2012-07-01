package no.uib.ii.algo.st8.start;

public class UnEdge {
	private final UnVertex u;
	private final UnVertex v;

	public UnEdge(UnVertex u, UnVertex v) {
		if (u == null)
			throw new NullPointerException("Vertex u was null, v was " + v);
		this.u = u;
		if (v == null)
			throw new NullPointerException("Vertex v was null, u was " + v);
		this.v = v;
	}

	public UnVertex getSource() {
		return u;
	}

	public UnVertex getTarget() {
		return v;
	}

	public boolean isIncident(UnVertex vertex) {
		return u.equals(vertex) || v.equals(vertex);
	}

	public UnVertex getOpposite(UnVertex vertex) {
		if (u.equals(vertex))
			return v;
		if (v.equals(vertex))
			return u;
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
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
		UnEdge other = (UnEdge) obj;
		if (u == null) {
			if (other.u != null)
				return false;
		} else if (!u.equals(other.u))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass() + ":[" + u + "--" + v + "]";
	}

}