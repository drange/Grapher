package no.uib.ii.algo.st8.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultVertex;
import no.uib.ii.algo.st8.util.Coordinate;

import org.jgrapht.graph.SimpleGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileAccess {

	public String save(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph)
			throws JSONException {
		JSONObject json = new JSONObject();
		JSONObject vertices = new JSONObject();
		json.put("vertices", vertices);

		JSONArray edges = new JSONArray();
		json.put("edges", edges);

		Integer vertexNumber = 1;

		for (DefaultVertex v : graph.vertexSet()) {
			JSONObject vertexJson = new JSONObject();
			vertexJson.put("x", (double) v.getCoordinate().getX());
			vertexJson.put("y", (double) v.getCoordinate().getY());
			if (v.getLabel().isEmpty() || vertices.has(v.getLabel())) {
				v.setLabel("n" + vertexNumber);
			}
			vertices.put(v.getLabel(), vertexJson);
			vertexNumber += 1;
		}
		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
			JSONObject edge = new JSONObject();
			edge.put("source", e.getSource().getLabel());
			edge.put("target", e.getTarget().getLabel());
			edges.put(edge);
		}
		return json.toString();
	}

	public void load(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph,
			String json) throws JSONException {

		JSONObject graphJson = new JSONObject(json);
		HashSet<DefaultVertex> verticesSet = new HashSet<DefaultVertex>(
				graph.vertexSet());
		HashSet<DefaultEdge<DefaultVertex>> edgesSet = new HashSet<DefaultEdge<DefaultVertex>>(
				graph.edgeSet());
		graph.removeAllVertices(verticesSet);
		graph.removeAllEdges(edgesSet);

		JSONObject vertices = graphJson.getJSONObject("vertices");
		HashMap<String, DefaultVertex> verticesMap = new HashMap<String, DefaultVertex>();
		for (@SuppressWarnings("rawtypes")
		Iterator i = vertices.keys(); i.hasNext();) {
			String key = (String) i.next();
			JSONObject vertexJson = vertices.getJSONObject(key);
			DefaultVertex vertex = new DefaultVertex(new Coordinate(
					vertexJson.getDouble("x"), vertexJson.getDouble("y")));
			vertex.setLabel(key);
			graph.addVertex(vertex);
			verticesMap.put(key, vertex);
		}

		JSONArray edges = graphJson.getJSONArray("edges");

		for (int i = 0; i < edges.length(); i++) {
			JSONObject edge = edges.getJSONObject(i);
			String source = edge.getString("source");
			String target = edge.getString("target");
			graph.addEdge(verticesMap.get(source), verticesMap.get(target));
		}

	}

}
