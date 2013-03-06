package no.uib.ii.algo.st8;

import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultVertex;
import no.uib.ii.algo.st8.util.Coordinate;

import org.jgrapht.graph.SimpleGraph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;

public class GraphView extends View {

	private SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph;
	private String info = "";
	private Matrix transformMatrix = new Matrix();

	private final Matrix prev = new Matrix();

	public GraphView(Context context) {
		super(context);

		System.out.println("done!?!");

		System.out.println("GraphView initialized");

		setFocusable(true);
	}

	public Matrix getTransformMatrix() {
		return transformMatrix;
	}

	public void redraw(String info, SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		this.info = info;
		this.graph = graph;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (graph == null)
			return;

		Matrix m = canvas.getMatrix();
		prev.set(m);
		m.preConcat(transformMatrix);
		canvas.setMatrix(m);

		// setBackgroundColor(Color.WHITE);

		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
			DefaultVertex v1 = e.getSource();
			DefaultVertex v2 = e.getTarget();

			Coordinate c1 = v1.getCoordinate();
			Coordinate c2 = v2.getCoordinate();

			float x1 = Math.round(c1.getX() / 10) * 10;
			float y1 = Math.round(c1.getY() / 10) * 10;
			float x2 = Math.round(c2.getX() / 10) * 10;
			float y2 = Math.round(c2.getY() / 10) * 10;

			// Coordinate ce = e.getCoordinate();

			Paint edgePaint = new Paint();
			edgePaint.setColor(e.getColor());
			edgePaint.setStrokeWidth(2);
			edgePaint.setStyle(Paint.Style.STROKE);

			// if (ce != null) {
			// // TODO what's the purpose of an edge's coordinate?
			// // do we want a curve going through ce?

			if (v1.getLabel() == "selected") {
				x1 -= 3;
				y1 -= 3;
			}
			if (v2.getLabel() == "selected") {
				x2 -= 3;
				y2 -= 3;
			}

			canvas.drawLine(x1, y1, x2, y2, edgePaint);
		}

		Paint vertexPaint = new Paint();
		Paint shadow = new Paint();
		shadow.setColor(Color.DKGRAY);

		for (DefaultVertex v : graph.vertexSet()) {
			Coordinate c = v.getCoordinate();
			float x = Math.round(c.getX() / 10) * 10;
			float y = Math.round(c.getY() / 10) * 10;

			// this should be vertex.isSelected() / highlighted etc.
			if (v.getLabel().equals("selected")) {
				canvas.drawCircle(x + 3, y + 3, v.getSize(), shadow);

				x -= 3;
				y -= 3;
			}

			vertexPaint.setStrokeWidth(2);
			vertexPaint.setStyle(Style.STROKE);
			vertexPaint.setColor(Color.BLACK);
			canvas.drawCircle(x, y, v.getSize(), vertexPaint);

			vertexPaint.setStrokeWidth(1);
			vertexPaint.setStyle(Style.FILL);

			vertexPaint.setColor(v.getColor());
			canvas.drawCircle(x, y, v.getSize(), vertexPaint);

			vertexPaint.setStyle(Style.FILL_AND_STROKE);
			vertexPaint.setColor(Color.WHITE);
			if (v.getId() > 9)
				canvas.drawText("" + v.getId(), x - 7, y + 4, vertexPaint);
			else
				canvas.drawText("" + v.getId(), x - 4, y + 4, vertexPaint);
		}

		canvas.setMatrix(prev);
		writeInfo(canvas);
	}

	private void writeInfo(Canvas canvas) {
		Paint textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
		canvas.drawText(info, 10, 10, textPaint);
	}

}
