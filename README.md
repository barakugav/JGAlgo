[![Tests](https://github.com/barakugav/algo/actions/workflows/tests.yaml/badge.svg)](https://github.com/barakugav/algo/actions/workflows/tests.yaml)

# Algo

Algo is a collection of algorithms implemented in Java. It contains mostly algorithms for various graphs problems and some utilities data structures and array algorithms.

## Algorithms


| Algorithm | Running time |
| - | - |
| Binary heap | $O(m \log n)$ for $m$ operations |
| Binomial heap | $O(m \log n)$ for $m$ operations |
| Fibonacci heap | $O(m \log n)$ for $m$ operations, decrease key in $O(1)$ |
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
