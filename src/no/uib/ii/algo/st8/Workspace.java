package no.uib.ii.algo.st8;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import no.uib.ii.algo.st8.util.FileAccess;
import no.uib.ii.algo.st8.util.GraphExporter;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author pgd
 */
public class Workspace extends Activity implements OnClickListener, SensorEventListener {

	private GraphViewController controller;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		System.out.println("done markus log hei stupedamen");

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;

		// scaleGestureDetector = new ScaleGestureDetector(this,
		// new SimpleScaleGestureDetector());

		// Bitmapbmp=BitmapFactory.decodeResource(getResources(),R.drawable.bg_image);

		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.bg_image_larger);

		BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
		bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		controller = new GraphViewController(this, width, height);
		controller.getView().setBackgroundDrawable(bitmapDrawable);

		setContentView(controller.getView());

		// shake
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		this.sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sensor = sensors.get(0);
			sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
			System.out.println("PAAL REGISTERED SENSOR");
		}
		controller.redraw();
	}

	private boolean copyTikzToClipboard() {
		String text = GraphExporter.getTikz(controller.getGraph(), controller.getTransformMatrix());
		try {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(text);
			return true;
		} catch (Exception e) {
			System.err.println("Error while copying TiKZ to clipboard: " + e.getMessage());
			e.printStackTrace();
			e.printStackTrace();
			return false;
		}
	}

	private boolean copyMetapostToClipboard() {
		String text = GraphExporter.getMetapost(controller.getGraph());
		try {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(text);
			return true;
		} catch (Exception e) {
			System.err.println("Error while copying metapost to clipboard: " + e.getMessage());
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
	private static final int FORCE_THRESHOLD = 800; // used to be 900
	private final int DATA_X = SensorManager.DATA_X;
	private final int DATA_Y = SensorManager.DATA_Y;
	private final int DATA_Z = SensorManager.DATA_Z;

	// //// shake
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER || event.values.length < 3)
			return;

		currentTime = System.currentTimeMillis();

		if ((currentTime - lastUpdate) > 100) {
			long diffTime = (currentTime - lastUpdate);
			lastUpdate = currentTime;

			current_x = event.values[DATA_X];
			current_y = event.values[DATA_Y];
			current_z = event.values[DATA_Z];

			currenForce = Math.abs(current_x + current_y + current_z - last_x - last_y - last_z) / diffTime * 10000;

			if (currenForce > FORCE_THRESHOLD) {
				controller.shake();
			}

			last_x = current_x;
			last_y = current_y;
			last_z = current_z;

		}
	}

	private void shareTikz() {
		String shareBody = GraphExporter.getTikz(controller.getGraph(), controller.getTransformMatrix());

		shareBody += "\n\n% Sent to you by Grapher";

		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, controller.graphInfo());
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, "Share graph with"));

	}

	private void shareMetapost() {
		String shareBody = GraphExporter.getMetapost(controller.getGraph());

		shareBody += "\n\n% Sent to you by Grapher";

		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, controller.graphInfo());
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
		Toast.makeText(Workspace.this, toast, Toast.LENGTH_SHORT).show();
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

		case R.id.compute_treewidth:
			int x = controller.treewidth();
			if (x > 0)
				shortToast("Treewidth = " + x);
			else
				shortToast("Application failure detected.");

			return true;

		case R.id.compute_simplicial_vertices:
			int simplicials = controller.showSimplicialVertices();
			if (simplicials > 1)
				shortToast(simplicials + " simplicial vertices");
			else if (simplicials == 1)
				shortToast(simplicials + " simplicial vertex");
			else
				shortToast("No simplicial vertices.");

			return true;

		case R.id.compute_claw_deletion:
			int deletionSize = controller.showClawDeletion();
			controller.redraw();
			if (deletionSize == 0)
				shortToast("Graph is claw-free");
			else
				shortToast("Claw-free deletion size " + deletionSize);
			return true;

		case R.id.compute_perfect_code:
			int perfCodeSize = controller.showPerfectCode();
			controller.redraw();
			if (perfCodeSize < 0)
				shortToast("Not perfect code");
			else
				shortToast("Perfect code size " + perfCodeSize);
			return true;

		case R.id.compute_claws:
			boolean hasclaw = controller.showAllClaws();
			controller.redraw();
			if (hasclaw)
				shortToast("Found claw");
			else
				shortToast("Graph is claw-free");
			return true;

		case R.id.compute_cycle_4:
			int c4s = controller.showAllCycle4();
			controller.redraw();
			if (c4s == 0)
				shortToast("No C_4s");
			else
				shortToast("Number of C4s " + c4s);
			return true;

		case R.id.compute_regularity_deletion_set:
			int regdel = controller.showRegularityDeletionSet();
			controller.redraw();
			if (regdel == 0)
				shortToast("Graph is regular");
			else
				shortToast("Regularity deletion set number " + regdel);
			return true;

		case R.id.compute_odd_cycle_transversal:
			int oct = controller.showOddCycleTransversal();
			controller.redraw();
			if (oct == 0)
				shortToast("Graph is bipartite (has no odd cycles)");
			else
				shortToast("Odd Cycle Transversal number " + oct);
			return true;

		case R.id.compute_feedback_vertex_set:
			int fvs = controller.showFeedbackVertexSet();
			controller.redraw();
			if (fvs == 0)
				shortToast("Graph is acyclic");
			else
				shortToast("Feedback Vertex Set number " + fvs);
			return true;

		case R.id.compute_vertex_cover:
			int vc = controller.showVertexCover();
			controller.redraw();
			shortToast("Vertex Cover Number " + vc);
			return true;

		case R.id.compute_connected_vertex_cover:
			int cvc = controller.showConnectedVertexCover();
			controller.redraw();
			if (cvc < 0) {
				shortToast("No connected vertex cover");
			} else {
				shortToast("Connected Vertex Cover Number " + cvc);
			}
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
			controller.longShake(200);
			controller.redraw();
			shortToast("Shaken, not stirred");
			return true;

		case R.id.hamiltonian_path:
			boolean hamiltonianPath = controller.showHamiltonianPath();

			if (hamiltonianPath)
				shortToast("Hamiltonian path highlighted");
			else
				shortToast("No hamiltonian path!");

			controller.redraw();
			return true;

		case R.id.hamiltonian_cycle:
			boolean hamiltonianCycle = controller.showHamiltonianCycle();

			if (hamiltonianCycle)
				shortToast("Graph is hamiltonian (cycle highlighted)");
			else
				shortToast("Graph is not hamiltonian");

			controller.redraw();
			return true;

		case R.id.flow:
			int flow = controller.showFlow();
			if (flow < 0)
				shortToast("Please select two vertices (hold to select)");
			else if (flow == 0)
				shortToast("Not connected");
			else
				shortToast("Max flow " + flow);
			controller.redraw();
			return true;

		case R.id.path:
			int res = controller.showPath();
			if (res < 0)
				shortToast("Please select two vertices (hold to select)");
			else if (res == 0)
				shortToast("No path!");
			else
				shortToast("Path length " + res);
			controller.redraw();
			return true;

		case R.id.power:
			controller.constructPower();
			shortToast("Power graph has been constructed");
			controller.redraw();
			return true;

		case R.id.compute_mst:
			controller.showSpanningTree();
			controller.redraw();
			return true;

		case R.id.compute_balanced_separator:
			int sep = controller.showSeparator();
			if (sep < 0)
				shortToast("No balanced separator.");
			else
				shortToast("Found balanced separator of size " + sep);
			controller.redraw();
			return true;

		case R.id.compute_diameter:
			int diam = controller.diameter();
			if (diam < 0)
				shortToast("Diameter is infinite");
			else
				shortToast("Diameter " + diam);

			controller.redraw();
			return true;

		case R.id.compute_girth:
			int girth = controller.girth();
			if (girth < 0)
				shortToast("Acyclic");
			else
				shortToast("Girth " + girth);

			controller.redraw();
			return true;

		case R.id.bipartition:
			boolean bipartite = controller.showBipartition();
			if (bipartite)
				shortToast("Is bipartite");
			else
				shortToast("Is not bipartite");

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

		case R.id.test_eulerian:
			boolean eulerian = controller.isEulerian();
			if (eulerian)
				shortToast("Graph is eulerian");
			else
				shortToast("Graph is not eulerian, odd degree vertices highlighted.");

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

		case R.id.select_all:
			controller.selectAll();
			controller.redraw();
			return true;

		case R.id.deselect_all:
			controller.deselectAll();
			controller.redraw();
			return true;

		case R.id.select_all_highlighted_vertices:
			controller.selectAllHighlightedVertices();
			controller.redraw();
			return true;

		case R.id.invert_selected:
			controller.invertSelectedVertices();
			controller.redraw();
			return true;

		case R.id.select_reachable:
			controller.selectAllReachableVertices();
			controller.redraw();
			return true;

		case R.id.complete_selected:
			controller.completeSelectedVertices();
			controller.redraw();
			return true;

		case R.id.complement_selected:
			controller.complementSelected();
			controller.redraw();
			return true;

		case R.id.delete_selected:
			int deleted = controller.deleteSelectedVertices();
			if (deleted == 0) {
				shortToast("No vertices selected");
			} else {
				shortToast("Deleted " + deleted + " vertices");
			}
			controller.redraw();
			return true;

		case R.id.induce_subgraph:
			int removed = controller.induceSubgraph();
			if (removed == 0) {
				shortToast("All vertices selected, none deleted");
			} else {
				shortToast("Removed " + removed + " vertices");
			}
			controller.redraw();
			return true;

		case R.id.clear:
			controller.clearAll();
			controller.redraw();
			return true;

		case R.id.save:
			save();
			return true;

		case R.id.load:
			load();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void save() {

		System.out.println("save");
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Title");
		alert.setMessage("Message");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@SuppressLint("WorldReadableFiles")
			public void onClick(DialogInterface dialog, int whichButton) {
				Editable value = input.getText();
				try {
					String json = new FileAccess().save(controller.getGraph());
					FileOutputStream fOut = openFileOutput(value + ".json", MODE_WORLD_READABLE);
					OutputStreamWriter osw = new OutputStreamWriter(fOut);

					// Write the string to the file
					osw.write(json);

					/*
					 * ensure that everything is really written out and close
					 */
					osw.flush();
					osw.close();

				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();
	}

	public void load() {

		System.out.println("load");
		final String[] files = fileList();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick a file");
		builder.setItems(files, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Toast.makeText(getApplicationContext(), files[item], Toast.LENGTH_SHORT).show();
				try {
					StringBuffer stringBuffer = new StringBuffer();
					String inputLine = "";
					FileInputStream input = openFileInput(files[item].toString());
					InputStreamReader isr = new InputStreamReader(input);
					BufferedReader bufferedReader = new BufferedReader(isr);

					while ((inputLine = bufferedReader.readLine()) != null) {
						stringBuffer.append(inputLine);
						stringBuffer.append("\n");
					}

					bufferedReader.close();
					String json = stringBuffer.toString();
					System.out.println(json);

					new FileAccess().load(controller.getGraph(), json);

					controller.redraw();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

}
