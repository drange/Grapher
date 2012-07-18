package no.uib.ii.algo.st8;

import java.util.List;

import no.uib.ii.algo.st8.start.Coordinate;
import no.uib.ii.algo.st8.start.GraphExporter;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SuperTango8Activity extends Activity implements OnClickListener,
		SensorEventListener {

	private GestureDetector gestureDetector;
	private View.OnTouchListener gestureListener;

	// private ScaleGestureDetector scaleGestureDetector;

	private GraphViewController controller;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// RESTORING GRAPH IF PRESENT!
		// if (savedInstanceState != null) {
		// SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph = null;
		// Object restore = savedInstanceState.getSerializable("graph");
		// if (restore != null) {
		// graph = (SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>)
		// restore;
		// }
		// if (graph != null && graph instanceof SimpleGraph) {
		// System.out.println("recovered graph "
		// + GraphInformation.graphInfo(graph));
		// }
		// }

		System.out.println("done markus log hei stupedamen");

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;

		// Gesture detection
		gestureDetector = new GestureDetector(new SimpleGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};

		// scaleGestureDetector = new ScaleGestureDetector(this,
		// new SimpleScaleGestureDetector());

		controller = new GraphViewController(this, gestureListener, width,
				height);
		setContentView(controller.getView());

		// shake
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		this.sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sensor = sensors.get(0);
			sensorManager.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_GAME);
			System.out.println("PAAL REGISTERED SENSOR");
		}
		controller.redraw();
	}

	// @Override
	// public void onSaveInstanceState(Bundle savedInstanceState) {
	// super.onSaveInstanceState(savedInstanceState);
	// // Save UI state changes to the savedInstanceState.
	// // This bundle will be passed to onCreate if the process is
	// // killed and restarted.
	// savedInstanceState.putSerializable("graph", controller.getGraph());
	// }

	private boolean copyTikzToClipboard() {
		String text = "";
		try {
			text = GraphExporter.getTikz(controller.getGraph());
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(text);
			return true;
		} catch (Exception e) {
			System.out.println("ERROR ON CLIPBOARD");
			System.out.println(text);
			e.printStackTrace();
			return false;
		}
	}

	private boolean copyMetapostToClipboard() {
		String text = "";
		try {
			text = GraphExporter.getMetapost(controller.getGraph());
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(text);
			return true;
		} catch (Exception e) {
			System.out.println("ERROR ON CLIPBOARD");
			System.out.println(text);
			e.printStackTrace();
			return false;
		}
	}

	private SensorManager sensorManager;
	private List<Sensor> sensors;
	private Sensor sensor;
	private long lastUpdate = -1;
	private long currentTime = -1;

	private float last_x, last_y, last_z;
	private float current_x, current_y, current_z, currenForce;
	private static final int FORCE_THRESHOLD = 500; // used to be 900
	private final int DATA_X = SensorManager.DATA_X;
	private final int DATA_Y = SensorManager.DATA_Y;
	private final int DATA_Z = SensorManager.DATA_Z;

	// //// shake
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER
				|| event.values.length < 3)
			return;

		currentTime = System.currentTimeMillis();

		if ((currentTime - lastUpdate) > 100) {
			long diffTime = (currentTime - lastUpdate);
			lastUpdate = currentTime;

			current_x = event.values[DATA_X];
			current_y = event.values[DATA_Y];
			current_z = event.values[DATA_Z];

			currenForce = Math.abs(current_x + current_y + current_z - last_x
					- last_y - last_z)
					/ diffTime * 10000;

			if (currenForce > FORCE_THRESHOLD) {
				controller.shake();
			}

			last_x = current_x;
			last_y = current_y;
			last_z = current_z;

		}
	}

	private void shareTikz() {
		String shareBody = GraphExporter.getTikz(controller.getGraph());

		shareBody += "\n\n% Sent to you by Grapher";

		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				controller.graphInfo());
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, "Share graph with"));

	}

	private void shareMetapost() {
		String shareBody = GraphExporter.getMetapost(controller.getGraph());

		shareBody += "\n\n% Sent to you by Grapher";

		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				controller.graphInfo());
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, "Share graph with"));

	}

	// //// shake

	public void onClick(View v) {
		// ignoring clicks, listens to gesture stuff anyway
	}

	// Initiating Menu XML file (menu.xml)
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.layout.drawer_context, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Unregister from SensorManager.
		sensorManager.unregisterListener(this);
		sensorManager.unregisterListener(this, sensor);
		for (Sensor s : sensors) {
			sensorManager.unregisterListener(this, s);
		}
	}

	private void shortToast(String toast) {
		Toast.makeText(SuperTango8Activity.this, toast, Toast.LENGTH_SHORT)
				.show();
	}

	// private void longToast(String toast) {
	// Toast.makeText(SuperTango8Activity.this, toast, Toast.LENGTH_LONG)
	// .show();
	// }

	/**
	 * Event Handling for Individual menu item selected Identify single menu
	 * item by it's id
	 * */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.compute_vertex_cover:
			int vc = controller.showVertexCover();
			controller.redraw();
			shortToast("Vertex Cover Number " + vc);
			return true;

		case R.id.compute_maximum_independent_set:
			int mis = controller.showMaximumIndependentSet();
			controller.redraw();
			shortToast("Independent Set Number " + mis);
			return true;

		case R.id.compute_maximum_clique:
			int mc = controller.showMaximumClique();
			controller.redraw();
			shortToast("Clique Number " + mc);
			return true;

		case R.id.compute_minimum_dominating_set:
			int ds = controller.showDominatingSet();
			controller.redraw();
			shortToast("Dominating Set Number " + ds);
			return true;

		case R.id.spring:
			controller.longShake(100);
			controller.redraw();
			shortToast("Shaken, not stirred");
			return true;

		case R.id.path:
			int res = controller.showPath();
			if (res < 0)
				shortToast("No path!");
			else
				shortToast("Path length " + res);
			controller.redraw();
			return true;

		case R.id.compute_mst:
			controller.showSpanningTree();
			controller.redraw();
			return true;

		case R.id.compute_diameter:
			int diam = controller.diameter();
			if (diam < 0)
				Toast.makeText(SuperTango8Activity.this,
						"Diameter is infinite", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(SuperTango8Activity.this, "Diameter " + diam,
						Toast.LENGTH_SHORT).show();

			controller.redraw();
			return true;

		case R.id.compute_girth:
			int girth = controller.girth();
			if (girth < 0)
				Toast.makeText(SuperTango8Activity.this, "Acyclic",
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(SuperTango8Activity.this, "Girth " + girth,
						Toast.LENGTH_SHORT).show();

			controller.redraw();
			return true;

			// case R.id.compute_cut:
			// boolean hascut = controller.showCutVertex();
			// if (!hascut)
			// Toast.makeText(SuperTango8Activity.this, "No cut vertices",
			// Toast.LENGTH_SHORT).show();
			// controller.redraw();
			// return true;

		case R.id.compute_all_cuts:
			int cuts = controller.showAllCutVertices();
			if (cuts == 0)
				shortToast("No cut vertices");

			else if (cuts == 1)
				shortToast("1 cut vertex");
			else
				shortToast(cuts + " cut vertices");
			controller.redraw();
			return true;

			// case R.id.compute_bridge:
			// boolean hasbridge = controller.showBridge();
			// if (!hasbridge)
			// Toast.makeText(SuperTango8Activity.this, "No bridges",
			// Toast.LENGTH_SHORT).show();
			// controller.redraw();
			// return true;

		case R.id.compute_all_bridges:
			int bridges = controller.showAllBridges();
			if (bridges == 0)
				shortToast("No bridges");
			else if (bridges == 1)
				shortToast("1 bridge");
			else
				shortToast(bridges + " bridges");

			controller.redraw();
			return true;

		case R.id.show_center:
			boolean conn = controller.showCenterVertex();
			if (!conn)
				shortToast("No center vertex in disconnected graph");
			controller.redraw();
			return true;

		case R.id.centralize:
			controller.centralize();
			controller.redraw();
			return true;

		case R.id.add_universal_vertex:
			int degree = controller.addUniversalVertex();
			if (degree == 0)
				shortToast("Added singleton");
			else if (degree == 1)
				shortToast("Added vertex adjacent to 1 vertex");
			else
				shortToast("Added vertex adjacent to " + degree + " vertices");

			controller.redraw();
			return true;

		case R.id.compute_bandwidth:
			int bandwidth = controller.computeBandwidth();
			shortToast("Bandwidth " + bandwidth);
			return true;

		case R.id.metapost_to_clipboard:
			if (copyMetapostToClipboard()) {
				shortToast("Copied info on " + controller.graphInfo());
			} else {
				shortToast("An error occured copying to clipboard!");
			}
			return true;

		case R.id.tikz_to_clipboard:
			if (copyTikzToClipboard()) {
				shortToast("Copied info on " + controller.graphInfo());
			} else {
				shortToast("An error occured copying to clipboard!");
			}
			return true;

		case R.id.share_tikz:
			shareTikz();
			return true;

		case R.id.share_metapost:
			shareMetapost();
			return true;

		case R.id.clear:
			controller.clearAll();
			controller.redraw();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class SimpleGestureDetector extends SimpleOnGestureListener {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			controller.moveView(new Coordinate(-distanceX, -distanceY));
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			float x = e.getX();
			float y = e.getY();
			controller.userClicked(new Coordinate(x, y));
			return super.onSingleTapConfirmed(e);
		}

		@Override
		public void onLongPress(MotionEvent e) {
			float x = e.getX();
			float y = e.getY();
			controller.userLongPress(new Coordinate(x, y));
			super.onLongPress(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			float x = e.getX();
			float y = e.getY();
			return controller.userDoubleTap(new Coordinate(x, y));
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// System.out.println("FLING! " + e1.getX() + "," + e1.getY() +
			// " -- " + e2.getX() + "," + e2.getY());
			//controller.fling(e1, e2, velocityX, velocityY);
			return false;
		}
	}
}
