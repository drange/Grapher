package no.uib.ii.algo.st8;

import java.util.ArrayList;
import java.util.Collections;
import no.uib.ii.algo.st8.interval.BasicGraph;
import no.uib.ii.algo.st8.interval.Interval;
import no.uib.ii.algo.st8.interval.IntervalGraph;
import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultEdgeFactory;
import no.uib.ii.algo.st8.model.DefaultVertex;
import no.uib.ii.algo.st8.util.Coordinate;
import org.jgrapht.graph.SimpleGraph;
import android.content.Context;
import android.graphics.Matrix;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * Class for controlling all the features of the interval view, except, menus
 * and drawing of screen.
 * 
 * @author Olav Wegner Eide
 */
public class IntervalViewController {
	private IntervalView view;
	public static boolean moveMode;
	public static boolean packing;

	private ArrayList<DrawableInterval> toShift;
	private ArrayList<DrawableInterval> toExpandOrDecrease;

	private boolean moving = false;

	private int scale = 100;

	public static int TRASH_CAN = 0;
	private String info = "";
	private IntervalGraph graph;
	private final IntervalActivity activity;
	private final Vibrator vibrator;
	private ArrayList<DrawableInterval> marked = new ArrayList<DrawableInterval>();
	private ArrayList<ArrayList<DrawableInterval>> representation;

	private DrawableInterval touchedVertex = null;
	private int oldX, oldY;

	// TODO this should depend on screen size and or zoom (scale of matrix)
	public final static float USER_MISS_RADIUS = 40;

	private final Coordinate CENTER_COORDINATE;

	public IntervalViewController(IntervalActivity activity, int width,
			int height) {
		this.activity = activity;
		vibrator = (Vibrator) activity
				.getSystemService(Context.VIBRATOR_SERVICE);

		// not good! but for now
		if (GraphViewController.ig != null) {
			graph = GraphViewController.ig; // save for purpose of translating
			// back to simplegraph
		} else
			graph = new IntervalGraph();

		view = new IntervalView(activity);
		togglePack();
		redraw();

		moveMode = false;
		view.setOnClickListener(activity);
		view.setOnTouchListener(new View.OnTouchListener() {
			PrivateGestureListener gl = new PrivateGestureListener();
			GestureDetector gd = new GestureDetector(gl); // TODO deprecated!

			public boolean onTouch(View view, MotionEvent event) {
				// What
				// happends
				// when
				// intervals
				// are moved
				// around.
				if (event.getAction() == MotionEvent.ACTION_UP && moveMode
						&& touchedVertex != null) {
					if (TRASH_CAN == 2) {
						touchedVertex = null;
						redraw();
					} else if (touchedVertex.getMoveCoordinate() != null) {
						int y = Math.round(translateCoordinate(touchedVertex.getMoveCoordinate()).getY()
								/ scale);
						int x = leftCoordinateTranslation(translateCoordinate(touchedVertex.getMoveCoordinate()), false);
						Interval i = touchedVertex.getInterval();
						System.out.println("Insert: Y: " + y + " x: " + x);
						if (y > -1 && y < representation.size()) {
							boolean overlapping = isOverlapping(i, y, x);
							// Check overlapping, if true return
							if (overlapping || x < 0) {
								representation.get(oldY).add(oldX,
										touchedVertex);
								graph.addInterval(i.getLeft(), i.getRight());
								System.out.println("1");
							} else { // No: add
								Interval i2 = new Interval(x, x + i.length());
								DrawableInterval di = new DrawableInterval(i2,
										y * scale);
								representation.get(y).add(di);
								graph.addInterval(i2.getLeft(), i2.getRight());
								System.out.println("2");
							}
						}

						else if (y < 0) {
							// Add to top, increaseYs
							ArrayList<DrawableInterval> list = new ArrayList<DrawableInterval>();
							Interval i2;
							if (x < 0) {
								i2 = new Interval(1, 1 + i.length());
								list.add(0, new DrawableInterval(i2, 0 * scale));
								System.out.println("3");
							} else {
								i2 = new Interval(x, x + i.length());
								list.add(0, new DrawableInterval(i2, 0 * scale));
								System.out.println("4");
							}
							representation.add(0, list);
							graph.addInterval(i2.getLeft(), i2.getRight());
							updateYs(1);
						}

						else if (y > representation.size()) {
							// Add to bottom
							ArrayList<DrawableInterval> list = new ArrayList<DrawableInterval>();
							Interval i2;
							if (x < 0) {
								i2 = new Interval(1, 1 + i.length());
								list.add(new DrawableInterval(i2,
										representation.size() * scale));
								System.out.println("5");
							} else {
								i2 = new Interval(x, x + i.length());
								skipLine(y);
								list.add(new DrawableInterval(i2,
										representation.size() * scale));
								System.out.println("6");
							}
							representation.add(list);
							graph.addInterval(i2.getLeft(), i2.getRight());
						}

						else {
							representation.get(oldY).add(oldX, touchedVertex);
							graph.addInterval(i.getLeft(), i.getRight());
							System.out.println("7");
						}
					}
					touchedVertex = null;
					redraw();
					gl.clearCoordinate();
					trashCan(0);
					moving = false;
				}
				return gd.onTouchEvent(event);
			}
		});
		CENTER_COORDINATE = new Coordinate(width / 2, height / 2);
	}

