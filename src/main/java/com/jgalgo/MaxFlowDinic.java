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

public class MaxFlowDinic implements MaxFlow {

	/**
	 * Dinic's max flow algorithm.
	 *
	 * O(m n^2)
	 */

	private Supplier<? extends GraphBuilder> layerGraphFactory = () -> GraphBuilder.newInstance("com.jgalgo.Linked");

	private static final Object EdgeRefWeightKey = new Object();
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	public MaxFlowDinic() {
	}

	public void experimental_setLayerGraphFactory(Supplier<? extends GraphBuilder> factory) {
		layerGraphFactory = Objects.requireNonNull(factory);
	}

	@Override
	public double calcMaxFlow(Graph g, FlowNetwork net, int source, int target) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		return calcMaxFlow0((DiGraph) g, net, source, target);
	}

	private double calcMaxFlow0(DiGraph g0, FlowNetwork net, int source, int target) {
		if (source == target)
			throw new IllegalArgumentException("Source and target can't be the same vertices");

		final int n = g0.vertices().size();
		DiGraph g = new GraphArrayDirected(n);
		Weights.Int edgeRef = g.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		Weights.Int twin = g.addEdgesWeights(EdgeRevWeightKey, int.class, Integer.valueOf(-1));
		Weights.Double flow = g.addEdgesWeights(FlowWeightKey, double.class);
		Weights.Double capacity = g.addEdgesWeights(CapacityWeightKey, double.class);
		for (IntIterator it = g0.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g0.edgeSource(e), v = g0.edgeTarget(e);
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

		GraphBuilder builder = layerGraphFactory.get();
		DiGraph L = builder.setVerticesNum(n).setEdgesIDStrategy(Fixed.class).buildDirected();
		Weights.Int edgeRefL = L.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		IntPriorityQueue bfsQueue = new IntArrayFIFOQueue();
		int[] level = new int[n];

		for (;;) {
			L.clearEdges();

			/* Calc the sub graph non saturated edges from source to target using BFS */
			final int unvisited = Integer.MAX_VALUE;
			Arrays.fill(level, unvisited);
			bfsQueue.clear();
			level[source] = 0;
			bfsQueue.enqueue(source);
			bfs: while (!bfsQueue.isEmpty()) {
				int u = bfsQueue.dequeueInt();
				if (u == target)
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
			if (level[target] == unvisited)
				break; /* All paths to target are saturated */

			searchBlockingFlow: for (;;) {
				IntStack path = new IntArrayList();
				searchAugPath: for (;;) {
					int u = path.isEmpty() ? source : L.edgeTarget(path.topInt());
					EdgeIter eit = L.edgesOut(u);
					if (!eit.hasNext()) {
						if (path.isEmpty()) {
							// no path from source to target
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
					if (eit.v() == target) {
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
