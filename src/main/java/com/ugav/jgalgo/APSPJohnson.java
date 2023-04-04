package com.ugav.jgalgo;

import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class APSPJohnson implements APSP {

	/**
	 * O(nm + n^2 log n)
	 */

	@Override
	public Result calcDistances(Graph g0, EdgeWeightFunc w) {
		if (!(g0 instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		DiGraph g = (DiGraph) g0;
		int n = g.vertices().size();

		boolean negWeight = false;
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			if (w.weight(e) < 0) {
				negWeight = true;
				break;
			}
		}

		if (!negWeight) {
			/* No negative weights, no need for potential */
			SuccessRes res = calcDistancesPositive(g, w);
			res.potential = new double[n];
			return res;
		}

		Pair<double[], Path> potential0 = calcPotential(g, w);
		if (potential0.e2 != null)
			return new NegCycleRes(potential0.e2);
		double[] potential = potential0.e1;

		EdgeWeightFunc wPotential = e -> {
			double up = potential[g.edgeSource(e)];
			double vp = potential[g.edgeTarget(e)];
			return w.weight(e) + up - vp;
		};
		SuccessRes res = calcDistancesPositive(g, wPotential);
		res.potential = potential;
		return res;
	}

	private static SuccessRes calcDistancesPositive(DiGraph g, EdgeWeightFunc w) {
		int n = g.vertices().size();
		SSSP ssspAlgo = new SSSPDijkstra();
		SuccessRes res = new SuccessRes(n);
		for (int source = 0; source < n; source++)
			res.ssspResults[source] = ssspAlgo.calcDistances(g, w, source);
		return res;
	}

	private static Pair<double[], Path> calcPotential(DiGraph g, EdgeWeightFunc w) {
		int n = g.vertices().size();
		DiGraph refG = new GraphArrayDirected(n + 1);
		Weights.Int edgeEef = refG.addEdgesWeight("edgeEef").ofInts();
		for (int u = 0; u < n; u++) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				int refE = refG.addEdge(u, v);
				edgeEef.set(refE, e);
			}
		}

		/* Add fake vertex */
		final int fakeV = n;
		final int fakeEdge = -1;
		for (int v = 0; v < n; v++)
			edgeEef.set(refG.addEdge(fakeV, v), fakeEdge);

		SSSP.Result res = new SSSPBellmanFord().calcDistances(refG,
				e -> (e != fakeEdge ? w.weight(edgeEef.getInt(e)) : 0), fakeV);
		if (!res.foundNegativeCycle()) {
			double[] potential = new double[n];
			for (int v = 0; v < n; v++)
				potential[v] = res.distance(v);
			return Pair.of(potential, null);
		} else {
			Path negCycleRef = res.getNegativeCycle();
			IntList negCycle = new IntArrayList(negCycleRef.size());
			for (IntIterator it = negCycleRef.iterator(); it.hasNext();)
				negCycle.add(edgeEef.getInt(it.nextInt()));
			return Pair.of(null, new Path(g, negCycleRef.source(), negCycleRef.target(), negCycle));
		}
	}

	private static class NegCycleRes implements APSP.Result {

		private final Path negCycle;

		public NegCycleRes(Path negCycle) {
			Objects.requireNonNull(negCycle);
			this.negCycle = negCycle;
		}

		@Override
		public double distance(int source, int target) {
			throw new IllegalStateException();
		}

		@Override
		public Path getPath(int source, int target) {
			throw new IllegalStateException();
		}

		@Override
		public boolean foundNegativeCycle() {
			return true;
		}

		@Override
		public Path getNegativeCycle() {
			return negCycle;
		}

	}

	private static class SuccessRes implements APSP.Result {

		final SSSP.Result[] ssspResults;
		double[] potential;

		SuccessRes(int n) {
			ssspResults = new SSSP.Result[n];
		}

		@Override
		public double distance(int source, int target) {
			return ssspResults[source].distance(target) + potential[source] - potential[target];
		}

		@Override
		public Path getPath(int source, int target) {
			return ssspResults[source].getPathTo(target);
		}

		@Override
		public boolean foundNegativeCycle() {
			return false;
		}

		@Override
		public Path getNegativeCycle() {
			throw new IllegalStateException();
		}

	}

}
