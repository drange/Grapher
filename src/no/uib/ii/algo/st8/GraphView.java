package no.uib.ii.algo.st8;

import no.uib.ii.algo.st8.start.Coordinate;
import no.uib.ii.algo.st8.start.UnEdge;
import no.uib.ii.algo.st8.start.UnVertex;
import no.uib.ii.algo.st8.start.VisualGraph;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class GraphView extends View {

	private VisualGraph<StandardVertexConfiguration, StandardEdgeConfiguration> graph;
	private String info = "";

	public GraphView(Context context) {
		super(context);
		System.out.println("done!?!");

		System.out.println("GraphView initialized");

		setFocusable(true);
	}

	public void redraw(
			String info,
			VisualGraph<StandardVertexConfiguration, StandardEdgeConfiguration> graph) {
		this.info = info;
		this.graph = graph;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (graph == null)
			return;
		for (UnEdge e : graph.getEdges()) {
			Coordinate c1 = graph.getVertexConfiguration(e.getSource())
					.getCoordinate();
			Coordinate c2 = graph.getVertexConfiguration(e.getTarget())
					.getCoordinate();
			Paint p = new Paint();
			p.setColor(Color.WHITE);
			StandardEdgeConfiguration ec = graph.getEdgeConfiguration(e);
			if (ec != null)
				p.setColor(ec.getColor());
			canvas.drawLine(c1.getX(), c1.getY(), c2.getX(), c2.getY(), p);
		}

		for (UnVertex v : graph.getVertices()) {
			StandardVertexConfiguration config = graph
					.getVertexConfiguration(v);
			Coordinate c = config.getCoordinate();
			Paint p = new Paint();
			p.setColor(config.getColor());
			canvas.drawCircle(c.getX(), c.getY(), config.getSize(), p);
		}
		writeInfo(canvas);
	}

	private void writeInfo(Canvas canvas) {
		Paint p = new Paint();
		p.setColor(Color.WHITE);
		canvas.drawText(info, 10, 10, p);
	}

}
