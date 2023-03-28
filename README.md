[![Tests](https://github.com/barakugav/JGAlgo/actions/workflows/tests.yaml/badge.svg)](https://github.com/barakugav/JGAlgo/actions/workflows/tests.yaml)
[![Benchmarks](https://github.com/barakugav/JGAlgo/actions/workflows/benchmarks.yaml/badge.svg)](https://github.com/barakugav/JGAlgo/actions/workflows/benchmarks.yaml)
[![Coverage](https://github.com/barakugav/JGAlgo/blob/coverage/badges/jacoco.svg?raw=true)](https://github.com/barakugav/JGAlgo/tree/coverage)
[![Javadoc](https://img.shields.io/badge/JavaDoc-Online-green)](https://barakugav.github.io/JGAlgo)


# JGAlgo

JGAlgo is a collection of algorithms implemented in Java. It contains mostly algorithms for various graphs problems and some utilities data structures and array algorithms.

[Documentation](https://barakugav.github.io/JGAlgo)

## Algorithms


| Algorithm | Running time |
| - | - |
| Binary heap | $O(m \log n)$ for $m$ operations |
| Binomial heap | $O(m \log n)$ for $m$ operations |
| Fibonacci heap | $O(m \log n)$ for $m$ operations, decrease key in $O(1)$ |
| Pairing heap | $O(m \log n)$ for $m$ operations, decrease key in $O(\log \log n)$, very good pointer based heap in practice |
| Red-Black tree (sub tree size/max/min extensions supported) | $O(m \log n)$ for $m$ operations |
| Splay Tree | $O(m \log n)$ for $m$ operations, efficient splits and joins |
| Dynamic Tree using Splay trees | $O(m \log n)$ for $m$ operations |
| Union Find | $O(m \cdot \alpha(m,n))$ where $\alpha(\cdot, \cdot)$ is the inverse Ackermann func |
| Split Find Min | $O(m \log n)$ for $m$ operations |
| SSSP Dijkstra positive weights | $O(m + n \log n)$ |
| SSSP Dial1969 positive integer weights | $O(m + D)$ where $D$ is the maximum distance |
| SSSP BellmanFord general weights | $O(m n)$ |
| SSSP Goldberg1995 positive and negative integer weights | $O(m \sqrt{n} \log N)$ where $N$ is the minimum negative weight |
| RMQ $\pm 1$ BenderFarachColton2000 | $O(n)$ preprocessing, $O(1)$ query |
| RMQ GabowBentleyTarjan1984 | $O(n)$ preprocessing, $O(1)$ query |
| LCA static BenderFarachColton2000, using RMQ | $O(n+m)$ |
| LCA dynamic Gabow simple | $O(n \log^2 n + m)$ |
| LCA dynamic Gabow2017 | $O(n+m)$ |
| Max flow EdmondsKarp | $O(m n^2)$ |
| Max flow Push/Relabel | $O(n^3)$ |
| Max flow Push/Relabel using Dynamic Trees | $O\left(m n \log \left(\frac{n^2}{m}\right)\right)$ |
| Max flow Dinic using Dynamic Trees | $O(m n \log n)$ |
| MST Boruvka1926 | $O(m \log n)$ |
| MST Kruskal1956 | $O(m \log n)$ |
| MST Prim1957 | $O(m + n \log n)$ |
| MST Yao1976 | $O(m \log \log n + n \log n)$ |
| MST FredmanTarjan1987 | $O(m \log^* n)$ |
| MST KargerKleinTarjan1995 randomized | $O(n+m)$ expected |
| MDST Tarjan1977 directed graphs | $O(m \log n)$ |
| Tree path maxima (TMP) Komlos1985King1997Hagerup2009 | $O(n+m)$ where $m$ is the number of queries |
| Subtree Merge Findmin (used in general weighted matching) | $O(m + n \log n)$ |
| Maximum matching bipartite unweighted HopcroftKarp1973 | $O(m \sqrt{n})$ |
| Maximum matching general unweighted Gabow1976 | $O(m n \cdot \alpha (m,n))$ |
| Maximum matching bipartite weighted SSSP | $O(m n + n^2 \log n)$ |
| Maximum matching bipartite weighted Hungarian method | $O(m n + n^2 \log n)$ |
| Maximum matching general weighted Gabow2017 | $O(m n + n^2 \log n)$ |
| Travelling Salesman Problem (TSP) $2$-appx using MST | $O(n^2)$ |
| Travelling Salesman Problem (TSP) $3/2$-appx using maximum matching | $O(n^3)$ |

### Additional Utils

| Utility | Running time |
| - | - |
| Connectivity components calculation (undirected), strongly connected (directed) | $O(n+m)$ |
| Topological sort calculation (DAG) | $O(n+m)$ |
| SSSP DAG | $O(n+m)$ |
| Euler Tour calculation | $O(n+m)$ |
| Array k'th element | $O(n)$ |
| Array bucket partition | $O(n \log k)$ where $k$ is the bucket size |
| Bits Lookup tables (bit count, ith bit, ctz) | $O(n)$ preprocessing, $O(1)$ query |

## Graph API

```java
/* Create a directed graph with three vertices and edges between them */
DiGraph g = new GraphArrayDirected();
int v1 = g.addVertex();
int v2 = g.addVertex();
int v3 = g.addVertex();
int e1 = g.addEdge(v1, v2);
int e2 = g.addEdge(v2, v3);
int e3 = g.addEdge(v1, v3);

/* Assign some weights to the edges */
Weights.Double w = g.addEdgesWeight("weightsKey").ofDoubles();
w.set(e1, 1.2);
w.set(e2, 3.1);
w.set(e3, 15.1);

/* Calculate the shortest paths from v1 to all other vertices */
SSSP ssspAlgo = new SSSPDijkstra();
SSSP.Result ssspRes = ssspAlgo.calcDistances(g, w, v1);

/* Display the shortest path from v1 to v3 */
assert ssspRes.distance(v3) == 4.3;
assert ssspRes.getPathTo(v3).equals(IntList.of(e1, e2));
System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
System.out.println("The shortest path from v1 to v3 is:");
for (IntIterator it = ssspRes.getPathTo(v3).iterator(); it.hasNext();) {
	int e = it.nextInt();
	int u = g.edgeSource(e), v = g.edgeTarget(e);
	System.out.println(" " + e + "(" + u + ", " + v + ")");
}
```