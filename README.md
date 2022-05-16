# Algo

Algo is a collection of algorithms implemented in Java. It contains mostly algorithms for various graphs problems and some utilities data structures and array algorithms.

## Algorithms


| Algorithm | Running time |
| - | - |
| Binary heap | O(m log n) for m operations |
| Binomial heap | O(m log n) for m operations |
| Fibonacci heap | O(m log n) for m operations, decrease key in O(1) |
| Red-Black tree (sub tree size/max/min extensions supported) | O(m log n) for m operations |
| Splay Tree | O(m log n) for m operations, efficient splits and joins |
| Union Find | O(m alpha(m,n)) where alpha is inverse Ackermann func |
| Split Find Min | O(m log n) for m operations |
| SSSP Dijkstra positive weights | O(m + n log n) |
| SSSP Dial1969 positive integer weights | O(m + D) where D is the maximum distance |
| SSSP BellmanFord general weights | O(m n) |
| SSSP Goldberg1995 positive and negative integer weights | O(m n^0.5 logN) where N is the minimum negative weight |
| RMQ +-1 BenderFarachColton2000 | O(n) preprocessing, O(1) query |
| RMQ GabowBentleyTarjan1984 | O(n) preprocessing, O(1) query |
| LCA static BenderFarachColton2000, using RMQ | O(n + m) |
| LCA dynamic Gabow simple | O(n log^2 n + m) |
| LCA dynamic Gabow17 | O(n + m) |
| Max flow EdmondsKarp | O(m n^2) |
| MST Boruvka1926 | O(m log n) |
| MST Kruskal1956 | O(m log n) |
| MST Prim1957 | O(m + n log n) |
| MST Yao1976 | O(m log log n + n log n) |
| MST FredmanTarjan1987 | O(m log* n) |
| MST KargerKleinTarjan1995 randomized | O(m + n) expected |
| MDST Tarjan1977 directed graphs | O(m log n) |
| TMP (tree path maxima) Komlos1985King1997Hagerup2009 | O(m + n) where m is the number of queries |
| Maximum matching bipartite unweighted HopcroftKarp1973 | O(m n^0.5) |
| Maximum matching general unweighted Gabow1976 | O(m n alpha(m,n)) |
| Maximum matching bipartite weighted SSSP | O(m n + n^2 log n) |
| Maximum matching bipartite weighted Hungarian method | O(m n + n^2 log n) |
| Maximum matching general weighted Gabow1990 | O(m n logn), WIP for O(m n + n^2 log n) |

### Additional Utils

| Utility | Running time |
| - | - |
| Connectivity components calculation (undirected), strongly connected (directed) | O(m + n) |
| Topological sort calculation (DAG) | O(m + n) |
| SSSP DAG | O(m + n) |
| Array k'th element | O(n) |
| Array bucket partition | O(n log k) where k is the bucket size |
| Bits Lookup tables (bit count, ith bit, ctz) | O(n) preprocessing, O(1) query |
