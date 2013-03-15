package no.uib.ii.algo.st8.algorithms;

import android.os.AsyncTask;

public abstract class Algorithm<V, E, Progress, Result> extends
		AsyncTask<Void, Progress, Result> {

	protected ProgressListener progressListener;

	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	public abstract Result doInBackground();

	@Override
	public final Result doInBackground(Void... params) {
		return doInBackground();
	}

}
