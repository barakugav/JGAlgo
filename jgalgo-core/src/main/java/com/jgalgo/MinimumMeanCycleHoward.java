package com.jgalgo;

import java.util.Arrays;
import java.util.function.IntPredicate;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Howard's algorithm for minimum mean cycle detection.
 * <p>
 * The algorithm runs in {@code O(m N)} and uses linear space, where {@code N}
 * is product of the out-degrees of all the vertices in the graph. Although this
 * bound is not polynomial, this algorithm perform well in practice. There are
 * other bounds on the time such as {@code O(n m \alpha)} where {@code \alpha}
 * is the number of simple cycles in the graph, or
 * {@code O(n^2 m (MaxW-MinW)/\epsilon)} where {@code MaxW,MinW} are the maximum
 * and minimum edge weight in the graph, and {@code \epsilon} is the precision
 * of the algorithm.
 * <p>
 * Based on 'Efficient Algorithms for Optimal Cycle Mean and Optimum Cost to
 * Time Ratio Problems' by Ali Dasdan, Sandy S. Irani, Rajesh K. Gupta (1999).
 *
 * @author Barak Ugav
 */
public class MinimumMeanCycleHoward implements MinimumMeanCycle {

	private final ConnectivityAlgorithm ccAlg = ConnectivityAlgorithm.newBuilder().build();
	private IntList[] ccVertices = MemoryReuse.EmptyIntListArr;
	private IntList[] ccEdges = MemoryReuse.EmptyIntListArr;
	private double[] d = DoubleArrays.EMPTY_ARRAY;
	private int[] policy = IntArrays.EMPTY_ARRAY;
	private IntPriorityQueue queue;
	private int[] visitIdx = IntArrays.EMPTY_ARRAY;

	private static final double EPS = 0.0001;

	/**
	 * Create a new minimum mean cycle algorithm.
	 */
	public MinimumMeanCycleHoward() {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public Path computeMinimumMeanCycle(Graph g, EdgeWeightFunc w) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		int n = g.vertices().size();

		/* find all SCC */
		ConnectivityAlgorithm.Result cc = ccAlg.computeConnectivityComponents(g);
		int ccNum = cc.getNumberOfCC();
		IntList[] ccVertices = this.ccVertices = MemoryReuse.ensureLength(this.ccVertices, ccNum);
		IntList[] ccEdges = this.ccEdges = MemoryReuse.ensureLength(this.ccEdges, ccNum);
		for (int c = 0; c < ccNum; c++) {
			ccVertices[c] = MemoryReuse.ensureAllocated(ccVertices[c], IntArrayList::new);
			ccEdges[c] = MemoryReuse.ensureAllocated(ccEdges[c], IntArrayList::new);
			assert ccVertices[c].isEmpty();
			assert ccEdges[c].isEmpty();
		}
		for (int u = 0; u < n; u++)
			ccVertices[cc.getVertexCc(u)].add(u);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = cc.getVertexCc(g.edgeSource(e));
			int v = cc.getVertexCc(g.edgeTarget(e));
			if (u == v)
				ccEdges[u].add(e);
		}

		/* init distances and policy */
		double[] d = this.d = MemoryReuse.ensureLength(this.d, n);
		Arrays.fill(d, 0, n, Double.POSITIVE_INFINITY);
		int[] policy = this.policy = MemoryReuse.ensureLength(this.policy, n);
		Arrays.fill(policy, 0, n, -1);
		for (int c = 0; c < ccNum; c++) {
			for (IntIterator it = ccEdges[c].iterator(); it.hasNext();) {
				int e = it.nextInt();
				double ew = w.weight(e);
				int u = g.edgeSource(e);
				if (ew < d[u]) {
					d[u] = ew;
					policy[u] = e;
				}
			}
		}

		IntPriorityQueue queue = this.queue = MemoryReuse.ensureAllocated(this.queue, IntArrayFIFOQueue::new);

