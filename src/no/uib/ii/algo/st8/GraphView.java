package no.uib.ii.algo.st8;

import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultVertex;
import no.uib.ii.algo.st8.model.EdgeStyle;
import no.uib.ii.algo.st8.util.Coordinate;

import org.jgrapht.graph.SimpleGraph;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.TypedValue;
import android.view.View;

public class GraphView extends View {

  private SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph;
  private String info = "";
  private Matrix transformMatrix = new Matrix();

  private final Matrix prev = new Matrix();

  private final Paint edgePaint = new Paint();
  private final Paint vertexPaint = new Paint();
  private final Paint vertexOutlinePaint = new Paint();
  private final Paint vertexTextPaint = new Paint();
  private final Paint shadowPaint = new Paint();

  private final Bitmap trashBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.trash64x64);
  private final Bitmap trashRedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.trash_red64x64);

  private final Bitmap trashBgRedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.red_bg);

  private final Paint trashPaint = new Paint();

  private boolean trashCoordinatesSet = false;
  private int trashX = -1;
  private int trashY = -1;

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

  public boolean isOnTrashCan(Coordinate c) {
    int x = (int) c.getX();
    int y = (int) c.getY();

    return (x >= trashX && x <= trashX + trashBitmap.getWidth() && y >= trashY && y <= trashY + trashBitmap.getHeight());
  }

  private int getPixels(int dipValue) {
    Resources r = getResources();
    int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
    return px;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
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

    // setBackgroundColor(Color.WHITE);

    edgePaint.setStrokeWidth(2);
    edgePaint.setStyle(Paint.Style.STROKE);

    for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
      DefaultVertex v1 = e.getSource();
      DefaultVertex v2 = e.getTarget();

      Coordinate c1 = v1.getCoordinate();
      Coordinate c2 = v2.getCoordinate();

      float x1 = c1.getX(), y1 = c1.getY(), x2 = c2.getX(), y2 = c2.getY();

      // in case the vertex appears lifted and thus moved 3 px up,left
      if (v1.getLabel() == "selected") {
        x1 -= 3;
        y1 -= 3;
      }
      if (v2.getLabel() == "selected") {
        x2 -= 3;
        y2 -= 3;
      }

      if (e.getStyle() == EdgeStyle.BOLD) {
        edgePaint.setColor(GraphViewController.MARKED_EDGE_COLOR);
        edgePaint.setStrokeWidth(5);
        canvas.drawLine(x1, y1, x2, y2, edgePaint);
        edgePaint.setStrokeWidth(2);
      }
      edgePaint.setColor(e.getColor());
      canvas.drawLine(x1, y1, x2, y2, edgePaint);
    }

    // the inner part of vertex
    vertexPaint.setStrokeWidth(1);
    vertexPaint.setStyle(Style.FILL);

    // The outline of the vertex
    vertexOutlinePaint.setStrokeWidth(5);
    vertexOutlinePaint.setStyle(Style.STROKE);

    vertexTextPaint.setColor(Color.WHITE);

    shadowPaint.setColor(Color.DKGRAY);
    shadowPaint.setAlpha(100); // transparent

    for (DefaultVertex v : graph.vertexSet()) {
      Coordinate c = v.getCoordinate();

      float x = c.getX(), y = c.getY();

      // this should be vertex.isSelected() / highlighted etc.
      if (v.getLabel().equals("selected") || !GraphViewController.EDGE_DRAW_MODE) {
        float[] shadow = new float[] { 1, 1 };

        m.mapVectors(shadow);
        float sx = shadow[0];
        float sy = shadow[1];

        double length = Math.sqrt((sx * sx) + (sy * sy));

        sx = (float) ((length * sx) / Math.abs(length));
        sy = (float) ((length * sy) / Math.abs(length));

        canvas.drawCircle(x + sx, y + sy, v.getSize() + 2, shadowPaint);

        x -= sx;
        y -= sy;
      }

      int vcolor = v.getColor();
      int red = Color.red(vcolor);
      int green = Color.green(vcolor);
      int blue = Color.blue(vcolor);

      vertexPaint.setColor(v.getColor());

      double darken = .8; // Yes, completely arbitrary
      vertexOutlinePaint.setColor(Color.rgb((int) (red * darken), (int) (green * darken), (int) (blue * darken)));
      // draws outline
      canvas.drawCircle(x, y, v.getSize(), vertexOutlinePaint);

      // draws vertex
      canvas.drawCircle(x, y, v.getSize(), vertexPaint);

      // HAHAHA: A global static public variable flag!
      // Talk about nice coding practice!
      if (GraphViewController.DO_SHOW_LABELS) {
        // a hack for now
        if (v.getId() > 9)
          canvas.drawText("" + v.getId(), x - 7, y + 4, vertexTextPaint);
        else
          canvas.drawText("" + v.getId(), x - 4, y + 4, vertexTextPaint);
      }
    }

    canvas.setMatrix(prev);

    if (GraphViewController.TRASH_CAN == 1) {
      canvas.drawBitmap(trashBitmap, trashX, trashY, trashPaint);
    } else if (GraphViewController.TRASH_CAN == 2) {
      canvas.drawBitmap(trashBgRedBitmap, trashX - 46, trashY - 46, trashPaint);
      canvas.drawBitmap(trashRedBitmap, trashX, trashY, trashPaint);
    }

    writeInfo(canvas);
  }

  private void writeInfo(Canvas canvas) {
    Paint textPaint = new Paint();
    textPaint.setColor(Color.BLACK);
    canvas.drawText(info, 10, 10, textPaint);
  }

}
