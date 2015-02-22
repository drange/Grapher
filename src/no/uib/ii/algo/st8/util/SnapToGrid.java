package no.uib.ii.algo.st8.util;

import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultVertex;

import org.jgrapht.graph.SimpleGraph;

public class SnapToGrid {

  private static float SQRT3 = (float) Math.sqrt(3);

  private final SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph;

  // TODO should we treat stuff component-wise?
  // private Map<DefaultVertex, Integer> vertexToComponent;

  public SnapToGrid(SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
    this.graph = graph;
    // vertexToComponent = new HashMap<DefaultVertex, Integer>();
  }

  private DefaultVertex arbitrary() {
    if (graph.vertexSet().isEmpty())
      return null;
    return graph.vertexSet().iterator().next();
  }

  public void snap() {
    int n = graph.vertexSet().size();
    if (n == 0)
      return;

    DefaultVertex arb = arbitrary();

    Coordinate cArb = arb.getCoordinate();
    float size = arb.getSize();

    float xMin = cArb.getX();
    float xMax = cArb.getX();
    float yMin = cArb.getY();
    float yMax = cArb.getY();

    for (DefaultVertex v : graph.vertexSet()) {
      xMin = Math.min(xMin, v.getCoordinate().getX());
      xMax = Math.max(xMax, v.getCoordinate().getX());

      yMin = Math.min(yMin, v.getCoordinate().getY());
      yMax = Math.max(yMax, v.getCoordinate().getY());
      System.out.println("pre:  " + v + " " + v.getCoordinate());
    }
    float xDiff = xMax - xMin;
    float yDiff = yMax - yMin;

    for (DefaultVertex v : graph.vertexSet()) {
      Coordinate c = v.getCoordinate();

      float cx = c.getX() - xMin;
      float cy = c.getY() - yMin;

      cx = (cx * 100 * size) / xDiff;
      cy = (cy * 100 * size) / yDiff;

      cx = (Math.round(cx / 200)) * 100;
      cy = (Math.round(cy / 200)) * 100;

      v.setCoordinate(new Coordinate(cx, cy));
      System.out.println("post: " + v + " " + v.getCoordinate());
    }
  }
}