		double overallBestCycleMeanWeight = Double.POSITIVE_INFINITY;
		int overallBestCycleVertex = -1;
		int nextSearchIdx = 1;
		int[] visitIdx = this.visitIdx = MemoryReuse.ensureLength(this.visitIdx, n);
		Arrays.fill(visitIdx, 0, n, 0);
		/* operate on each SCC separately */
		for (int c = 0; c < ccNum; c++) {
			if (ccVertices[c].size() < 2)
				continue;
			/* run in iteration as long as we find improvements */
			sccLoop: for (;;) {
				double bestCycleMeanWeight = Double.POSITIVE_INFINITY;
				int bestCycleVertex = -1;

				final int iterationFirstSearchIdx = nextSearchIdx;
				IntPredicate visited = v -> visitIdx[v] >= iterationFirstSearchIdx;
				/* DFS root loop */
				for (IntIterator rootIt = ccVertices[c].iterator(); rootIt.hasNext();) {
					final int root = rootIt.nextInt();
					if (visited.test(root))
						continue;
					final int searchIdx = nextSearchIdx++;

					/* Run DFS from root */
					int cycleVertex;
					for (int v = root;;) {
						visitIdx[v] = searchIdx;
						v = g.edgeTarget(policy[v]);
						if (visited.test(v)) {
							cycleVertex = visitIdx[v] == searchIdx ? v : -1;
							break;
						}
					}

					/* cycle found */
					if (cycleVertex != -1) {

						/* find cycle mean weight */
						double cycleWeight = 0;
						int cycleLength = 0;
						for (int v = cycleVertex;;) {
							int e = policy[v];
							cycleWeight += w.weight(e);
							cycleLength++;

							v = g.edgeTarget(e);
							if (v == cycleVertex)
								break;
						}

						/* compare to best */
						cycleWeight = cycleWeight / cycleLength;
						if (bestCycleMeanWeight > cycleWeight) {
							bestCycleMeanWeight = cycleWeight;
							bestCycleVertex = cycleVertex;
						}
					}
				}
				assert bestCycleVertex != -1;
				if (overallBestCycleMeanWeight > bestCycleMeanWeight) {
					overallBestCycleMeanWeight = bestCycleMeanWeight;
					overallBestCycleVertex = bestCycleVertex;
				}

				/* run a reversed BFS from a vertex in the best cycle */
				final int searchIdx = nextSearchIdx++;
				visitIdx[bestCycleVertex] = searchIdx;
				assert queue.isEmpty();
				queue.enqueue(bestCycleVertex);
				while (!queue.isEmpty()) {
					int v = queue.dequeueInt();
					for (EdgeIter eit = g.edgesIn(v); eit.hasNext();) {
						int e = eit.nextInt();
						int u = g.edgeSource(e);
						if (policy[u] != e || visited.test(u))
							continue;
						/* update distance */
						d[u] += w.weight(e) - bestCycleMeanWeight;

						/* enqueue in BFS */
						visitIdx[u] = searchIdx;
						queue.enqueue(u);
					}
				}

				/* check for improvements */
				boolean improved = false;
				for (IntIterator eit = ccEdges[c].iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int u = g.edgeSource(e);
					int v = g.edgeTarget(e);
					double newDistance = d[v] + w.weight(e) - bestCycleMeanWeight;
					double delta = d[u] - newDistance;
					if (delta > 0) {
						if (delta > EPS)
							improved = true;
						d[u] = newDistance;
						policy[u] = e;
					}
				}
				if (!improved)
					break sccLoop;
			}
		}

		if (overallBestCycleVertex == -1)
			return null;
		IntList cycle = new IntArrayList();
		for (int v = overallBestCycleVertex;;) {
			int e = policy[v];
			cycle.add(e);
			v = g.edgeTarget(e);
			if (v == overallBestCycleVertex)
				break;
		}
		Path result = new Path(g, overallBestCycleVertex, overallBestCycleVertex, cycle);

		for (int c = 0; c < ccNum; c++) {
			ccVertices[c].clear();
			ccEdges[c].clear();
		}
		return result;
	}

}
