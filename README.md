# Grapher


This is an Android app for graph drawing and editing, and comes with several
built-in algorithms.  The project started out as a way to create graphs for TiKZ
by drawing on the phone, but quickly escalated to be a pedagogical tool in graph
theory and graph algorithms.

## Grapher conception

The Algorithms group at the University of Bergen made use of the application in
students' projects, to get theoretical and practical experience (e.g. the
Steiner Tree and Interval layout were both student projects).  In addition, the
students got the satisfaction of having their program in an app available for
anyone to download and use on the Android platform.

Grapher's potential is endless, and patches are indeed most welcome, both for
bug fixes, new algorithm implementations, optimization of already implemented
algorithms and (last but definitely not least) general UI and Android
improvements.  Admittedly, algorithm theoreticians and mathematicians are not
the best worlds best UX developers.


## Key features

1. Creating and modifying graphs with through an intuitive interface
2. Export to tikz and metapost for use in LaTeX
3. Automatic layout (spring layout)
4. Execution of a plethora of classical algorithms, and some admittedly more
   exotic ones
5. Visualization of results of algorithms
6. Undoing, saving a graph for later graphing, and loading it again.

The list of available algorithms includes (but is not limited by)

1. Computing diameter, connectivity, degree statistics, shortest paths, checking
   bipartiteness
2. Solving several NP-complete problems, like Vertex Cover, Independent Set,
   Maximum Clique, Dominating Set, Treewidth, Longest Path, Coloring,
   Triangulaltions, Odd Cycle Transversal, Feedback Vertex Set, Vertex
   Integrity, Hamiltonicity, Bandwidth computation, etc.
3. Max flow, Steiner Tree, Girth, finding cut vertices and bridges
4. Modifications like graph and local complements, contraction, power graph,
   induced subgraphs etc.
5. Sharing and exporting an Interval Representation provided the graph is an
   interval graph


## About Grapher

Grapher is based on JGraphT, and released under the GNU General Public License
version 3 or any later version of GPL.

By the SuperTango8 team, we present to you, _Grapher by Algoritmegruppen_.
