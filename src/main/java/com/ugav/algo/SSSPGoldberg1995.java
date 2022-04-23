package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

public class SSSPGoldberg1995 implements SSSP {

	/*
	 * O(m * n^0.5 * logN) where N is the minimum negative weight
	 */

	private SSSPGoldberg1995() {
	}

	private static final SSSPGoldberg1995 INSTANCE = new SSSPGoldberg1995();

	public static SSSPGoldberg1995 getInstace() {
		return INSTANCE;
	}

	@Override
	public <E> SSSP.Result<E> calcDistances(Graph<E> g, WeightFunction<E> w0, int source) {
		if (!g.isDirected())
			throw new IllegalArgumentException("Undirected graphs are not supported");
		if (!(w0 instanceof WeightFunctionInt))
			throw new IllegalArgumentException("Only integer weights are supported");
		WeightFunctionInt<E> w = (WeightFunctionInt<E>) w0;

		int minWeight = Integer.MAX_VALUE;
		for (Edge<E> e : g.edges())
			minWeight = Math.min(minWeight, w.weightInt(e));
		if (minWeight >= 0)
			// All weights are positive, use Dijkstra
			return new SSSPDijkstra().calcDistances(g, w, source);

		Pair<int[], List<Edge<E>>> p = calcPotential(g, w, minWeight);
		if (p.e2 != null)
			return new Result<>(source, null, null, p.e2);
		int[] potential = p.e1;
		PotentialWeightFunction<E> pw = new PotentialWeightFunction<>(w, potential);
		SSSP.Result<E> dijkstra = new SSSPDijkstra().calcDistances(g, pw, source);
		return new Result<>(source, potential, dijkstra, null);
	}

	private static <E> Pair<int[], List<Edge<E>>> calcPotential(Graph<E> g, WeightFunctionInt<E> w, int minWeight) {
		int n = g.vertices();
		int[] potential = new int[n];

		boolean[] connected = new boolean[n];
		int[] layerSize = new int[n + 1];

		SSSPDial1969 ssspDial = new SSSPDial1969();

		Graph<E> gNeg = new GraphArray<>(DirectedType.Directed, n);
		Graph<Integer> G = new GraphArray<>(DirectedType.Directed, n);
		int fakeS = G.newVertex();

		int minWeightWordsize = Utils.log2(-minWeight);
		for (int weightMask = minWeightWordsize; weightMask >= 0; weightMask--) {
			if (weightMask != minWeightWordsize)
				for (int v = 0; v < n; v++)
					potential[v] *= 2;

			// updated potential function until there are no more negative vertices with
			// current weight function
			do {
				// Create a graph with edges with weight <= 0
				gNeg.edges().clear();
				for (Edge<E> e : g.edges())
					if (weight(e, w, potential, weightMask) <= 0)
						gNeg.addEdge(e);

				// Find all strong connectivity components in the graph
				Pair<Integer, int[]> pair = Graphs.findStrongConnectivityComponents(gNeg);
				int N = pair.e1.intValue();
				int[] v2V = pair.e2;

				// Contract each strong connectivity component and search for a negative edge
				// within it, if found - negative cycle found
				G.edges().clear();
				for (int u = 0; u < n; u++) {
					int U = v2V[u];
					for (Edge<E> e : Utils.iterable(gNeg.edges(u))) {
						int V = v2V[e.v()];
						int weight = weight(e, w, potential, weightMask);
						if (U != V)
							G.addEdge(U, V).val(Integer.valueOf(weight));
						else if (weight < 0) {
							// negative cycle
							gNeg.removeEdge(e);
							List<Edge<E>> negCycle = new ArrayList<>(Graphs.findPath(gNeg, e.v(), u));
							negCycle.add(e);
							return Pair.valueOf(null, negCycle);
						}
					}
				}

				// Create a fake vertex S, connect with 0 edges to all and calc distances
				for (int U = 0; U < N; U++)
					G.addEdge(fakeS, U).val(Integer.valueOf(0));
				SSSP.Result<Integer> ssspRes = Graphs.calcDistancesDAG(G, Graphs.WEIGHT_INT_FUNC_DEFAULT, fakeS);

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
					G.removeEdgesOut(fakeS);
					int assignedWeight = layerNum - 2;
					for (Edge<Integer> e : ssspRes.getPathTo(vertexInMaxLayer)) {
						if (e.val().intValue() < 0) {
							int V = e.v();
							G.addEdge(fakeS, V).val(Integer.valueOf(assignedWeight--));
							connected[V] = true;
						}
					}
					for (int V = 0; V < N; V++)
						if (!connected[V])
							G.addEdge(fakeS, V).val(Integer.valueOf(layerNum - 1));

					// Add the remaining edges to the graph, not only 0,-1 edges
					for (Edge<E> e : g.edges()) {
						int weight = weight(e, w, potential, weightMask);
						if (weight > 0)
							G.addEdge(v2V[e.u()], v2V[e.v()]).val(Integer.valueOf(weight));
					}

					// Calc distance with abs weight function to update potential function
					ssspRes = ssspDial.calcDistances(G, e -> Math.abs(e.val().intValue()), fakeS, layerNum);
					for (int v = 0; v < n; v++)
						potential[v] += ssspRes.distance(v2V[v]);
				}
			} while (true);
		}

		return Pair.valueOf(potential, null);
	}

	private static <E> int weight(Edge<E> e, WeightFunctionInt<E> w, int[] potential, int weightMask) {
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
		return weight + potential[e.u()] - potential[e.v()];
	}

	private static class Result<E> implements SSSP.Result<E> {

		private final int sourcePotential;
		private final int[] potential;
		private final SSSP.Result<E> dijkstraRes;
		private final List<Edge<E>> cycle;

		Result(int source, int[] potential, SSSP.Result<E> dijkstraRes, List<Edge<E>> cycle) {
			this.sourcePotential = potential != null ? potential[source] : 0;
			this.potential = potential;
			this.dijkstraRes = dijkstraRes;
			this.cycle = cycle != null ? Collections.unmodifiableList(cycle) : null;
		}

		@Override
		public double distance(int v) {
			if (foundNegativeCycle())
				throw new IllegalStateException();
			return dijkstraRes.distance(v) - sourcePotential + potential[v];
		}

		@Override
		public List<Edge<E>> getPathTo(int v) {
			if (foundNegativeCycle())
				throw new IllegalStateException();
			return dijkstraRes.getPathTo(v);
		}

		@Override
		public boolean foundNegativeCycle() {
			return cycle != null;
		}

		@Override
		public List<Edge<E>> getNegativeCycle() {
			if (!foundNegativeCycle())
				throw new IllegalStateException();
			return cycle;
		}

		@Override
		public String toString() {
			return foundNegativeCycle() ? "[NegCycle=" + cycle + "]" : dijkstraRes.toString();
		}

	}

	private static class PotentialWeightFunction<E> implements WeightFunctionInt<E> {

		private final WeightFunctionInt<E> w;
		private final int[] potential;

		PotentialWeightFunction(WeightFunctionInt<E> w, int[] potential) {
			this.w = w;
			this.potential = potential;
		}

		@Override
		public int weightInt(Edge<E> e) {
			return w.weightInt(e) + potential[e.u()] - potential[e.v()];
		}

	}

}
