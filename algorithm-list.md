# Algorithms

| Algorithm | Running time | notes |
| - | - | - |
| SSSP Dijkstra positive weights | $O(m + n \log n)$ | |
| SSSP Dial positive integer weights | $O(m + D)$ | where $D$ is the maximum distance |
| SSSP BellmanFord general weights | $O(m n)$ | |
| SSSP Goldberg positive and negative integer weights | $O(m \sqrt{n} \log N)$ | where $N$ is the minimum negative weight |
| SSSP DAG | $O(n + m)$ |
| APSP Floyd-Warshall general weights | $O(n^3)$ | |
| APSP Johnson general weights | $O(m n + n^2 \log n)$ | |
| A* | $O(b^d)$ | where $b$ is the branching factor and $d$ is the depth, $O(m \log n)$ worst case |
| Max flow EdmondsKarp | $O(m^2 n)$ | |
| Max flow Push/Relabel (FIFO order) | $O(n^3)$ | with global relabeling and gap heuristics |
| Max flow Push/Relabel-to-Front | $O(n^3)$ | with global relabeling and gap heuristics |
| Max flow Push/Relabel Highest First | $O(n^2 \sqrt{m})$ | with global relabeling and gap heuristics |
| Max flow Push/Relabel Lowest First | $O(n^2 m)$ | with global relabeling and gap heuristics |
| Max flow Push/Relabel using Dynamic Trees | $O\left(m n \log \left(\frac{n^2}{m}\right)\right)$ | |
| Max flow Dinic | $O(m n^2)$ | |
| Max flow Dinic using Dynamic Trees | $O(m n \log n)$ | |
| Minimum Cut S-T using any maximum flow algorithm | $O(MF)$ | where $MF$ is the running time of the maximum flow algorithm |
| Minimum Cut Global Stoer-Wagner | $O(m n + n^2 \log n)$ | |
| MST Boruvka | $O(m \log n)$ | |
| MST Kruskal | $O(m \log n)$ | |
| MST Prim | $O(m + n \log n)$ | |
| MST Yao | $O(m \log \log n + n \log n)$ | |
| MST Fredman-Tarjan | $O(m \log^* n)$ | |
| MST Karger-Klein-Tarjan randomized | $O(n + m)$ | expected  |
| MDST Tarjan directed graphs | $O(m \log n)$ | |
| Tree path maxima (TMP) Hagerup | $O(n + m)$ | where $m$ is the number of queries |
| Maximum matching bipartite unweighted Hopcroft-Karp | $O(m \sqrt{n})$ | |
| Maximum matching general unweighted Gabow1976 with Union-Find | $O(m n \cdot \alpha (m,n))$ | where $\alpha(\cdot, \cdot)$ is the inverse Ackermann func |
| Maximum matching bipartite weighted using SSSP | $O(m n + n^2 \log n)$ | |
| Maximum matching bipartite weighted Hungarian method with heaps | $O(m n + n^2 \log n)$ | |
| Maximum matching general weighted Gabow1990 implementation with dynamic LCA | $O(m n + n^2 \log n)$ | |
| Minimum perfect matching general weighted Blossom V | $O(m n^3)$ | best in practice |
| Traveling Salesman Problem (TSP) $2$-appx using MST | $O(n^2)$ | |
| Traveling Salesman Problem (TSP) $3/2$-appx using maximum matching | $O(n^3)$ | |
| Vertex Coloring Greedy (random vertices order) | $O(n + m)$ | assuming the number of colors is constant |
| Vertex Coloring DSatur | $O(m \log n)$ | assuming the number of colors is constant |
| Vertex Coloring Recursive Largest First | $O(m n)$ | |
| Tarjan Cycles Finder | $O((m + n) (c + 1))$ | where $c$ is the number of simple cycles |
| Johnson Cycles Finder | $O((m + n) (c + 1))$ | where $c$ is the number of simple cycles |
| Minimum Mean Cycle Howard | $O(m N)$ | where $N$ is product of the out-degrees of all the vertices. Perform well in practice |
| Minimum Mean Cycle Dasdan-Gupta | $O(m n)$ | |
| Vertex Cover Ben Yehuda | $O(n + m)$ | |
| Connected components calculation (undirected), strongly connected (directed) | $O(n + m)$ | |
| Bi-Connected components calculation (undirected) | $O(n + m)$ | |
| Topological sort calculation (DAG) | $O(n + m)$ |
| Euler Tour calculation | $O(n + m)$ |


# Data Structures


| Algorithm | Running time | notes |
| - | - | - |
| Binary heap | $O(m \log n)$ | where $n$ and $m$ are the number of elements and operations |
| Binomial heap | $O(m \log n)$ | |
| Fibonacci heap | $O(m \log n)$ | decrease key in $O(1)$ |
| Pairing heap | $O(m \log n)$ | decrease key in $O(\log \log n)$, very good pointer based heap in practice |
| Red-Black tree | $O(m \log n)$ | supported extensions: sub tree size/max/min |
| Splay Tree | $O(m \log n)$ | efficient splits and joins |
| Dynamic Tree using Splay trees | $O(m \log n)$ | supported extensions: node's tree size (for push/relabel with dynamic trees) |
| Union Find | $O(m \cdot \alpha(m,n))$ | where $\alpha(\cdot, \cdot)$ is the inverse Ackermann func |
| LCA static by reduction to RMQ | $O(n + m)$ | |
| LCA dynamic Gabow (without bit tricks) | $O(n \log^2 n + m)$ | |
| LCA dynamic Gabow linear (using RAM model) | $O(n + m)$ | |
| Split Find Min | $O(m \log n)$ | |
| Subtree Merge Findmin | $O(m + n \log n)$ | used in general weighted matching |
| RMQ static $\pm 1$ by reduction to LCA | $O(n)$ preprocessing, $O(1)$ query | |
| RMQ static using Cartesian trees | $O(n)$ preprocessing, $O(1)$ query | |
| Bits Lookup tables (bit count, ith bit, ctz) | $O(n)$ preprocessing, $O(1)$ query | |
