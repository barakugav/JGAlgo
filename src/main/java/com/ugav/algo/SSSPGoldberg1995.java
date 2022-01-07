package com.ugav.algo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

public class SSSPGoldberg1995 implements SSSP {

	@Override
	public <E> Result<E> calcDistances(Graph<E> g, WeightFunction<E> w0, int s) {
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
			return SSSPDijkstra.getInstace().calcDistances(g, w, s);

		int n = g.vertices();
		int[] potential = new int[n];

		int minWeightWordsize = Utils.log2(-minWeight);
		for (int i = 0; i < minWeightWordsize; i++) {
			int weightShift = minWeightWordsize - i;

			// Create a graph with edges with weight <= 0
			Graph<E> gNeg = new GraphArray<>(DirectedType.Directed, n);
			for (Edge<E> e : g.edges())
				if (weight(e, w, potential, weightShift) <= 0)
					gNeg.addEdge(e);

			Pair<Integer, int[]> pair = Graphs.findStrongConnectivityComponents(gNeg);
			int compNum = pair.e1;
			int[] vToComp = pair.e2;

			for (int u = 0; u < n; u++) {
				int U = vToComp[u];
				for (Iterator<Edge<E>> it = gNeg.edges(u); it.hasNext();) {
					Edge<E> e = it.next();
					if (U == vToComp[e.v()] && weight(e, w, potential, weightShift) < 0) {
						// negative cycle
						gNeg.removeEdge(e);
						List<Edge<E>> negCycle = Graphs.findPath(gNeg, e.v(), u);
						negCycle.add(e);
						return new ResultNegative<>(negCycle);
					}
				}
			}

			int[] vListBegin = new int[n]; // (super vertex -> first vertex in the list) of the current iteration
//			int[] vListEnd = new int[n]; // (super vertex -> last vertex in the list) of the current iteration
			int[] vListNext = new int[n]; // (vertex -> next vertex in the list)

			Arrays.fill(vListBegin, -1);

			for (int v = 0; v < n; v++) {
				int V = vToComp[v];
				vListNext[v] = vListBegin[V];
				vListBegin[V] = v;
			}

			int[] inDegree = new int[compNum];
			int[] queue = new int[compNum];
			int[] topolSort = new int[compNum];
			int queueBegin = 0, queueEnd = 0, topolSortSize = 0;
			for (Edge<E> e : gNeg.edges()) {
				int V = vToComp[e.v()];
				if (vToComp[e.u()] != V)
					inDegree[V]++;
			}

			// Find vertices with zero in degree and insert them to the queue
			for (int V = 0; V < compNum; V++)
				if (inDegree[V] == 0)
					queue[queueEnd++] = V;

			// Poll vertices from the queue and "remove" each one from the tree and add new
			// zero in degree vertices to the queue
			while (queueBegin != queueEnd) {
				int U = queue[queueBegin++];
				topolSort[topolSortSize++] = U;
				for (int u = vListBegin[U]; u != -1; u = vListNext[u]) {
					for (Iterator<Edge<E>> it = g.edges(u); it.hasNext();) {
						int V = vToComp[it.next().v()];
						if (U != V && --inDegree[V] == 0)
							queue[queueEnd++] = V;
					}
				}
			}
			if (topolSortSize != compNum)
				throw new InternalError(); // TODO remove

			int[] distance = new int[n];
			for (int j = 0; j < compNum; j++) {
				int U = topolSort[j];
				int UDis = distance[U];
				for (int u = vListBegin[U]; u != -1; u = vListNext[u]) {
					for (Iterator<Edge<E>> it = g.edges(u); it.hasNext();) {
						Edge<E> e = it.next();
						int V = vToComp[e.v()];
						if (U != V)
							distance[V] = Math.min(distance[V], UDis + weight(e, w, potential, weightShift));
					}
				}
			}
			
			
		}

		// TODO Auto-generated method stub
		return null;
	}

	private static <E> int weight(Edge<E> e, WeightFunctionInt<E> w, int[] potential, int weightShift) {
		int weight = w.weightInt(e);
		boolean neg = weight < 0;
		if (neg)
			weight = -weight;
		// weight = ceil(weight / 2^weightShift)
		weight = (weight + (1 << (weightShift - 1))) >> weightShift;
		if (neg)
			weight = -weight;
		return weight + potential[e.u()] - potential[e.v()];
	}

	private static class ResultNegative<E> implements SSSP.Result<E> {

		private final List<Edge<E>> cycle;

		ResultNegative(List<Edge<E>> cycle) {
			this.cycle = Collections.unmodifiableList(cycle);
		}

		@Override
		public double distance(int t) {
			throw new IllegalStateException();
		}

		@Override
		public Collection<Edge<E>> getPathTo(int t) {
			throw new IllegalStateException();
		}

		@Override
		public boolean foundNegativeCircle() {
			return true;
		}

		@Override
		public Collection<Edge<E>> getNegativeCircle() {
			return cycle;
		}

	}

}
