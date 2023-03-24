package com.ugav.algo;

import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphArrayUndirected extends GraphArrayAbstract implements UGraph {

	private final Weights<int[]> edges;
	private final Weights.Int edgesNum;

	public GraphArrayUndirected() {
		this(0);
	}

	public GraphArrayUndirected(int n) {
		this(n, null, null);
	}

	protected GraphArrayUndirected(int n, IDStrategy verticesIDStrategy, IDStrategy edgesIDStrategy) {
		super(verticesIDStrategy, edgesIDStrategy);
		edgesNum = new VerticesWeights.Builder(this, null).ofInts(0);
		/* We use edgesNum to maintain the current vertices in the graph */
		IDStrategy vIDStrategy = getVerticesIDStrategy();
		WeightsAbstract<?> verticesSet = (WeightsAbstract<?>) edgesNum;
		verticesSet.forceAdd = true;
		for (int i = 0; i < n; i++) {
			int u = vIDStrategy.nextID(i);
			verticesSet.keyAdd(u);
		}
		addInternalVerticesWeight(edgesNum, false);

		VerticesWeights.Builder vBuilder = new VerticesWeights.Builder(this, () -> vertices().size());
		edges = vBuilder.ofObjs(EmptyIntArr);
		addInternalVerticesWeight(edges);
	}

	@Override
	public IntSet vertices() {
		return ((WeightsAbstract<?>) edgesNum).keysSet();
	}

	@Override
	public EdgeIter edges(int u) {
		checkVertexIdx(u);
		return new EdgeIt(u, edges.get(u), edgesNum.getInt(u));
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		addEdgeToList(edges, edgesNum, u, e);
		addEdgeToList(edges, edgesNum, v, e);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = swapBeforeRemove(e);
		int u = edgeSource(e), v = edgeTarget(e);
		removeEdgeFromList(edges, edgesNum, u, e);
		removeEdgeFromList(edges, edgesNum, v, e);
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		assert e1 != e2;
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		int i1 = edgeIndexOf(edges, edgesNum, u1, e1);
		int j1 = edgeIndexOf(edges, edgesNum, v1, e1);
		int i2 = edgeIndexOf(edges, edgesNum, u2, e2);
		int j2 = edgeIndexOf(edges, edgesNum, v2, e2);
		int[] u1es = edges.get(u1), v1es = edges.get(v1);
		int[] u2es = edges.get(u2), v2es = edges.get(v2);
		u1es[i1] = e2;
		v1es[j1] = e2;
		u2es[i2] = e1;
		v2es[j2] = e1;
		super.edgeSwap(e1, e2);
	}

	@Override
	public void removeEdgesAll(int u) {
		checkVertexIdx(u);
		while (edgesNum.getInt(u) > 0)
			removeEdge(edges.get(u)[0]);
	}

	@Override
	public int degree(int u) {
		checkVertexIdx(u);
		return edgesNum.getInt(u);
	}

	@Override
	public void clearEdges() {
		int n = vertices().size();
		for (int u = 0; u < n; u++) {
			edges.set(u, EmptyIntArr);
			edgesNum.set(u, 0);
		}
		super.clearEdges();
	}

	private class EdgeIt extends GraphArrayAbstract.EdgeIt {

		private final int u;

		EdgeIt(int u, int[] edges, int count) {
			super(edges, count);
			this.u = u;
		}

		@Override
		public int u() {
			return u;
		}

		@Override
		public int v() {
			int u0 = edgeSource(lastEdge);
			int v0 = edgeTarget(lastEdge);
			return u == u0 ? v0 : u0;
		}

	}

}
