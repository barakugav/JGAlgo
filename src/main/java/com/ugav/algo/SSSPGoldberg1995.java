package com.ugav.algo;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

public class SSSPGoldberg1995 implements SSSP {

	/*
	 * O(m n^0.5 log N) where N is the minimum negative weight
	 */

	private static final Object EdgeRefWeightKey = new Object();

	public SSSPGoldberg1995() {
	}

	@Override
	public SSSP.Result calcDistances(Graph g0, EdgeWeightFunc w0, int source) {
		if (!(g0 instanceof DiGraph))
			throw new IllegalArgumentException("Undirected graphs are not supported");
		DiGraph g = (DiGraph) g0;
		if (!(w0 instanceof EdgeWeightFunc.Int))
			throw new IllegalArgumentException("Only integer weights are supported");
		EdgeWeightFunc.Int w = (EdgeWeightFunc.Int) w0;

		int minWeight = Integer.MAX_VALUE;
		for (int e = 0; e < g.edgesNum(); e++)
			minWeight = Math.min(minWeight, w.weightInt(e));
		if (minWeight >= 0)
			// All weights are positive, use Dijkstra
			return new SSSPDijkstra().calcDistances(g, w, source);

		Pair<int[], IntList> p = calcPotential(g, w, minWeight);
		if (p.e2 != null)
			return new Result(source, null, null, p.e2);
		int[] potential = p.e1;
		PotentialWeightFunction pw = new PotentialWeightFunction(g, w, potential);
		SSSP.Result dijkstra = new SSSPDijkstra().calcDistances(g, pw, source);
		return new Result(source, potential, dijkstra, null);
	}

	private static Pair<int[], IntList> calcPotential(DiGraph g, EdgeWeightFunc.Int w, int minWeight) {
		int n = g.verticesNum();
		int[] potential = new int[n];

		boolean[] connected = new boolean[n];
		int[] layerSize = new int[n + 1];

		SSSPDial1969 ssspDial = new SSSPDial1969();

		DiGraph gNeg = new GraphArrayDirected(n);
		GraphWeights.Int gNegEdgeRefs = gNeg.edgesWeightsFactory().ints().build(EdgeRefWeightKey);
		DiGraph G = new GraphArrayDirected(n);
		GraphWeights.Int GWeights = G.edgesWeightsFactory().ints().build("weight");
		int fakeS1 = G.addVertex(), fakeS2 = G.addVertex();

		int minWeightWordsize = Utils.log2(-minWeight);
		for (int weightMask = minWeightWordsize; weightMask >= 0; weightMask--) {
			if (weightMask != minWeightWordsize)
				for (int v = 0; v < n; v++)
					potential[v] *= 2;

			// updated potential function until there are no more negative vertices with
			// current weight function
			for (;;) {
				// Create a graph with edges with weight <= 0
				gNeg.clearEdges();
				for (int e = 0; e < g.edgesNum(); e++) {
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					if (weight(g, e, w, potential, weightMask) <= 0)
						gNegEdgeRefs.set(gNeg.addEdge(u, v), e);
				}

				// Find all strong connectivity components in the graph
				Pair<Integer, int[]> pair = Graphs.findStrongConnectivityComponents(gNeg);
				int N = pair.e1.intValue();
				int[] v2V = pair.e2;

				// Contract each strong connectivity component and search for a negative edge
				// within it, if found - negative cycle found
				G.clearEdges();
				for (int u = 0; u < n; u++) {
					int U = v2V[u];
					for (EdgeIter eit = gNeg.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.v();
						int V = v2V[v];
						int weight = weight(g, gNegEdgeRefs.getInt(e), w, potential, weightMask);
						if (U != V) {
							GWeights.set(G.addEdge(U, V), weight);

						} else if (weight < 0) {
							// negative cycle
							IntList negCycle0 = Graphs.findPath(gNeg, v, u);
							negCycle0.add(e);
							IntList negCycle = new IntArrayList(negCycle0.size());
							for (IntIterator it = negCycle0.iterator(); it.hasNext();)
								negCycle.add(gNegEdgeRefs.getInt(it.nextInt()));
							return Pair.of(null, negCycle);
						}
					}
				}

				// Create a fake vertex S, connect with 0 edges to all and calc distances
				for (int U = 0; U < N; U++)
					GWeights.set(G.addEdge(fakeS1, U), 0);
				SSSP.Result ssspRes = Graphs.calcDistancesDAG(G, GWeights, fakeS1);

				// Divide super vertices into layers by distance
				int layerNum = 0;
				int vertexInMaxLayer = -1;
				Arrays.fill(layerSize, 0);
				for (int V = 0; V < N; V++) {
					int l = -(int) ssspRes.distance(V);
					if (l + 1 > layerNum) {
						layerNum = l + 1;
						vertexInMaxLayer = V;
					}
					layerSize[l]++;
				}
				if (layerNum == 1)
					break; // no negative vertices, done

				// Find biggest layer
				int biggestLayer = -1;
				for (int l = layerNum - 1; l > 0; l--)
					if (biggestLayer == -1 || layerSize[l] > layerSize[biggestLayer])
						biggestLayer = l;
				if (layerSize[biggestLayer] >= Math.sqrt(N)) {
					// A layer with sqrt(|V|) was found, decrease potential of layers l,l+1,l+2,...
					for (int v = 0; v < n; v++) {
						int V = v2V[v], l = -(int) ssspRes.distance(V);
						if (l >= biggestLayer)
							potential[v]--;
					}
				} else {
					// No big layer is found, use path which has at least sqrt(|V|) vetices.
					// Connected a fake vertex to all vertices, with edge r-i to negative vertex vi
					// on the path and with edge r to all other vertices
					Arrays.fill(connected, 0, N, false);
					int assignedWeight = layerNum - 2;
					for (IntIterator it = ssspRes.getPathTo(vertexInMaxLayer).iterator(); it.hasNext();) {
						int e = it.nextInt();
						int ew = GWeights.getInt(e);
						if (ew < 0) {
							int V = G.edgeTarget(e);
							GWeights.set(G.addEdge(fakeS2, V), assignedWeight--);
							connected[V] = true;
						}
					}
					for (int V = 0; V < N; V++)
						if (!connected[V])
							GWeights.set(G.addEdge(fakeS2, V), layerNum - 1);

					// Add the remaining edges to the graph, not only 0,-1 edges
					for (int e = 0; e < g.edgesNum(); e++) {
						int u = g.edgeSource(e), v = g.edgeTarget(e);
						int weight = weight(g, e, w, potential, weightMask);
						if (weight > 0)
							GWeights.set(G.addEdge(v2V[u], v2V[v]), weight);
					}

					// Calc distance with abs weight function to update potential function
					ssspRes = ssspDial.calcDistances(G, e -> Math.abs(GWeights.getInt(e)), fakeS2, layerNum);
					for (int v = 0; v < n; v++)
						potential[v] += ssspRes.distance(v2V[v]);
				}
			}
		}

		return Pair.of(potential, null);
	}

