
[![Tests](https://github.com/barakugav/JGAlgo/actions/workflows/tests.yaml/badge.svg)](https://github.com/barakugav/JGAlgo/actions/workflows/tests.yaml)
[![Coverage](https://github.com/barakugav/JGAlgo/blob/coverage/badges/jacoco.svg?raw=true)](https://github.com/barakugav/JGAlgo/tree/coverage)
[![SpotBugs](https://github.com/barakugav/JGAlgo/actions/workflows/spotbugs.yaml/badge.svg)](https://github.com/barakugav/JGAlgo/actions/workflows/spotbugs.yaml)
[![Javadoc](https://img.shields.io/badge/JavaDoc-Online-green)](https://barakugav.github.io/JGAlgo)
[![Benchmarks](https://github.com/barakugav/JGAlgo/actions/workflows/benchmarks.yaml/badge.svg)](https://github.com/barakugav/JGAlgo/actions/workflows/benchmarks.yaml)


# JGAlgo

JGAlgo is a high-performance library for graph algorithms written in Java. It contains a wide collection of optimized algorithms and data structures for a range of problems on graphs. From calculating shortest paths and maximum flows to computing minimum spanning trees, maximum matchings, vertex covers, and minimum coloring.
The library runs on Java 11 or higher, and it is installed using Maven.

JGAlgo offer [unparalleled speed](https://github.com/barakugav/JGAlgo/actions/workflows/benchmarks.yaml) and efficiency by implementing algorithms with theoretically guaranteed running times and using the most efficient underlying building blocks and data-structures. A few concrete reasons for the library performance are:
- All building blocks of the library are primitives, rather than Objects
- The underlying Graph implementations and algorithms do not use costly hash maps, only plain primitive arrays, yielding faster query time, smaller memory footprint and better cache hit rate
- Extensive use of [fastutil](https://fastutil.di.unimi.it/) for all collections
- Memory allocations are postpone and reused by algorithms objects

**Notice:** This project is still under active development and does not guarantee a stable API.

* [Documentation](https://barakugav.github.io/JGAlgo)
* [Website](https://www.jgalgo.com/)

If you are passionate about graph algorithms and data structure, join us develop the most performant competitive graph library in Java! There are still many real-world problems not addressed by the library, and new algorithms invented every year.

### Quick Start

Add the following lines to your `pom.xml`:
```
<dependency>
	<groupId>com.jgalgo</groupId>
	<artifactId>jgalgo-core</artifactId>
	<version>0.1.1.2</version>
</dependency>
```

## Graph API

The most basic object in the library is a [Graph](https://barakugav.github.io/JGAlgo/com/jgalgo/Graph.html). A graph consist of vertices and edges (directed or undirected) connecting between pairs of vertices, all represented by `int` primitive IDs. Algorithms such as [shortest path algorithm](https://barakugav.github.io/JGAlgo/com/jgalgo/ShortestPathSingleSource.html) accept a graph as an input and perform some computation on it. Here is a snippet creating a directed graph with three vertices and edges with real values weights, and computing the shortest paths from a source vertex:

```java
/* Create a directed graph with three vertices and edges between them */
Graph g = Graph.newBuilderDirected().build();
int v1 = g.addVertex();
int v2 = g.addVertex();
int v3 = g.addVertex();
int e1 = g.addEdge(v1, v2);
int e2 = g.addEdge(v2, v3);
int e3 = g.addEdge(v1, v3);

/* Assign some weights to the edges */
Weights.Double w = g.addEdgesWeights("weightsKey", double.class);
w.set(e1, 1.2);
w.set(e2, 3.1);
w.set(e3, 15.1);

/* Calculate the shortest paths from v1 to all other vertices */
ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newBuilder().build();
ShortestPathSingleSource.Result ssspRes = ssspAlgo.computeShortestPaths(g, w, v1);

assert ssspRes.distance(v3) == 4.3;
assert ssspRes.getPath(v3).equals(IntList.of(e1, e2));
System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));

/* Print the shortest path from v1 to v3 */
System.out.println("The shortest path from v1 to v3 is:");
for (int e : ssspRes.getPath(v3)) {
	int u = g.edgeSource(e);
	int v = g.edgeTarget(e);
	System.out.println(" " + e + "(" + u + ", " + v + ")");
}
```
