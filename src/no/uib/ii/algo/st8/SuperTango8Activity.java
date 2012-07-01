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

		System.out.println("done markus log hei stupedamen");

		// Gesture detection
		gestureDetector = new GestureDetector(new SimpleGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};

		// scaleGestureDetector = new ScaleGestureDetector(this,
		// new SimpleScaleGestureDetector());

		controller = new GraphViewController(this, gestureListener);
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
	}

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

	/**
	 * Event Handling for Individual menu item selected Identify single menu
	 * item by it's id
	 * */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.compute_vertex_cover:
			controller.showVertexCover();
			controller.redraw();
			return true;

		case R.id.compute_maximum_independent_set:
			controller.showMaximumIndependentSet();
			controller.redraw();
			return true;

		case R.id.compute_maximum_clique:
			controller.showMaximumClique();
			controller.redraw();
			return true;

		case R.id.compute_minimum_dominating_set:
			controller.showDominatingSet();
			controller.redraw();
			return true;

		case R.id.spring:
			controller.longShake(50);
			controller.redraw();
			return true;

		case R.id.path:
			int res = controller.showPath();
			if (res < 0)
				Toast.makeText(SuperTango8Activity.this, "No path!",
						Toast.LENGTH_SHORT).show();

			else
				Toast.makeText(SuperTango8Activity.this, "Path length " + res,
						Toast.LENGTH_SHORT).show();

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
				Toast.makeText(SuperTango8Activity.this, "No cycles",
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(SuperTango8Activity.this, "Girth " + girth,
						Toast.LENGTH_SHORT).show();

			controller.redraw();
			return true;

		case R.id.metapost_to_clipboard:
			if (copyMetapostToClipboard()) {
				Toast.makeText(SuperTango8Activity.this,
						controller.graphInfo(), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(SuperTango8Activity.this,
						"An error occured copying to clipboard!",
						Toast.LENGTH_SHORT).show();
			}
			return true;

		case R.id.tikz_to_clipboard:
			if (copyTikzToClipboard()) {
				Toast.makeText(SuperTango8Activity.this,
						controller.graphInfo(), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(SuperTango8Activity.this,
						"An error occured copying to clipboard!",
						Toast.LENGTH_SHORT).show();
			}
			return true;

		case R.id.share_tikz:
			shareTikz();
			return true;

		case R.id.share_metapost:
			shareMetapost();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class SimpleGestureDetector extends SimpleOnGestureListener {
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
			controller.fling(e1, e2, velocityX, velocityY);
			return false;
		}
	}
}