	private static int weight(DiGraph g, int e, EdgeWeightFunc.Int w, int[] potential, int weightMask) {
		int weight = w.weightInt(e);
		// weight = ceil(weight / 2^weightMask)
		if (weightMask != 0) {
			if (weight <= 0) {
				weight = -((-weight) >> weightMask);
			} else {
				weight += 1 << (weightMask - 1);
				weight = weight >> weightMask;
				if (weight == 0)
					weight = 1;
			}
		}
		return weight + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];
	}

	private static class Result implements SSSP.Result {

		private final int sourcePotential;
		private final int[] potential;
		private final SSSP.Result dijkstraRes;
		private final IntList cycle;

		Result(int source, int[] potential, SSSP.Result dijkstraRes, IntList cycle) {
			this.sourcePotential = potential != null ? potential[source] : 0;
			this.potential = potential;
			this.dijkstraRes = dijkstraRes;
			this.cycle = cycle != null ? IntLists.unmodifiable(cycle) : null;
		}

		@Override
		public double distance(int v) {
			if (foundNegativeCycle())
				throw new IllegalStateException();
			return dijkstraRes.distance(v) - sourcePotential + potential[v];
		}

		@Override
		public IntList getPathTo(int v) {
			if (foundNegativeCycle())
				throw new IllegalStateException();
			return dijkstraRes.getPathTo(v);
		}

		@Override
		public boolean foundNegativeCycle() {
			return cycle != null;
		}

		@Override
		public IntList getNegativeCycle() {
			if (!foundNegativeCycle())
				throw new IllegalStateException();
			return cycle;
		}

		@Override
		public String toString() {
			return foundNegativeCycle() ? "[NegCycle=" + cycle + "]" : dijkstraRes.toString();
		}

	}

	private static class PotentialWeightFunction implements EdgeWeightFunc.Int {

		private final DiGraph g;
		private final EdgeWeightFunc.Int w;
		private final int[] potential;

		PotentialWeightFunction(DiGraph g, EdgeWeightFunc.Int w, int[] potential) {
			this.g = g;
			this.w = w;
			this.potential = potential;
		}

		@Override
		public int weightInt(int e) {
			return w.weightInt(e) + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];
		}

	}

}
