package no.uib.ii.algo.st8;

import no.uib.ii.algo.st8.algorithms.Algorithm;
import no.uib.ii.algo.st8.algorithms.ProgressListener;
import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultVertex;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;

/**
 * An android-UI-wrapper for <code>Algorithm</class>. 
 * It adds a progress dialog for the execution of the algorithm
 * 
 * @param <Result>
 *            the return type of the <code>Algorithm</code>
 */
public abstract class AlgoWrapper<Result> extends AsyncTask<Void, Integer, Result> implements ProgressListener {
	protected final Workspace activity;
	protected final ProgressDialog pDialog;
	protected final Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Result> algorithm;

	/**
	 * Sets a new title for the progress dialog
	 * 
	 * @param dialogTitle
	 *            the new title
	 */
	public void setTitle(String dialogTitle) {
		pDialog.setTitle(dialogTitle);
	}

	@Override
	protected void onPreExecute() {
		pDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				activity.shortToast("Computation cancelled");
				cancel(true);
				algorithm.cancel();
				GraphViewController.time(false);
			}
		});

		pDialog.show();
	}

	@Override
	public void progress(float f) {
		pDialog.setMax(100);
		publishProgress((int) f * 100);
	}

	@Override
	public void progress(int k, int n) {
		pDialog.setMax(n);
		publishProgress(k);
	}

	@Override
	protected void onCancelled() {
		pDialog.cancel();
		GraphViewController.time(false);
	}

	/**
	 * Override this with whatever you want the result-text to be <b /> example:
	 * return "The tree width is " + result;
	 * 
	 * <p>
	 * Other code, such as updating the view could/should of course also be
	 * added here
	 * </p>
	 * 
	 * @param result
	 *            the result the algorithm has calculated
	 * @return a textual representation of the algorithm's result
	 * */
	protected abstract String resultText(Result result);

	@Override
	protected void onPostExecute(Result result) {
		pDialog.dismiss();

		AlertDialog.Builder resDialog = new AlertDialog.Builder(activity);

		resDialog.setMessage(resultText(result));
		resDialog.setTitle("Result");
		resDialog.setPositiveButton("OK", null);
		resDialog.create().show();

		GraphViewController.time(false);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		for (Integer progress : values)
			pDialog.setProgress(progress);
	}

	private void setUpProgressDialog() {
		pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.setTitle("Computing...");
		pDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				pDialog.cancel();
			}
		});
	}

	/**
	 * Instantiates a an android-wrapper for the given <code>Algorithm</code> <br />
	 * It displays a progress bar during computation and shows a dialog with the
	 * result of the algorithm when it is done. <br />
	 * 
	 * @param activity
	 *            the current activity (for making the progress dialog modal
	 * @param algorithm
	 *            the algorithm this object is wrapping
	 */
	public AlgoWrapper(Workspace activity, Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Result> algorithm) {
		this.activity = activity;
		this.algorithm = algorithm;
		this.pDialog = new ProgressDialog(activity);
		this.algorithm.setProgressListener(this);
		setUpProgressDialog();
	}

	/**
	 * Instantiates a an android-wrapper for the given <code>Algorithm</code> <br />
	 * It displays a progress bar during computation and shows a dialog with the
	 * result of the algorithm when it is done. <br />
	 * 
	 * @param activity
	 *            the current activity (for making the progress dialog modal
	 * @param algorithm
	 *            the algorithm this object is wrapping
	 * @param progressTitle
	 *            the title text of the progress dialog
	 */
	public AlgoWrapper(Workspace activity, Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Result> algorithm,
			String progressTitle) {
		this(activity, algorithm);
		pDialog.setTitle(progressTitle);
	}

	@Override
	protected Result doInBackground(Void... params) {
		GraphViewController.time(true);
		return algorithm.execute();
	}

}
