package no.uib.ii.algo.st8.start;

import no.uib.ii.algo.st8.StandardEdgeConfiguration;
import no.uib.ii.algo.st8.StandardVertexConfiguration;

public class GraphExporter {

	// TODO zero-pad
	// int red = Color.red(c.getColor());
	// int green = Color.green(c.getColor());
	// int blue = Color.blue(c.getColor());

	public static String getMetapost(
			VisualGraph<StandardVertexConfiguration, StandardEdgeConfiguration> graph) {
		String res = "";

		res += "%Metapostified\n\n";
		res += "pair n[];\n";
		res += "numeric skalering;\n\n";
		res += "skalering := 0.3;";
		res += "vardef drawvertex(expr i) =\n";
		res += "    dotlabel.urt(decimal i,skalering*n[i]);\n";
		res += "enddef;\n\n";

		res += "vardef drawedge (expr inn,ut) =\n";
		res += "    draw (skalering*inn) -- (skalering*ut);\n";
		res += "enddef;\n\n";

		for (UnVertex v : graph.getVertices()) {
			StandardVertexConfiguration c = graph.getVertexConfiguration(v);
			res += "n" + v.getLabel() + " = (" + c.getCoordinate().getX() + ","
					+ c.getCoordinate().getY() + ");\n";
		}
		res += "\n";
		for (UnEdge e : graph.getEdges()) {
			res += "drawedge(n" + e.getSource().getLabel() + ", n"
					+ e.getTarget().getLabel() + ");\n";
		}

		res += "for i = 1 step 1 until 11:\n";
		res += "    drawvertex(i);\n";
		res += "endfor;\n";

		return res;

	}

	private static String getBeginFigure() {
		String res = "";
		res += "\\begin{figure}\n\t\\centering";
		return res;
	}

	private static String getEndFigure(
			VisualGraph<StandardVertexConfiguration, StandardEdgeConfiguration> graph) {
		String info = graph.graphInfo();
		String infop = info.replaceAll(" ", "-").replaceAll(",", "-")
				.replaceAll("\\.", "-").toLowerCase();
		String res = "";
		res += "\n\t\\caption{" + info + "}\n";
		res += "\t\\label{fig:" + infop + "}\n";
		res += "\\end{figure}\n";
		return res;
	}

	public static String getTikz(
			VisualGraph<StandardVertexConfiguration, StandardEdgeConfiguration> graph) {
		String res = "\n\t\\begin{tikzpicture}";
		res += "[every node/.style={circle, draw, scale=.6}, scale=1.0, rotate = 180]\n\n";

		for (UnVertex v : graph.getVertices()) {
			StandardVertexConfiguration c = graph.getVertexConfiguration(v);

			res += "\t\t\\node (" + v.getLabel() + ") at";
			res += " ( " + c.getCoordinate().getX() / 50f + ", "
					+ +c.getCoordinate().getY() / 50f + ")";
			res += " {};\n";
		}
		res += "\n";
		for (UnEdge e : graph.getEdges()) {
			res += "\t\t\\draw (" + e.getSource().getLabel() + ") -- ("
					+ e.getTarget().getLabel() + ");\n";
		}
		res += "\n";
		res += "\t\\end{tikzpicture}";

		return getBeginFigure() + res + getEndFigure(graph);

	}
}