	private void skipLine(int y){
		for(int i=representation.size(); i<y; i++){
			representation.add(new ArrayList<DrawableInterval>());
		}
	}


	/**
	 * Checking if a interval o is overlapping with any other intervals on a
	 * line y
	 * 
	 * @param o
	 *            - interval to be checked
	 * @param y
	 *            - line in representation
	 * @param intervalStart
	 *            - the left side of the interval
	 * @return - true if there is an interval overlapping o, false if not
	 */
	private boolean isOverlapping(Interval o, int y, int intervalStart) {
		Interval i2 = new Interval(intervalStart, intervalStart + o.length());
		for (DrawableInterval di : representation.get(y)) {
			if (di.getInterval().overlaps(i2))
				return true;
		}
		return false;
	}

	/**
	 * Checks if the increased/decreased interval i is overlapping with any other intervals on that line
	 */
	private boolean overLappingLine(int y, int x, Interval i) {
		ArrayList<DrawableInterval> line = representation.get(y);
		for (int j = 0; j < line.size(); j++) {
			int l=line.get(j).getInterval().getLeft(), r=line.get(j).getInterval().getRight();
			if (i.overlaps(line.get(j).getInterval()) && j!=x){
				System.out.println("J:" + l + "-" +r);
				System.out.println("overlaps!: " + i.getLeft() + " " + i.getRight());
				return true;
			}	
		}
		return false;
	}

	/**
	 * Move all Intervals to the right of the intersection of the given interval
	 * 
	 * @param move
	 *            - how many places the interval should be moved
	 * @param y
	 *            - line in representation
	 * @param x
	 *            - interval in the line
	 */
	private void shiftRight(int move, int y, int x) {
		Interval i = representation.get(y).get(x).getInterval();
		for (ArrayList<DrawableInterval> list : representation) {
			for (DrawableInterval di : list) {
				if (!di.equals(representation.get(y).get(x))) {
					Interval i2 = di.getInterval();
					if (i2.getLeft() >= i.getLeft()) {
						int index = getIntervalIndex(i2);
						di.setInterval(new Interval(i2.getLeft() + move, i2
								.getRight() + move));
						graph.setInterval(i2.getLeft() + move, i2.getRight()
								+ move, index);
					}
				}
			}
		}
	}

	/**
	 * Move all intervals to the left of the intersection of the given interval
	 * 
	 * @param move
	 *            - how many places the interval should be moved
	 * @param y
	 *            - line in representation
	 * @param x
	 *            - interval in the line
	 */
	private boolean shiftLeft(int move, int y, int x) {
		toShift = new ArrayList<DrawableInterval>();
		Interval i = representation.get(y).get(x).getInterval();
		for (ArrayList<DrawableInterval> list : representation) {
			int longestToTheLeft = -1;
			for (DrawableInterval di : list) {
				Interval i2 = di.getInterval();
				if (i2.getRight() > i.getRight() && i2.getLeft() < i.getRight()) {
					longestToTheLeft = i2.getLeft() + move;
				}
			}
			for (DrawableInterval di : list) {
				Interval i2 = di.getInterval();
				if ((!di.equals(representation.get(y).get(x)))
						&& (i2.getRight() <= i.getRight())) {
					if (i2.getRight() + move > i.getRight()
							|| i2.getRight() + move >= longestToTheLeft) {
						toShift = null;
						return false;
					}
					toShift.add(di);
				}
			}
		}
		return true;
	}

	/**
	 * Move all Intervals to the left of the intersection of the given interval
	 * 
	 * @param move
	 *            - how many places the interval should be moved
	 * @param y
	 *            - line in representation
	 * @param x
	 *            - interval in the line
	 */
	private boolean deShiftLeft(int move, int y, int x) {
		toShift = new ArrayList<DrawableInterval>();
		Interval i = representation.get(y).get(x).getInterval();
		for (ArrayList<DrawableInterval> list : representation) {
			for (DrawableInterval di : list) {
				Interval i2 = di.getInterval();
				if ((!di.equals(representation.get(y).get(x)))
						&& (i2.getRight() <= i.getRight())) {
					if (i2.getLeft() - move <= 0) {
						toShift = null;
						return false;
					}
					toShift.add(di);
				}
			}
		}
		return true;
	}

