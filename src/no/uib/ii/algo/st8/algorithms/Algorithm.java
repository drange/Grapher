package no.uib.ii.algo.st8.algorithms;

public abstract class Algorithm<V, E, Return> {

	protected ProgressListener progressListener;

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
	 * Notifies the progress listener of the current progress 
	 * (reached check point k out of n) 
	 */
	protected void progress(int k, int n) {
		if (progressListener != null) {
			progressListener.progress(k, n);
		}
	}
	
	public abstract Return execute();

}
