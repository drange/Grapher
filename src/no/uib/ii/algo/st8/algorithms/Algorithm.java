package no.uib.ii.algo.st8.algorithms;

import org.jgrapht.graph.SimpleGraph;

public abstract class Algorithm<V, E, Return> {

	protected volatile boolean cancelFlag = false;
	protected ProgressListener progressListener;
	protected final SimpleGraph<V, E> graph;

	public Algorithm(SimpleGraph<V, E> graph) {
		this.graph = graph;
		System.gc(); // TODO REMOVE!!!
	}

	public int graphSize() {
		return graph.vertexSet().size();
	}

	public int graphEdgeSize() {
		return graph.edgeSet().size();
	}

	public void cancel() {
		System.out.println("We have been cancelled.");
		cancelFlag = true;
		System.gc(); // TODO REMOVE!!!
	}

	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	/** Notifies the progress listener of the progress */
	protected void progress(float percent) {
		if (progressListener != null) {
			progressListener.progress(percent);
		}
	}

	/**
	 * Notifies the progress listener of the current progress (reached check
	 * point k out of n)
	 */
	protected void progress(int k, int n) {
		if (progressListener != null) {
			progressListener.progress(k, n);
		}
	}

	public abstract Return execute();

}