	private void doDeShifting(int move) {
		if (toShift != null) {
			for (DrawableInterval di : toShift) {
				int index = getIntervalIndex(di.getInterval());
				int y = Math.round(di.getLeft().getY() / scale);
				int x = representation.get(y).indexOf(di);
				Interval i2 = di.getInterval();
				representation
				.get(y)
				.get(x)
				.setInterval(
						new Interval(i2.getLeft() - move, i2.getRight()
								- move));
				graph.setInterval(i2.getLeft() - move, i2.getRight() - move,
						index);

			}
		}
	}

	/**
	 * Does the DeShifting for the intervals in toShift.
	 * 
	 * @param - move number of places to shift.
	 */
	private void doShifting(int move) {
		if (toShift != null) {
			for (DrawableInterval di : toShift) {
				int index = getIntervalIndex(di.getInterval());
				int y = Math.round(di.getLeft().getY() / scale);
				int x = representation.get(y).indexOf(di);
				Interval i2 = di.getInterval();
				representation
				.get(y)
				.get(x)
				.setInterval(
						new Interval(i2.getLeft() + move, i2.getRight()
								+ move));
				graph.setInterval(i2.getLeft() + move, i2.getRight() + move,
						index);

			}
		}
	}

	/**
	 * Move all Intervals to the right of the intersection of the given interval
	 * 
	 * @param move
	 *            - how many places the interval should be moved
	 * @param y
	 *            - line in representation
	 * @param x
	 *            - interval in the line
	 */
	private boolean deShiftRight(int move, int y, int x) {
		Interval i = representation.get(y).get(x).getInterval();
		toShift = new ArrayList<DrawableInterval>();
		for (ArrayList<DrawableInterval> list : representation) {
			int longestFromRight = -1;
			for (DrawableInterval di : list) {
				Interval i2 = di.getInterval();
				if (i2.getLeft() < i.getLeft() && i2.getRight() > i.getLeft()
						&& i2.getRight() <= i.getRight())
					longestFromRight = i2.getRight() - move;
			}
			for (DrawableInterval di : list) {
				Interval i2 = di.getInterval();
				if ((!di.equals(representation.get(y).get(x)))
						&& (i2.getLeft() >= i.getLeft())) {
					if (longestFromRight > i2.getLeft() - move
							|| i2.getLeft() - move < i.getLeft()) {
						toShift = null;
						return false;
					}
					// if((!di.equals(representation.get(y).get(x))) &&
					// i2.getRight()<=i.getRight()){
					// System.out.println("I: " + i.getLeft() + "-" +
					// i.getRight() + " I2: " + i2.getLeft() + "-" +
					// i2.getRight());
					// if(i2.getLeft()-move<=0){
					// toShift=null;
					// return false;
					// }
					toShift.add(di);
					// int index=getIntervalIndex(i2);
					// di.setInterval(new
					// Interval(i2.getLeft()-move,i2.getRight()-move));
					// graph.setInterval(i2.getLeft()-move,i2.getRight()-move,index);
				}
			}
		}
		return true;
	}

	/**
	 * Does the expanding or Decreasing of intervals added to
	 * toExpandOrDecrease.
	 * 
	 * @param move
	 *            - number of places to expand or decrease.
	 * @param moveLeftSide
	 *            - true: move left side of interval, False: move Right side of
	 *            interval.
	 * @param moveLeft
	 *            - true: move interval to the left, False: move interval to the
	 *            right.
	 */
	private void doExpandingOrDecreasing(int move, boolean moveLeftSide,
			boolean moveLeft) {
		if (toExpandOrDecrease != null) {
			for (DrawableInterval di : toExpandOrDecrease) {
				int index = getIntervalIndex(di.getInterval());
				int y = Math.round(di.getLeft().getY() / scale);
				int x = representation.get(y).indexOf(di);
				Interval i2 = di.getInterval();

				if (!moveLeftSide && !moveLeft) { // move right side to the
					// right
					representation
					.get(y)
					.get(x)
					.setInterval(
							new Interval(i2.getLeft(), i2.getRight()
									+ move));
					graph.setInterval(i2.getLeft(), i2.getRight() + move, index);
				} else if (!moveLeftSide && moveLeft) { // move right side to
					// the left
					representation
					.get(y)
					.get(x)
					.setInterval(
							new Interval(i2.getLeft(), i2.getRight()
									- move));
					graph.setInterval(i2.getLeft(), i2.getRight() - move, index);
				} else if (moveLeftSide && moveLeft) { // move left side to the
					// left
					representation
					.get(y)
					.get(x)
					.setInterval(
							new Interval(i2.getLeft() - move, i2
									.getRight()));
					graph.setInterval(i2.getLeft() - move, i2.getRight(), index);
				} else if (moveLeftSide && !moveLeft) {// move left side to the
					// right
					representation
					.get(y)
					.get(x)
					.setInterval(
							new Interval(i2.getLeft() + move, i2
									.getRight()));
					graph.setInterval(i2.getLeft() + move, i2.getRight(), index);
				}
			}
		}
	}

