[![Build](https://github.com/barakugav/JGAlgo/actions/workflows/build.yaml/badge.svg)](https://github.com/barakugav/JGAlgo/actions/workflows/build.yaml)
[![Coverage](https://github.com/barakugav/JGAlgo/blob/coverage/badges/jacoco.svg?raw=true)](https://github.com/barakugav/JGAlgo/tree/coverage)
[![Javadoc](https://img.shields.io/badge/JavaDoc-Online-green)](https://barakugav.github.io/JGAlgo)
[![Benchmarks](https://github.com/barakugav/JGAlgo/actions/workflows/benchmarks.yaml/badge.svg)](https://github.com/barakugav/JGAlgo/actions/workflows/benchmarks.yaml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jgalgo/jgalgo/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jgalgo/jgalgo)


# JGAlgo

The <b>*J*</b>ava <b>*G*</b>raph <b>*Algo*</b>rithm library is a high-performance library for graph algorithms written in Java. It contains a wide collection of optimized algorithms and data structures for a range of problems on graphs. From calculating shortest paths and maximum flows to computing minimum spanning trees, maximum matchings, vertex covers, and minimum coloring.
The library runs on Java 11 or higher, and it is installed using [Maven](https://central.sonatype.com/artifact/com.jgalgo/jgalgo).

JGAlgo offer [unparalleled speed](https://github.com/barakugav/JGAlgo/actions/workflows/benchmarks.yaml) and efficiency by implementing algorithms with theoretically guaranteed running times using the most efficient underlying building blocks and data-structures. A few concrete reasons for the library performance are:
- All building blocks of the library are primitives, rather than Objects
- The underlying [Graph](https://barakugav.github.io/JGAlgo/0.5.0/com/jgalgo/graph/Graph.html) implementations and algorithms do not use costly hash maps, only plain primitive arrays, yielding faster query time, smaller memory footprint and better cache hit rate
- Extensive use of [fastutil](https://fastutil.di.unimi.it/) for all collections
- Memory allocations are postpone and reused by algorithms objects

**Notice:** This project is still under active development and does not guarantee a stable API.

* [Documentation](https://barakugav.github.io/JGAlgo)
* [Website](https://www.jgalgo.com/)

If you are passionate about graph algorithms and data structure, come help develop the most performant competitive graph library in Java! There are still many real-world problems not addressed by the library, and newer or better algorithms not implemented yet.

### Quick Start

Add the following lines to your `pom.xml`:
```
<dependency>
	<groupId>com.jgalgo</groupId>
	<artifactId>jgalgo-core</artifactId>
	<version>0.5.0</version>
</dependency>
```


The most basic object in the library is a [Graph](https://barakugav.github.io/JGAlgo/0.5.0/com/jgalgo/graph/Graph.html). A graph consist of vertices and edges (directed or undirected) connecting between pairs of vertices, all represented by some hashable objects. Algorithms such as [shortest path algorithm](https://barakugav.github.io/JGAlgo/0.5.0/com/jgalgo/alg/shortestpath/ShortestPathSingleSource.html) accept a graph as an input and perform some computation on it. Here is a snippet creating an undirected graph representing the roads between cities in Germany, and computing the shortest path from a source city to all others with respect to a weight function:

```java
/* Create an undirected graph with three vertices and edges between them */
Graph<String, Integer> g = Graph.newUndirected();
g.addVertex("Berlin");
g.addVertex("Leipzig");
g.addVertex("Dresden");
g.addEdge("Berlin", "Leipzig", 9);
g.addEdge("Berlin", "Dresden", 13);
g.addEdge("Dresden", "Leipzig", 14);

/* Assign some weights to the edges */
WeightsDouble<Integer> w = g.addEdgesWeights("distance-km", double.class);
w.set(9, 191.1);
w.set(13, 193.3);
w.set(14, 121.3);

/* Calculate the shortest paths from Berlin to all other cities */
ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
ShortestPathSingleSource.Result<String, Integer> ssspRes = ssspAlgo.computeShortestPaths(g, w, "Berlin");

/* Print the shortest path from Berlin to Leipzig */
System.out.println("Distance from Berlin to Leipzig is: " + ssspRes.distance("Leipzig"));
System.out.println("The shortest path from Berlin to Leipzig is:");
for (Integer e : ssspRes.getPath("Leipzig").edges()) {
	String u = g.edgeSource(e), v = g.edgeTarget(e);
	System.out.println(" " + e + "(" + u + ", " + v + ")");
}
```
