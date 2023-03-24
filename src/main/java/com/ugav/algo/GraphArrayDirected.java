package com.ugav.algo;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphArrayDirected extends GraphArrayAbstract implements DiGraph {

	private final Weights<int[]> edgesOut;
	private final Weights.Int edgesOutNum;
	private final Weights<int[]> edgesIn;
	private final Weights.Int edgesInNum;

	public GraphArrayDirected() {
		this(0);
	}

	public GraphArrayDirected(int n) {
		edgesOutNum = new VerticesWeights.Builder(this, null).ofInts(0);
		/* We use edgesOutNum to maintain the current vertices in the graph */
		IDStrategy vIDStrategy = getVerticesIDStrategy();
		WeightsAbstract<?> verticesSet = (WeightsAbstract<?>) edgesOutNum;
		verticesSet.forceAdd = true;
		for (int i = 0; i < n; i++) {
			int u = vIDStrategy.nextID(i);
			verticesSet.keyAdd(u);
		}
		addInternalVerticesWeight(edgesOutNum, false);

		VerticesWeights.Builder vBuilder = new VerticesWeights.Builder(this, () -> vertices().size());

		edgesOut = vBuilder.ofObjs(EmptyIntArr);
		edgesIn = vBuilder.ofObjs(EmptyIntArr);
		edgesInNum = vBuilder.ofInts(0);

		addInternalVerticesWeight(edgesOut);
		addInternalVerticesWeight(edgesIn);
		addInternalVerticesWeight(edgesInNum);
	}

	@Override
	public IntSet vertices() {
		return ((WeightsAbstract<?>) edgesOutNum).keysSet();
	}

	@Override
	public EdgeIter edgesOut(int u) {
		checkVertexIdx(u);
		return new EdgeOutIt(u, edgesOut.get(u), edgesOutNum.getInt(u));
	}

	@Override
	public EdgeIter edgesIn(int v) {
		checkVertexIdx(v);
		return new EdgeInIt(v, edgesIn.get(v), edgesInNum.getInt(v));
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		addEdgeToList(edgesOut, edgesOutNum, u, e);
		addEdgeToList(edgesIn, edgesInNum, v, e);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = swapBeforeRemove(e);
		int u = edgeSource(e), v = edgeTarget(e);
		removeEdgeFromList(edgesOut, edgesOutNum, u, e);
		removeEdgeFromList(edgesIn, edgesInNum, v, e);
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		assert e1 != e2;
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		int i1 = edgeIndexOf(edgesOut, edgesOutNum, u1, e1);
		int j1 = edgeIndexOf(edgesIn, edgesInNum, v1, e1);
		int i2 = edgeIndexOf(edgesOut, edgesOutNum, u2, e2);
		int j2 = edgeIndexOf(edgesIn, edgesInNum, v2, e2);
		int[] u1es = edgesOut.get(u1), v1es = edgesIn.get(v1);
		int[] u2es = edgesOut.get(u2), v2es = edgesIn.get(v2);
		u1es[i1] = e2;
		v1es[j1] = e2;
		u2es[i2] = e1;
		v2es[j2] = e1;
		super.edgeSwap(e1, e2);
	}

	@Override
	public void removeEdgesAllOut(int u) {
		checkVertexIdx(u);
		while (edgesOutNum.getInt(u) > 0)
			removeEdge(edgesOut.get(u)[0]);
	}

	@Override
	public void removeEdgesAllIn(int v) {
		checkVertexIdx(v);
		while (edgesInNum.getInt(v) > 0)
			removeEdge(edgesIn.get(v)[0]);
	}

	@Override
	public void reverseEdge(int e) {
		int u = edgeSource(e), v = edgeTarget(e);
		removeEdgeFromList(edgesOut, edgesOutNum, u, e);
		removeEdgeFromList(edgesIn, edgesInNum, v, e);
		addEdgeToList(edgesOut, edgesOutNum, v, e);
		addEdgeToList(edgesIn, edgesInNum, u, e);
		super.reverseEdge(e);
	}

	@Override
	public int degreeOut(int u) {
		checkVertexIdx(u);
		return edgesOutNum.getInt(u);
	}

	@Override
	public int degreeIn(int v) {
		checkVertexIdx(v);
		return edgesInNum.getInt(v);
	}

	@Override
	public void clearEdges() {
		for (IntIterator it = vertices().iterator(); it.hasNext();) {
			int u = it.nextInt();
			// TODO do some sort of 'addKey' instead of set, no need
			edgesOut.set(u, EmptyIntArr);
			edgesIn.set(u, EmptyIntArr);
			edgesOutNum.set(u, 0);
			edgesInNum.set(u, 0);
		}
		super.clearEdges();
	}

	private class EdgeOutIt extends EdgeIt {

		private final int u;

		EdgeOutIt(int u, int[] edges, int count) {
			super(edges, count);
			this.u = u;
		}

		@Override
		public int u() {
			return u;
		}

		@Override
		public int v() {
			return edgeTarget(lastEdge);
		}

	}

	private class EdgeInIt extends EdgeIt {

		private final int v;

		EdgeInIt(int v, int[] edges, int count) {
			super(edges, count);
			this.v = v;
		}

		@Override
		public int u() {
			return edgeSource(lastEdge);
		}

		@Override
		public int v() {
			return v;
		}

	}

}
