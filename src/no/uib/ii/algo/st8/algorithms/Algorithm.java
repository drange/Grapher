package no.uib.ii.algo.st8.algorithms;

import org.jgrapht.graph.SimpleGraph;

public abstract class Algorithm<V, E, Return> {

	protected volatile boolean cancelFlag = false;
	protected ProgressListener progressListener;
	protected final SimpleGraph<V, E> graph;

	// these fields are to make sure we do not update progress bar more than n
	// times per second
	private long nanoDelay = 0L;// 20000000L;
	private long nanoPrev = System.nanoTime() - nanoDelay;

	private int progressGoal;
	private int currentProgress = 0;

	public Algorithm(SimpleGraph<V, E> graph) {
		this.graph = graph;
		progressGoal = (int) Math.pow(2, graphSize());
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
	}

	protected void setProgressGoal(int progressGoal) {
		this.progressGoal = progressGoal;
		currentProgress = Math.min(currentProgress, progressGoal);
	}

	/**
	 * Returns true if setting current progress was successful, i.e.,
	 * currentProgress <= progressGoal
	 * 
	 * @param currentProgress
	 * @return true if update was successful
	 */
	protected boolean setCurrentProgress(int currentProgress) {
		if (currentProgress > progressGoal)
			return false;
		this.currentProgress = currentProgress;
		return true;
	}

	/**
	 * Increases current progress, returns true if current progress has reached
	 * its goal.
	 * 
	 * @return true if current >= goal
	 */
	protected boolean increaseProgress() {
		currentProgress++;
		if (currentProgress > progressGoal) {
			currentProgress = progressGoal;
			return true;
		}

		progress(currentProgress, progressGoal);

		return false;
	}

	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	/** Notifies the progress listener of the progress */
	protected void progress(float percent) {
		if (progressListener != null) {
			long now = System.nanoTime();
			if (now - nanoPrev > nanoDelay) {
				progressListener.progress(percent);
				nanoPrev = now;
			}
		}
	}

	/**
	 * Notifies the progress listener of the current progress (reached check
	 * point k out of n)
	 */
	protected void progress(int k, int n) {
		if (progressListener != null) {
			long now = System.nanoTime();
			if (now - nanoPrev > nanoDelay) {
				progressListener.progress(k, n);
				nanoPrev = now;
			}
		}
	}

	public abstract Return execute();

}