	/**
	 * Checking for intervals that need to be expanded and add them to
	 * toExpandOrDecrease
	 * 
	 * @param move
	 *            - difference after moving
	 * @param y
	 *            - line in representation
	 * @param x
	 *            - interval on line y.
	 * @param moveLeftSide
	 *            - if true move left side of interval, false move right side
	 * @param moveLeft
	 *            - if true move to the left, false move to the right
	 */
	private boolean extendOrDecreaseIntersections(int move, int y, int x,
			boolean moveLeftSide, boolean moveLeft) {
		toExpandOrDecrease = new ArrayList<DrawableInterval>();
		Interval current = representation.get(y).get(x).getInterval();
		for (int i = 0; i < representation.size(); i++) {
			if (i != y) { // skip line we are working on, no intersections here
				for (int j = 0; j < representation.get(i).size(); j++) {
					Interval i2 = representation.get(i).get(j).getInterval();
					if (shouldExtend(current, i2, moveLeftSide, moveLeft)) {
						if (!moveLeftSide && !moveLeft) { // move right side to
							// the right
							toExpandOrDecrease
							.add(representation.get(i).get(j));
							System.out.println("MoveRight");
						} else if (!moveLeftSide && moveLeft) { // move right
							// side to the
							// left
							if (i2.getLeft() >= i2.getRight() - move
									|| i2.getRight() - move < current.getLeft()) { // cannot
								// move
								toExpandOrDecrease = null;
								return false;
							}
							toExpandOrDecrease
							.add(representation.get(i).get(j));
						} else if (moveLeftSide && moveLeft) { // move left side
							// to the left
							if (i2.getLeft() - move <= 0) {
								toExpandOrDecrease = null;
								return false;
							}
							toExpandOrDecrease
							.add(representation.get(i).get(j));
						} else if (moveLeftSide && !moveLeft) {// move left side
							// to the right
							if (i2.getLeft() + move >= i2.getRight()
									|| i2.getLeft() + move >= current
									.getRight()) { // cannot move
								toExpandOrDecrease = null;
								return false;
							}
							toExpandOrDecrease
							.add(representation.get(i).get(j));
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Checks if the interval should be expanded or decreased in the particular
	 * case.
	 * 
	 * @param i
	 *            - the interval that is being changed
	 * @param i2
	 *            - the interval that should be checked
	 * @param moveLeftSide
	 *            - true: moveLeftSide, false: moveRightSide
	 * @param moveLeft
	 *            - true: move to the left, false: move to the right.
	 * @return - true if i2 should be expanded or decreased, else false.
	 */
	private boolean shouldExtend(Interval i, Interval i2, boolean moveLeftSide,
			boolean moveLeft) {
		int i2Left = i2.getLeft(), i2Right = i2.getRight();
		if (!moveLeftSide && !moveLeft) {
			return i2Left < i.getLeft() && i2Right > i.getLeft();
		} else if (!moveLeftSide && moveLeft) {
			return i2Right > i.getLeft() && i2Left < i.getLeft();
		} else if (moveLeftSide && !moveLeft) {
			return i2Right > i.getRight() && i2Left < i.getRight();
		} else if (moveLeftSide && moveLeft) {
			return i2Right > i.getRight();
		}
		return false;
	}

	/**
	 * transforms intervalGraph to simplegraph
	 */
	public static SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> sg; // dangerous!

	public void toSimpleGraph() {
		BasicGraph bg = graph.getGraph();
		SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> sg = bg
				.getSimpleGraph(new DefaultEdgeFactory<DefaultVertex>());
		this.sg = sg;
	}

	/**
	 * Packs the intervals, ready to be drawed. Then disables packing.
	 * 
	 * @return list of lists of DrawableInterval
	 */
	private void pack() {
		packing = false;
		ArrayList<Interval> intervals = graph.getIntervals();
		Collections.sort(intervals);
		representation = new ArrayList<ArrayList<DrawableInterval>>();
		for (int l = 0; l < intervals.size(); l++) {
			Interval i = intervals.get(l);
			boolean added = false;
			for (int j = 0; j < representation.size(); j++) {
				ArrayList<DrawableInterval> row = representation.get(j);
				if (!row.get(row.size() - 1).getInterval().overlaps(i)) {
					row.add(new DrawableInterval(i, j * scale)); // 100x scale
					added = true;
					break;
				}
			}
			if (!added) {
				ArrayList<DrawableInterval> row = new ArrayList<DrawableInterval>();
				row.add(new DrawableInterval(i, representation.size() * scale)); // 100x
				// scale
				representation.add(row);
			}
		}
	}

	public IntervalGraph getGraph() {
		return graph;
	}

	public View getView() {
		return view;
	}

	/**
	 * Toggles between drawMode and moveMode
	 * 
	 * @return
	 */
	public boolean toggleDraw() {
		moveMode = !moveMode;
		activity.shortToast(moveMode ? "Move mode."
				: "Drag to create vertices.");
		marked.clear(); // marking not available in moveMode
		redraw();
		return moveMode;
	}

	/**
	 * Change between packing modes
	 * 
	 * @return boolean packing after change
	 */
	public boolean togglePack() {
		packing = !packing;
		return packing;
	}

	public void trashCan(int mode) {
		TRASH_CAN = mode;
		redraw();
	}

	public Matrix getTransformMatrix() {
		return view.getTransformMatrix();
	}

	/**
	 * Sends parameters to IntervalView and calls redraw of screen.
	 */
	public void redraw() {
		if (graph == null) {
			return;
		}
		if (packing)
			pack();
		view.redraw(info, graph, representation, marked, touchedVertex);
	}

	/**
	 * Returns the closest DrawableInterval within the given radius of the
	 * coordinate
	 * 
	 * @return the closest interval or null if no such exists
	 * @param c
	 *            coordinate
	 * @param radius
	 */
	public DrawableInterval getClosestInterval(Coordinate c, float radius) {
		if (representation == null)
			return null;
		float bestDistance = radius;
		DrawableInterval bestInterval = null;
		for (ArrayList<DrawableInterval> row : representation) {
			for (DrawableInterval current : row) {
				Coordinate posl = current.getLeft();
				Coordinate posr = current.getRight();
				float currentDistance;
				// when inside interval, decide only on Y-coordinate
				if (c.getX() >= posl.getX() && c.getX() <= posr.getX()
						&& Math.abs(c.getY() - posl.getY()) < bestDistance) {
					bestInterval = current;
					bestDistance = Math.abs(c.getY() - posl.getY());
				}
				// when not inside interval
				if (posl.distance(c) <= posr.distance(c)) // decide which side
					// of interval
					currentDistance = posl.distance(c);
				else
					currentDistance = posr.distance(c);
				if (currentDistance < bestDistance) {
					bestInterval = current;
					bestDistance = currentDistance;
				}
			}
		}
		return bestInterval;
	}

	public void shake() {
		togglePack();
		redraw();
	}

	/**
	 * Find the key of the given Interval in the IntervalGraph
	 * 
	 * @param o
	 * @return
	 */
	private int getIntervalIndex(Interval o) {
		int ret = -1;
		for (int i : graph.getVertices()) {
			if (graph.getInterval(i).equals(o)) {
				ret = i;
			}
		}
		return ret;
	}

	/**
	 * Increases all the y's from given position in representation Used when
	 * drawing an interval in the middle of two existing intervals.
	 * 
	 * @param fromPos
	 */
	private void updateYs(int fromPos) {
		for (int i = fromPos; i < representation.size(); i++) {
			for (DrawableInterval j : representation.get(i)) {
				j.setY(j.getLeft().getY() + scale);
			}
		}
	}

	/**
	 * Returns the coordinate the given point/coordinate on the screen
	 * represents in the graph
	 */
	public Coordinate translateCoordinate(Coordinate screenCoordinate) {

		float[] screenPoint = { screenCoordinate.getX(),
				screenCoordinate.getY() };
		Matrix invertedTransformMatrix = new Matrix();

		view.getTransformMatrix().invert(invertedTransformMatrix);
		invertedTransformMatrix.mapPoints(screenPoint);

		return new Coordinate(screenPoint[0], screenPoint[1]);
	}

	/**
	 * Translates the given coordinate to a legal left side int of an interval
	 * 
	 * @param a
	 *            - true: round up, false round down.
	 */
	private int leftCoordinateTranslation(Coordinate c, boolean a) {
		int from = Math.round(c.getX() / scale);
		if (from % 2 == 0) {
			if (a)
				from = from + 1;
			else
				from = from - 1;
		}
		if (from > 0)
			return from;
		return -1;
	}

	/**
	 * Translates the given coordinate to a legal right side int of an interval
	 * 
	 * @param a
	 *            - true: round up, false round down.
	 */
	private int rightCoordinateTranslation(Coordinate c, boolean a) {
		int to = Math.round(c.getX() / scale);
		if (to % 2 != 0) {
			if (a)
				to = to + 1;
			else
				to = to - 1;
		}
		if (to > 0)
			return to;
		return -1;
	}

	/**
	 * Translates the given coordinate to a legal y-coordinate
	 * 
	 * @param c
	 * @return
	 */
	private int translateY(Coordinate c) {
		int y = Math.round(c.getY() / scale);
		if (y + 1 > 0)
			return y + 1;
		return -1;
	}

	/**
	 * Gesture listener to read input from screen.
	 * 
	 * @author Olav Wegner Eide
	 *
	 */
	private class PrivateGestureListener extends SimpleOnGestureListener {
		/** This vertex was touch, e.g. for scrolling and moving purposes */

		/** This is set to the coordinate of the vertex we started move */
		private Coordinate startCoordinate = null;
		private int previousPointerCount = 0;
		private Coordinate[] previousPointerCoords = null;

		/**
		 * Check for hits onDown when moving intervals. If hit, the interval is
		 * deleted from graph and representation, and stored in touchedVertex.
		 * Interval is restored to the old position if not moved, or if the mode
		 * is accidently changed.
		 */
		public boolean onDown(MotionEvent e) {
			trashCan(0);
			Coordinate sCoordinate = new Coordinate(e.getX(), e.getY());
			Coordinate gCoordinate = translateCoordinate(sCoordinate);
			previousPointerCount = -1; // make any ongoing scroll restart
			touchedVertex = getClosestInterval(gCoordinate, USER_MISS_RADIUS);
			if(e.getPointerCount()>1)
				onScroll=true;
			if(e.getPointerCount()==1)
				onScroll=false;
			if (e.getPointerCount() == 1 && moveMode && touchedVertex != null) {
				int y = Math.round(touchedVertex.getLeft().getY() / scale);
				if (y < 0)
					y = 0;
				if (y > representation.size() - 1)
					y = representation.size() - 1;
				if (y > -1 && y < representation.size()) {
					int x = representation.get(y).indexOf(touchedVertex);
					if (x > -1) {
						moving=true;
						int index = getIntervalIndex(touchedVertex.getInterval());
						oldX = x;
						oldY = y;
						graph.deleteVertex(index);
						representation.get(y).remove(x);
						System.out.println("Delete: Y: " + y + " x: " + x);
						touchedVertex.setMoveCoordinate(sCoordinate);
						redraw();
					} else
						touchedVertex = null;
				}
			} else
				touchedVertex = null;
			return super.onDown(e);
		}
		private boolean onScroll=false;

		public Coordinate clearCoordinate() {
			Coordinate ret = startCoordinate;
			startCoordinate = null;
			return ret;
		}


		private void moveAllExceptX(int y, int x){
			ArrayList<DrawableInterval> newList = new ArrayList<DrawableInterval>(representation.get(y));
			ArrayList<DrawableInterval> old = new ArrayList<DrawableInterval>();
			old.add(representation.get(y).get(x));
			representation.set(y, old);
			newList.remove(x); //moved intervals
			if(y+1<representation.size())
				representation.add(y+1,newList);
			else
				representation.add(newList);
			updateYs(y+1);
		}

		/**
		 * Draw new intervals and increase/decrease existing ones
		 */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			float dist = (float) Math.round(Math.sqrt((velocityX * velocityX)
					+ (velocityY * velocityY)));
			float y1 = e1.getY(), y2=e2.getY();
			if (Math.abs(y2-y1) > 20 ||e1.getPointerCount()!=1 || e2.getPointerCount() != 1){
				System.out.println("2fingers");
				return false;
			}	
			trashCan(0);
			Coordinate startCoordinate = new Coordinate(e1.getX(), e1.getY());
			Coordinate endCoordinate = new Coordinate(e2.getX(), e2.getY());
			Coordinate transStart = translateCoordinate(startCoordinate);
			Coordinate transStop = translateCoordinate(endCoordinate);
			DrawableInterval hit = getClosestInterval(transStart,
					USER_MISS_RADIUS);
			if (!moveMode && hit != null && !moving) { // increase decrease an
				// interval

				// moveAllIntersections(int move, int y, int x,boolean
				// moveLeftSide,boolean moveLeft)

				Coordinate posl = hit.getLeft();
				Coordinate posr = hit.getRight();
				float distanceL = posl.distance(transStart);
				float distanceR = posr.distance(transStart);
				Interval i = hit.getInterval();
				int index = getIntervalIndex(i); // since we have hit, no need
				// to check if index>0
				int y = Math.round(hit.getLeft().getY() / scale);
				int x = representation.get(y).indexOf(hit);
				// move left side of interval
				if (distanceL < distanceR && distanceL < USER_MISS_RADIUS) {
					int newLeft, oldLeft = i.getLeft(), move;

					System.out.println("moveLeftSide:");
					if (transStart.getX() > transStop.getX()) {
						newLeft = leftCoordinateTranslation(transStop, false);
						move = Math.abs(oldLeft - newLeft);
						if (newLeft < oldLeft) {// move to the left
							// if(!deShiftLeft(move, y, x) ||
							// !extendOrDecreaseIntersections(move, y,
							// x,true,true)){
							// return true;
							// }
							// doDeShifting(move);
							// doExpandingOrDecreasing(move,true,true);
							representation.get(y).get(x).setInterval(new Interval(newLeft, i.getRight()));
							graph.setInterval(newLeft, i.getRight(), index);
							if (overLappingLine(y,x,new Interval(i.getLeft() - move, i.getRight()))) {
								moveAllExceptX(y, x);
								//							if (x > 0) {
								//									int end = x;
								//									System.out.println(end);
								//									ArrayList<DrawableInterval> line = new ArrayList<DrawableInterval>(
								//											representation.get(y).subList(0,
								//													end));
								//									representation.add(y + 1, line);
								//									updateYs(y + 1);
								//									representation.get(y).subList(0, end)
								//											.clear();
							}
							System.out.println("moveLeft");
							//							}
						} else
							return true;
					} else {// move to the right
						newLeft = leftCoordinateTranslation(transStop, true);
						move = Math.abs(oldLeft - newLeft);
						if (newLeft > oldLeft && newLeft < i.getRight()) {
							// if(!extendOrDecreaseIntersections(move, y,
							// x,true,false) || !shiftLeft(move,y,x))
							// return true;
							// doExpandingOrDecreasing(move,true,false);
							// doShifting(move);
							representation
							.get(y)
							.get(x)
							.setInterval(
									new Interval(newLeft, i.getRight()));
							;
							graph.setInterval(newLeft, i.getRight(), index);

							System.out.println("moveRight");

						} else
							return true;
					}
				}
				// move right side
				else if (distanceL > distanceR && distanceR < USER_MISS_RADIUS) {
					int newRight, oldRight = i.getRight(), move;
					System.out.println("moveRightSide");
					if (transStart.getX() > transStop.getX()) {// move to the
						// left
						newRight = rightCoordinateTranslation(transStop, false);
						move = Math.abs(oldRight - newRight);
						if (newRight < oldRight && newRight > i.getLeft()) {
							System.out.println("moveLeft");
							representation
							.get(y)
							.get(x)
							.setInterval(
									new Interval(i.getLeft(), i
											.getRight() - move));
							graph.setInterval(i.getLeft(), i.getRight() - move,
									index);
							// System.out.println("A:" + deShiftRight(move, y,
							// x) + " B: " +extendOrDecreaseIntersections(move,
							// y, x,false,true));
							// if(!extendOrDecreaseIntersections(move, y,
							// x,false,true) || !deShiftRight(move, y, x)){
							// return true;
							// }
							// doDeShifting(move);
							// doExpandingOrDecreasing(move,false,true);
							// representation.get(y).get(x).setInterval(new
							// Interval(i.getLeft(),newRight));
							// graph.setInterval(i.getLeft(),newRight,index);
						} else
							return true;
					} else {// move to the right
						newRight = rightCoordinateTranslation(transStop, true);
						move = Math.abs(oldRight - newRight);
						if (newRight > oldRight && newRight > 0) {
							System.out.println("moveRight");
							representation
							.get(y)
							.get(x)
							.setInterval(
									new Interval(i.getLeft(), i
											.getRight() + move));
							graph.setInterval(i.getLeft(), i.getRight() + move,
									index);
							if (overLappingLine(y, x,
									new Interval(i.getLeft(), i.getRight()
											+ move))) {
								//								int end = representation.get(y).size();
								//								ArrayList<DrawableInterval> line = new ArrayList<DrawableInterval>(
								//										representation.get(y).subList(x + 1,
								//												end));
								//								representation.add(y + 1, line);
								//								updateYs(y + 1);
								//								representation.get(y).subList(x + 1, end)
								//										.clear();
								moveAllExceptX(y, x);
							}

							// shiftRight(move, y, x);
							// extendOrDecreaseIntersections(move, y,
							// x,false,false);
							// doExpandingOrDecreasing(move,false,false);
							// representation.get(y).get(x).setInterval(new
							// Interval(i.getLeft(),newRight));
							// graph.setInterval(i.getLeft(),newRight,index);
						} else
							return true;
					}
				}
				redraw();
				return true;
			} else if (!moveMode && !onScroll && e2.getPointerCount() ==1 && e1.getPointerCount()==1) { // draw new interval
				// scale used
				int from = leftCoordinateTranslation(transStart, false), to = rightCoordinateTranslation(
						transStop, true);
				// add new element to current y
				ArrayList<DrawableInterval> newList = new ArrayList<DrawableInterval>();
				int y = translateY(transStop);
				newList.add(new DrawableInterval(new Interval(from, to), y
						* scale));

				if (from > 0 && to > 0 && from != to && y > 0) {
					if (y < representation.size()
							&& representation.get(y) == null) {
						graph.addInterval(from, to);
						representation.add(y, newList);
						System.out.println("available space: from " + from
								+ " to " + to + " y " + y);
						redraw();
						return true;
					} else if (y < representation.size()
							&& representation.get(y) != null) {
						graph.addInterval(from, to);
						representation.add(y, newList);
						updateYs(y + 1);
						System.out.println("add in the middle: from " + from
								+ " to " + to + " y " + y);
						redraw();
						return true;
					} else if (y > representation.size()) {
						y = representation.size();
						ArrayList<DrawableInterval> newList2 = new ArrayList<DrawableInterval>();
						newList2.add(new DrawableInterval(
								new Interval(from, to), y * scale));
						graph.addInterval(from, to);
						representation.add(newList2);
						System.out.println("add to end: from " + from + " to "
								+ to + " y " + y);
						redraw();
						return true;
					}
					System.out.println("Did not add");
				}
			}
			return true;
		}

		/**
		 * Mark/unmark a vertex by tapping it
		 */
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			trashCan(0);
			if (!moveMode) { // mark vertices

				Coordinate sCoordinate = new Coordinate(e.getX(), e.getY());
				Coordinate gCoordinate = translateCoordinate(sCoordinate);

				DrawableInterval hit = getClosestInterval(gCoordinate,
						USER_MISS_RADIUS);

				if (hit == null) { // did not find any vertex
					redraw();
					return true;
				} else {// found one.
					if (marked.contains(hit))
						marked.remove(hit);

					else
						marked.add(hit);
				}
			}

			redraw();
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			return true;
		}

		/**
		 * Change mode when holding button
		 */
		public void onLongPress(MotionEvent e) {

			System.out.println("long press, moving: " + moving);
			if (touchedVertex == null) {
				trashCan(0);
				vibrator.vibrate(50);
				//				touchedVertex = null;
				toggleDraw();
			}

			//			}
		}

		/**
		 * Controlls moving and zoom
		 */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			switch (e2.getPointerCount()) {
			case 2: // two fingers, zoom
				trashCan(0);
				if (previousPointerCoords == null || previousPointerCount != 2) {
					previousPointerCoords = new Coordinate[2];
					previousPointerCoords[0] = new Coordinate(e2.getX(0),
							e2.getY(0));
					previousPointerCoords[1] = new Coordinate(e2.getX(1),
							e2.getY(1));
				} else {
					Coordinate[] newCoords = {
							new Coordinate(e2.getX(0), e2.getY(0)),
							new Coordinate(e2.getX(1), e2.getY(1)) };
					Coordinate VectorPrevious = previousPointerCoords[1]
							.subtract(previousPointerCoords[0]);
					Coordinate VectorNew = newCoords[1].subtract(newCoords[0]);
					float scale = VectorNew.length() / VectorPrevious.length();

					// the transformations
					view.getTransformMatrix().postTranslate(
							-previousPointerCoords[0].getX(),
							-previousPointerCoords[0].getY());
					view.getTransformMatrix().postScale(scale, scale);
					view.getTransformMatrix().postTranslate(
							newCoords[0].getX(), newCoords[0].getY());
					previousPointerCoords = newCoords;
				}
				break;
			case 1: // one finger move //move intervals around/delete
				if (moveMode) {
					moving = true;
					previousPointerCoords = null;
					if (touchedVertex != null) {

						if (startCoordinate == null) {
							startCoordinate = touchedVertex.getMoveCoordinate();
						}

						trashCan(1);

						Coordinate sCoordinate = new Coordinate(e2.getX(),
								e2.getY());

						if (view.isOnTrashCan(sCoordinate)) {
							trashCan(2);
						} else {
							trashCan(1);

						}

						Coordinate gCoordinate = translateCoordinate(sCoordinate);
						touchedVertex.setMoveCoordinate(sCoordinate);

					}
				}
				else
					return true;
				break;
			default: // 3 or more
				trashCan(0);
				previousPointerCoords = null;
				previousPointerCount = e2.getPointerCount();
				return false;
			}
			previousPointerCount = e2.getPointerCount();
			redraw();
			return true;
		}
	}
}