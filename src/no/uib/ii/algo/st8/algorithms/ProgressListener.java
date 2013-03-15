package no.uib.ii.algo.st8.algorithms;

public interface ProgressListener {
	void progress(float percent);

	void progress(int k, int n);

}
