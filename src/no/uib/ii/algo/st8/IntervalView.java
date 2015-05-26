package no.uib.ii.algo.st8;

import java.util.ArrayList;

import no.uib.ii.algo.st8.interval.IntervalGraph;
import no.uib.ii.algo.st8.util.Coordinate;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

/**
 * Class for drawing intervals, marking and moving of intervals on screen.
 * 
 * @author Olav Wegner Eide
 *
 */
public class IntervalView extends View {

	private IntervalGraph graph;
	private ArrayList<ArrayList<DrawableInterval>> intervalList;
	private ArrayList<DrawableInterval> marked = new ArrayList<DrawableInterval>();

	private String info = "";
	private int scale = 100; // scale from interval to coordinates

	private Matrix transformMatrix = new Matrix();
	private final Matrix prev = new Matrix();
	// paint
	private final Paint vertexPaint = new Paint();
	private final Paint markedPaint = new Paint();
	// trashcan
	private final Bitmap trashBitmap = BitmapFactory.decodeResource(
			getResources(), R.drawable.trash64x64);
	private final Bitmap trashRedBitmap = BitmapFactory.decodeResource(
			getResources(), R.drawable.trash_red64x64);
	private final Bitmap trashBgRedBitmap = BitmapFactory.decodeResource(
			getResources(), R.drawable.red_bg);
	private final Paint trashPaint = new Paint();
	private boolean trashCoordinatesSet = false;
	private int trashX = -1;
	private int trashY = -1;

	private DrawableInterval touchedVertex;

	public IntervalView(Context context) {
		super(context);
		setFocusable(true);
		markedPaint.setColor(Color.RED);
	}

	public Matrix getTransformMatrix() {
		return transformMatrix;
	}

	public void redraw(String info, IntervalGraph graph,
			ArrayList<ArrayList<DrawableInterval>> intervalList,
			ArrayList<DrawableInterval> marked, DrawableInterval touchedVertex) {
		this.info = info;
		this.graph = graph;
		this.intervalList = intervalList;
		this.marked = marked;
		this.touchedVertex = touchedVertex;
		invalidate(); // calls drawing
	}

	public boolean isOnTrashCan(Coordinate c) {
		int x = (int) c.getX();
		int y = (int) c.getY();
		return (x >= trashX && x <= trashX + trashBitmap.getWidth()
				&& y >= trashY && y <= trashY + trashBitmap.getHeight());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (graph == null)
			return;

		if (!trashCoordinatesSet) {
			if (getWidth() > 0 && getHeight() > 0) {
				trashX = (getWidth() - trashBitmap.getWidth()) / 2;
				trashY = getHeight() - trashBitmap.getHeight() - 10;
				trashCoordinatesSet = true;
			}
		}

		Matrix m = canvas.getMatrix();
		prev.set(m);
		m.preConcat(transformMatrix);
		canvas.setMatrix(m);

		// set paint style
		vertexPaint.setStrokeWidth(8);
		// vertexPaint.setStyle(Paint.Style.STROKE);
		markedPaint.setStrokeWidth(4);
		markedPaint.setStyle(Paint.Style.STROKE);

		// do drawing:
		if (intervalList != null) {
			for (ArrayList<DrawableInterval> row : intervalList) {
				for (DrawableInterval di : row) {
					drawInterval(canvas, di.getLeft().getX(), di.getLeft()
							.getY(), di.getRight().getX());
				}
			}
		}
		if (marked != null) {
			for (DrawableInterval di : marked) {
				drawMarking(canvas, di.getLeft().getX(), di.getLeft().getY(),
						di.getRight().getX());
			}
		}
		if (touchedVertex != null)
			drawMovingInterval(canvas);
		canvas.setMatrix(prev);

		if (IntervalViewController.TRASH_CAN == 1) {
			canvas.drawBitmap(trashBitmap, trashX, trashY, trashPaint);
		} else if (IntervalViewController.TRASH_CAN == 2) {
			canvas.drawBitmap(trashBgRedBitmap, trashX - 46, trashY - 46,
					trashPaint);
			canvas.drawBitmap(trashRedBitmap, trashX, trashY, trashPaint);
		}
		
		writeInfo(canvas);
	}

