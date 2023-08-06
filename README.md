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

If you are passionate about graph algorithms and data structure, come help develop the most performant competitive graph library in Java! There are still many real-world problems not addressed by the library, and newer or better algorithms not implemented yet.

### Quick Start

Add the following lines to your `pom.xml`:
```
<dependency>
	<groupId>com.jgalgo</groupId>
	<artifactId>jgalgo-core</artifactId>
	<version>0.2.0</version>
</dependency>
```

## Graph API

The most basic object in the library is a [Graph](https://barakugav.github.io/JGAlgo/com/jgalgo/Graph.html). A graph consist of vertices and edges (directed or undirected) connecting between pairs of vertices, all represented by `int` primitive IDs. Algorithms such as [shortest path algorithm](https://barakugav.github.io/JGAlgo/com/jgalgo/ShortestPathSingleSource.html) accept a graph as an input and perform some computation on it. Here is a snippet creating a directed graph with three vertices and edges with real values weights, and computing the shortest paths from a source vertex:

```java
/* Create a directed graph with three vertices and edges between them */
Graph g = GraphFactory.newDirected().newGraph();
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

## Algorithms
The `JGAlgo` library offers a diverse collection of graph algorithms that operate on a `Graph` object as input. From finding the shortest paths and maximum flows to computing minimum spanning trees, vertex covers, and cycle detection, the library provides a comprehensive set of optimized algorithms solving these problems.

### Shortest Path
The Shortest Path family of algorithms deals with finding the shortest paths between vertices in a graph, where 'shortest' is defined with respect to a weight function that assign a real value to each edge, and the 'length' (or 'weight') of a path is its edge weights sum.
-   **Single Source (SSSP)**: Computes the shortest paths from a given source vertex to all other vertices in the graph using algorithms like Dijkstra's or Dial's algorithms for positive weights and Bellman-Ford's or Goldberg's algorithms if some of the weights are negative. It provides the shortest path distances and paths from the source vertex to all other vertices.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/ShortestPathSingleSource.html)
-   **All Pairs (APSP)**: Calculates the shortest paths between all pairs of vertices in the graph. It provides the shortest paths and distances between all pairs of vertices. It can also be run on a subset of vertices and compute the shortest paths between each pair of vertices contained in the subset.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/ShortestPathAllPairs.html)
-   **S-T with Heuristic**: Finds the shortest path from a specified source vertex `S` to a target vertex `T`, utilizing a heuristic to optimize the search.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/ShortestPathWithHeuristic.html)

### Matching
Matching is a set of edges without common vertices.
-   **Maximum Matching**: Computes the maximum cardinality matching in an undirected graph.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/MatchingAlgorithm.html)
-   **Maximum/Minimum Weighted (Perfect) Matching**: Determines the maximum/minimum-weighted (optionally perfect) matching in an undirected graph using algorithms like the Blossom algorithm.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/MatchingAlgorithm.html)

### Flow
Flow network is mapping of each directed edge in a graph to capacity and flow amount values. The amount of flow on an edge cannot exceed the capacity of the edge. An amount of flows along edges entering a vertex should be equal to the amount of flows along edges exiting it, except special vertices such as source and sink.
Algorithms of flow problem solve questions such as 'what is maximum flow that can be passed through the network from a source(s) to a sink(s)?'.
-   **Maximum Flow**: Calculates the maximum flow in a network graph using algorithms like the Edmonds-Karp or Push-Relabel. It determines the maximum amount of flow that can be sent from a source(s) vertex to a sink(s) vertex through the network while respecting the capacity constraints of the edges.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/MaximumFlow.html)
-   **Minimum Cost Flow**: Solves the minimum cost flow problem in a flow network using algorithms like the Cycle canceling, Cost scaling or Network Simplex. It finds the flow with the minimum total cost while satisfying the flow constraints and capacity constraints of the edges. Either a maximum flow with minimum cost is computed between a source(s) and a sink(s), or a signed supply function define for each vertex a real value representing the amount of flow it should provide/demand (negative value).
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/MinimumCostFlow.html)

### Minimum Spanning Tree (MST)
The Minimum Spanning Tree algorithms find the spanning tree (edges subset that maintain the connectivity of the graph) with the minimum weight with respect to a given edge weight function. Both  [undirected ](https://barakugav.github.io/JGAlgo/com/jgalgo/MinimumSpanningTree.html) and [directed ](https://barakugav.github.io/JGAlgo/com/jgalgo/MinimumDirectedSpanningTree.html) graphs are supported.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/MinimumSpanningTree.html)

### Cores
Given a graph `G=(V,E)`, a subgraph `H` induced by a subset of vertices `W` is a `k`-core if it is the maximum subgraph in which every vertex has a degree of at least `k`. The core number of vertex is the highest order of a core that contains this vertex. A cores algorithm can compute the core of a specific `k`, or the core number of the vertices.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/CoreAlgo.html)

### Cliques
Finds all maximal cliques in an undirected graph using algorithms like the Bron-Kerbosch algorithm. A clique is a set of vertices where every pair of vertices is adjacent, and a maximal clique is a clique that cannot be extended by adding more vertices.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/MaximalCliques.html)

### Coloring
The Coloring family of algorithms is concerned with finding the minimum number of colors needed to color the vertices of an undirected graph such that no adjacent vertices share the same color.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/Coloring.html)

### Graph Traversal
Traversal algorithms are used to explore the vertices and edges of a graph in a systematic way.
-   **Breadth-First Search (BFS)**: Performs a breadth-first search traversal of the graph starting from a specified source vertex. It visits all vertices reachable from the source vertex level by level. Backward BFS iterator is also supported, namely a BFS that use the reverse edges of a directed graph.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/BFSIter.html)
-   **Depth-First Search (DFS)**: Executes a depth-first search traversal of the graph starting from a specified source vertex. It explores as far as possible along each branch before backtracking.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/DFSIter.html)

### Connectivity
The connectivity of a graph is the ability of reaching from one vertex to another use the (maybe directed) edges of the graph.
-   **(Strongly) Connected Components**: Finds the connected components in a undirected graph, or the strongly connected components in a directed graph. A strongly connected component is a subgraph where any two vertices are reachable from each other.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/ConnectedComponentsAlgo.html)
-   **Weakly Connected Components**: Identifies the connected components in a directed graph as if the edges were undirected.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/ConnectedComponentsAlgo.html)
- **Bi-Connectivity**: Determines the bi-connected of an undirected graph. A bi-connected component is a maximal connected subset of vertices for which if any single one of the vertices would be removed, the component were still connected.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/BiConnectedComponentsAlgo.html)

### Vertex Cover
Computes the minimum (weighted) vertex cover in an undirected graph using algorithms like Bar-Yehuda. A vertex cover is a set of vertices such that every edge in the graph is incident to at least one vertex in the set.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/VertexCover.html)

### Cycles
Cycles algorithms involves finding cycles in a graph.
-   **Minimum Mean Cycle**: Finds the cycle in the graph with the minimum mean edge weight with respect to some weight function.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/MinimumMeanCycle.html)
-   **Cycle Iteration**: Iterate over the simple cycle in a directed graph using algorithms like the Tarjan or Johnson's cycle finding algorithm.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/CyclesFinder.html)

### Cuts
Given a graph, a cut is a partition of the vertices into two sets. The weight of a cut is sum of weights of the edges with endpoints that do not lies in the same set of the partition.
-   **Minimum Cut S-T**: Computes the minimum cut between two specified vertices `S` and `T` using algorithms like the Stoer-Wagner algorithm.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/MinimumCutST.html)
-   **Global Minimum Cut**: Finds the minimum cut that partitions the graph into two disjoint sets with the smallest sum of edge weights.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/MinimumCutGlobal.html)

### Lowest Common Ancestor (LCA)
Given a tree, the least common ancestor of two vertices is the common ancestor of both vertices (contained in the paths from the vertices to the root) and is the furthest from the root (lowest).
- **Static**: Preprocesses the tree to allow efficient LCA queries. The static LCA algorithm uses Euler tour and RMQ (Range Minimum Query) to find the lowest common ancestor of two vertices in a tree efficiently.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/LowestCommonAncestorStatic.html)
- **Dynamic**: support efficient queries of LCA while allowing adding new leaves to the tree.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/LowestCommonAncestorDynamic.html)

### Other
- **Eulerian Tour**: Finds an Eulerian tour (a cycle that visits every edge exactly once) in a graph if one exists.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/EulerianTourAlgo.html)
- **Topological Order Computation**: Determines a topological order of the vertices in a directed acyclic graph (DAG).
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/TopologicalOrderAlgo.html)
- **Chinese Postman Problem**: Solves the Chinese Postman Problem to find the shortest closed walk that traverses each edge at least once with respect to a weight function.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/ChinesePostman.html)
- **Traveling Salesman Problem Approximation**: Approximates the Traveling Salesman Problem to find a near-optimal tour that visits all vertices and returns to the starting point. It uses algorithms like Christofide's algorithm that uses perfect minimum matching.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/TSPMetricMatchingAppx.html)
- **Tree Path Maxima**: Given a tree and a set of queries, each consisting of a pair of vertices, find the maximal weighted edge along the paths of each pair of query vertices in linear time.
[[javadoc]](https://barakugav.github.io/JGAlgo/com/jgalgo/TreePathMaxima.html)
