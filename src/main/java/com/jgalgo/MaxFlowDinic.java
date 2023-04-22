package com.jgalgo;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import com.jgalgo.IDStrategy.Fixed;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Dinic's algorithm for maximum flow.
 * <p>
 * The algorithm finds a maximum flow by repetitively finding a blocking flow in
 * the residual network. It runs in {@code O(m n}<sup>2</sup>{@code )} time and
 * use linear space.
 * <p>
 * Based on the paper 'Algorithm for solution of a problem of maximum flow in a
 * network with power estimation' by Y. A. Dinitz (Dinic).
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Dinic%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class MaxFlowDinic implements MaxFlow {

	private Supplier<? extends GraphBuilder> layerGraphBuilder = GraphBuilder.Linked::new;

	private static final Object EdgeRefWeightKey = new Object();
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaxFlowDinic() {
	}

	/**
	 * [experimental API] Set the graph implementation used by this algorithm for
	 * the layers graph.
	 * <p>
	 * Multiple {@code remove} operations are performed on the layers graph,
	 * therefore its non trivial that an array graph implementation should be used,
	 * as linked graph implementation perform {@code remove} operations more
	 * efficiently.
	 *
	 * @param builder a builder that provide instances of graphs for the layers
	 *                graph
	 */
	public void experimental_setLayerGraphFactory(Supplier<? extends GraphBuilder> builder) {
		layerGraphBuilder = Objects.requireNonNull(builder);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public double computeMaximumFlow(Graph g, FlowNetwork net, int source, int sink) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		return computeMaxFlow((DiGraph) g, net, source, sink);
	}

	private double computeMaxFlow(DiGraph g0, FlowNetwork net, int source, int sink) {
		if (source == sink)
			throw new IllegalArgumentException("Source and sink can't be the same vertex");

		final int n = g0.vertices().size();
		DiGraph g = new GraphArrayDirected(n);
		Weights.Int edgeRef = g.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		Weights.Int twin = g.addEdgesWeights(EdgeRevWeightKey, int.class, Integer.valueOf(-1));
		Weights.Double flow = g.addEdgesWeights(FlowWeightKey, double.class);
		Weights.Double capacity = g.addEdgesWeights(CapacityWeightKey, double.class);
		for (IntIterator it = g0.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g0.edgeSource(e), v = g0.edgeTarget(e);
			if (u == v)
				continue;
			if (u == sink || v == source)
				continue;
			int e1 = g.addEdge(u, v);
			int e2 = g.addEdge(v, u);
			edgeRef.set(e1, e);
			edgeRef.set(e2, e);
			twin.set(e1, e2);
			twin.set(e2, e1);
			double cap = net.getCapacity(e);
			capacity.set(e1, cap);
			capacity.set(e2, cap);
			flow.set(e1, 0);
			flow.set(e2, cap);
		}

		GraphBuilder builder = layerGraphBuilder.get();
		DiGraph L = builder.setVerticesNum(n).setEdgesIDStrategy(Fixed.class).buildDirected();
		Weights.Int edgeRefL = L.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		IntPriorityQueue bfsQueue = new IntArrayFIFOQueue();
		int[] level = new int[n];

		for (;;) {
			L.clearEdges();

			/* Calc the sub graph non saturated edges from source to sink using BFS */
			final int unvisited = Integer.MAX_VALUE;
			Arrays.fill(level, unvisited);
			bfsQueue.clear();
			level[source] = 0;
			bfsQueue.enqueue(source);
			bfs: while (!bfsQueue.isEmpty()) {
				int u = bfsQueue.dequeueInt();
				if (u == sink)
					break bfs;
				int lvl = level[u];
				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();
					if (flow.getDouble(e) >= capacity.getDouble(e) || level[v] <= lvl)
						continue;
					edgeRefL.set(L.addEdge(u, v), e);
					if (level[v] != unvisited)
						continue;
					level[v] = lvl + 1;
					bfsQueue.enqueue(v);
				}
			}
			if (level[sink] == unvisited)
				break; /* All paths to sink are saturated */

			searchBlockingFlow: for (;;) {
				IntStack path = new IntArrayList();
				searchAugPath: for (;;) {
					int u = path.isEmpty() ? source : L.edgeTarget(path.topInt());
					EdgeIter eit = L.edgesOut(u);
					if (!eit.hasNext()) {
						if (path.isEmpty()) {
							// no path from source to sink
							break searchBlockingFlow;
						} else {
							// retreat
							int e = path.popInt();
							L.removeEdge(e);
							continue searchAugPath;
						}
					}

					int e = eit.nextInt();
					path.push(e);
					if (eit.v() == sink) {
						// augment
						break searchAugPath;
					} else {
						// advance
					}
				}

				// augment the path we found
				IntList pathList = (IntList) path;
				assert pathList.size() > 0;

				// find out what is the maximum flow we can pass
				double f = Double.MAX_VALUE;
				for (IntIterator it = pathList.iterator(); it.hasNext();) {
					int e = edgeRefL.getInt(it.nextInt());
					f = Math.min(f, capacity.getDouble(e) - flow.getDouble(e));
				}

				// update flow of all edges on path
				for (IntIterator it = pathList.iterator(); it.hasNext();) {
					int eL = it.nextInt();
					int e = edgeRefL.getInt(eL);
					int rev = twin.getInt(e);
					double newFlow = flow.getDouble(e) + f;
					double cap = capacity.getDouble(e);
					if (newFlow < cap) {
						flow.set(e, newFlow);
					} else {
						/* saturated, remove edge */
						flow.set(e, cap);
						L.removeEdge(eL);
					}
					flow.set(rev, Math.max(0, flow.getDouble(rev) - f));
				}
			}
		}

		/* Construct result */
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int eOrig = edgeRef.getInt(e);
			if (g.edgeSource(e) == g0.edgeSource(eOrig))
				net.setFlow(eOrig, flow.getDouble(e));
		}
		double totalFlow = 0;
		for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
			int e = eit.nextInt();
			int eOrig = edgeRef.getInt(e);
			if (g.edgeSource(e) == g0.edgeSource(eOrig))
				totalFlow += flow.getDouble(e);
		}
		for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
			int e = eit.nextInt();
			int eOrig = edgeRef.getInt(e);
			if (g.edgeSource(e) == g0.edgeSource(eOrig))
				totalFlow -= flow.getDouble(e);
		}
		return totalFlow;
	}

}