	/**
	 * Help for onDraw, draws the intervals
	 * 
	 * @param canvas
	 * @param fromX
	 * @param fromY
	 * @param toX
	 */
	private void drawInterval(Canvas canvas, float fromX, float fromY, float toX) {
		canvas.drawLine(fromX, fromY, toX, fromY, vertexPaint);
		// draw vertical side lines
		canvas.drawLine(fromX, fromY - 10, fromX, fromY + 10, vertexPaint);
		canvas.drawLine(toX, fromY - 10, toX, fromY + 10, vertexPaint);
	}

	/**
	 * Help for onDraw. Draws the marking of an Interval
	 * 
	 * @param canvas
	 * @param fromX
	 * @param fromY
	 * @param toX
	 */
	private void drawMarking(Canvas canvas, float fromX, float fromY, float toX) {
		// hor
		canvas.drawLine((float) (fromX + 0.5), fromY - 1, (float) (toX - 0.5),
				fromY - 1, markedPaint); // hor over
		canvas.drawLine((float) (fromX + 0.5), fromY + 1, (float) (toX - 0.5),
				fromY + 1, markedPaint); // hor under
		// ver left
		canvas.drawLine(fromX - 1, fromY - 10, fromX - 1, fromY + 10,
				markedPaint);
		canvas.drawLine(fromX + 1, fromY - 1, fromX + 1, fromY - 10,
				markedPaint); // ver lr
		canvas.drawLine(fromX + 1, fromY + 1, fromX + 1, fromY + 10,
				markedPaint); // ver lr
		// top, bottom left
		canvas.drawLine((float) (fromX - 1.5), fromY - 10,
				(float) (fromX + 1.5), fromY - 10, markedPaint); // hor l top
		canvas.drawLine((float) (fromX - 1.5), fromY + 10,
				(float) (fromX + 1.5), fromY + 10, markedPaint); // hor l bottom
		// ver right
		canvas.drawLine(toX + 1, fromY - 10, toX + 1, fromY + 10, markedPaint);
		canvas.drawLine(toX - 1, fromY - 10, toX - 1, fromY - 1, markedPaint); // ver
																				// lr
		canvas.drawLine(toX - 1, fromY + 1, toX - 1, fromY + 10, markedPaint); // ver
																				// lr
		// top, bottom right
		canvas.drawLine((float) (toX - 1.5), fromY - 10, (float) (toX + 1.5),
				fromY - 10, markedPaint); // hor l top
		canvas.drawLine((float) (toX - 1.5), fromY + 10, (float) (toX + 1.5),
				fromY + 10, markedPaint); // hor l bottomW
	}

	/**
	 * Draws the moving interval
	 * 
	 * @param canvas
	 */
	private void drawMovingInterval(Canvas canvas) {
		if (touchedVertex != null) {
			Coordinate a = touchedVertex.getMoveCoordinate() ;
			Coordinate c = translateCoordinate(a);
			float fromX = c.getX()- ((touchedVertex.getInterval().length() * scale)/2);
			float fromY = c.getY();
			float toX =   c.getX()+ ((touchedVertex.getInterval().length() * scale)/2);
			drawInterval(canvas,fromX,fromY,toX);
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

		this.getTransformMatrix().invert(invertedTransformMatrix);
		invertedTransformMatrix.mapPoints(screenPoint);

		return new Coordinate(screenPoint[0], screenPoint[1]);
	}

	/**
	 * write info to canvas top corner
	 * 
	 * @param canvas
	 */
	private void writeInfo(Canvas canvas) {
		Paint textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
		canvas.drawText(info, 10, 10, textPaint);
	}
}
