package no.uib.ii.algo.st8.algorithms;

public abstract class Algorithm<V, E, Return> {

	protected ProgressListener progressListener;

	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	public abstract Return execute();

}
