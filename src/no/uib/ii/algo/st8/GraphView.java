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
import android.view.View;

public class GraphView extends View {

	private SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph;
	private String info = "";
	private Paint p = new Paint();
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

		setBackgroundColor(Color.WHITE);

		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
			Coordinate c1 = e.getSource().getCoordinate();
			Coordinate c2 = e.getTarget().getCoordinate();

			float x1 = Math.round(c1.getX() / 10) * 10;
			float y1 = Math.round(c1.getY() / 10) * 10;
			float x2 = Math.round(c2.getX() / 10) * 10;
			float y2 = Math.round(c2.getY() / 10) * 10;

			// Coordinate ce = e.getCoordinate();

			p.setColor(Color.BLACK);
			// p.setColor(e.getColor());

			// if (ce != null) {
			// // TODO what's the purpose of an edge's coordinate?
			// // do we want a curve going through ce?
			canvas.drawLine(x1, y1, x2, y2, p);
		}

		for (DefaultVertex v : graph.vertexSet()) {
			Coordinate c = v.getCoordinate();
			float x = Math.round(c.getX() / 10) * 10;
			float y = Math.round(c.getY() / 10) * 10;
			p.setColor(v.getColor());
			canvas.drawCircle(x, y, v.getSize(), p);
		}

		canvas.setMatrix(prev);
		writeInfo(canvas);
	}

	private void writeInfo(Canvas canvas) {
		p.setColor(Color.BLACK);
		canvas.drawText(info, 10, 10, p);
	}

}
