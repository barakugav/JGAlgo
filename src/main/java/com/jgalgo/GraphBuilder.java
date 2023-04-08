package com.jgalgo;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import com.jgalgo.IDStrategy.IDAddRemoveListener;

public abstract class GraphBuilder {

	int verticesNum;
	Class<? extends IDStrategy> edgesIDStrategy;

	private GraphBuilder() {
	}

	public GraphBuilder setVerticesNum(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		verticesNum = n;
		return this;
	}

	public GraphBuilder setEdgesIDStrategy(Class<? extends IDStrategy> edgesIDStrategy) {
		this.edgesIDStrategy = edgesIDStrategy;
		return this;
	}

	public UGraph buildUndirected() {
		return (UGraph) buildGraph(false);
	}

	public DiGraph buildDirected() {
		return (DiGraph) buildGraph(true);
	}

	private Graph buildGraph(boolean directed) {
		Graph g = (directed ? buildDirectedBase() : buildUndirectedBase());
		IDStrategy eIDStrat = createIDStrategy(edgesIDStrategy);
		if (eIDStrat != null) {
			if (directed) {
				g = new GraphCustomIDStrategiesDirected((GraphBaseContinues) g, eIDStrat);
			} else {
				g = new GraphCustomIDStrategiesUndirected((GraphBaseContinues) g, eIDStrat);
			}
		}
		return g;
	}

	abstract UGraph buildUndirectedBase();

	abstract DiGraph buildDirectedBase();

	private static IDStrategy createIDStrategy(Class<? extends IDStrategy> strategyClass) {
		if (strategyClass == null)
			return null;
		if (strategyClass == IDStrategy.Continues.class) {
			return null; /* Use default */
		} else if (strategyClass == IDStrategy.Fixed.class) {
			return new IDStrategy.Fixed();
		} else if (strategyClass == IDStrategy.Rand.class) {
			return new IDStrategy.Rand();
		} else {
			throw new IllegalArgumentException(strategyClass.toString());
		}
	}

	public static class Array extends GraphBuilder {
		private Array() {
		}

		public static GraphBuilder.Array newInstance() {
			return new GraphBuilder.Array();
		}

		@Override
		public UGraph buildUndirectedBase() {
			return new GraphArrayUndirected(verticesNum);
		}

		@Override
		public DiGraph buildDirectedBase() {
			return new GraphArrayDirected(verticesNum);
		}
	}

	public static class Linked extends GraphBuilder {
		private Linked() {
		}

		public static GraphBuilder.Linked newInstance() {
			return new GraphBuilder.Linked();
		}

		@Override
		public UGraph buildUndirectedBase() {
			return new GraphLinkedUndirected(verticesNum);
		}

		@Override
		public DiGraph buildDirectedBase() {
			return new GraphLinkedDirected(verticesNum);
		}
	}

	public static class Table extends GraphBuilder {
		private Table() {
		}

		public static GraphBuilder.Table newInstance() {
			return new GraphBuilder.Table();
		}

		@Override
		public UGraph buildUndirectedBase() {
			return new GraphTableUndirected(verticesNum);
		}

		@Override
		public DiGraph buildDirectedBase() {
			return new GraphTableDirected(verticesNum);
		}
	}

	private static class GraphCustomIDStrategies extends GraphBase {

		final GraphBaseContinues g;

		GraphCustomIDStrategies(GraphBaseContinues g, IDStrategy edgesIDStrategy) {
			super(new IDStrategy.Continues(g.vertices().size()), edgesIDStrategy, g.getCapabilities());
			this.g = Objects.requireNonNull(g);

			g.getVerticesIDStrategy().addIDSwapListener((vIdx1, vIdx2) -> verticesIDStrategy.idxSwap(vIdx1, vIdx2));
			g.getVerticesIDStrategy().addIDAddRemoveListener(new IDAddRemoveListener() {

				@Override
				public void idRemove(int id) {
					verticesIDStrategy.removeIdx(id);
				}

				@Override
				public void idAdd(int id) {
					int idx = verticesIDStrategy.newIdx();
					if (idx != id)
						throw new IllegalStateException();
				}

				@Override
				public void idsClear() {
					verticesIDStrategy.clear();
				}
			});
			g.getEdgesIDStrategy().addIDSwapListener((eIdx1, eIdx2) -> edgesIDStrategy.idxSwap(eIdx1, eIdx2));
			g.getEdgesIDStrategy().addIDAddRemoveListener(new IDAddRemoveListener() {

				@Override
				public void idRemove(int id) {
					edgesIDStrategy.removeIdx(id);
				}

				@Override
				public void idAdd(int id) {
					int idx = edgesIDStrategy.newIdx();
					if (idx != id)
						throw new IllegalStateException();
				}

				@Override
				public void idsClear() {
					edgesIDStrategy.clear();
				}
			});
		}

		@Override
		public int addVertex() {
			int uIdx = g.addVertex();
			return verticesIDStrategy.idxToId(uIdx);
		}

		@Override
		public void removeVertex(int v) {
			int vIdx = verticesIDStrategy.idToIdx(v);
			g.removeVertex(vIdx);
		}

		@Override
		public EdgeIter edgesOut(int u) {
			EdgeIter it = g.edgesOut(verticesIDStrategy.idToIdx(u));
			return new EdgeItr(it);
		}

		@Override
		public EdgeIter edgesIn(int v) {
			EdgeIter it = g.edgesIn(verticesIDStrategy.idToIdx(v));
			return new EdgeItr(it);
		}

		@Override
		public int getEdge(int u, int v) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			int vIdx = verticesIDStrategy.idToIdx(v);
			int eIdx = g.getEdge(uIdx, vIdx);
			return eIdx == -1 ? -1 : edgesIDStrategy.idxToId(eIdx);
		}

		@Override
		public EdgeIter getEdges(int u, int v) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			int vIdx = verticesIDStrategy.idToIdx(v);
			EdgeIter it = g.getEdges(uIdx, vIdx);
			return new EdgeItr(it);
		}

		@Override
		public int addEdge(int u, int v) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			int vIdx = verticesIDStrategy.idToIdx(v);
			int eIdx = g.addEdge(uIdx, vIdx);
			return edgesIDStrategy.idxToId(eIdx);
		}

		@Override
		public void removeEdge(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			g.removeEdge(eIdx);
		}

		@Override
		public void removeEdgesAll(int u) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			g.removeEdgesAll(uIdx);
		}

		@Override
		public void removeEdgesAllOut(int u) {
			g.removeEdgesAllOut(verticesIDStrategy.idToIdx(u));
		}

		@Override
		public void removeEdgesAllIn(int v) {
			g.removeEdgesAllIn(verticesIDStrategy.idToIdx(v));
		}

		@Override
		public int edgeSource(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			return g.edgeSource(eIdx);
		}

		@Override
		public int edgeTarget(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			return g.edgeTarget(eIdx);
		}

		@Override
		public int edgeEndpoint(int edge, int endpoint) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			int endpointIdx = verticesIDStrategy.idToIdx(endpoint);
			int resIdx = g.edgeEndpoint(eIdx, endpointIdx);
			return verticesIDStrategy.idxToId(resIdx);
		}

		@Override
		public int degreeOut(int u) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			return g.degreeOut(uIdx);
		}

		@Override
		public int degreeIn(int u) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			return g.degreeIn(uIdx);
		}

		@Override
		public void clear() {
			g.clear();
			verticesIDStrategy.clear();
			edgesIDStrategy.clear();
		}

		@Override
		public void clearEdges() {
			g.clearEdges();
			edgesIDStrategy.clear();
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT verticesWeight(Object key) {
			return g.verticesWeight(key);
		}

		@Override
		public Set<Object> getVerticesWeightKeys() {
			return g.getVerticesWeightKeys();
		}

		@Override
		public Collection<Weights<?>> getVerticesWeights() {
			return g.getVerticesWeights();
		}

		@Override
		public void removeVerticesWeights(Object key) {
			g.removeVerticesWeights(key);
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT edgesWeight(Object key) {
			return g.edgesWeight(key);
		}

		@Override
		public Set<Object> getEdgesWeightsKeys() {
			return g.getEdgesWeightsKeys();
		}

		@Override
		public void removeEdgesWeights(Object key) {
			g.removeEdgesWeights(key);
		}

		@Override
		public Collection<Weights<?>> getEdgesWeights() {
			return g.getEdgesWeights();
		}

		@Override
		public IDStrategy.Continues getVerticesIDStrategy() {
			return verticesIDStrategy;
		}

		@Override
		public IDStrategy getEdgesIDStrategy() {
			return edgesIDStrategy;
		}

		@Override
		<V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, WeightsT weights) {
			return g.addVerticesWeights(key, weights);
		}

		@Override
		<E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, WeightsT weights) {
			return g.addEdgesWeights(key, weights);
		}

		class EdgeItr implements EdgeIter {

			private final EdgeIter it;

			EdgeItr(EdgeIter it) {
				this.it = it;
			}

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public int nextInt() {
				int eIdx = it.nextInt();
				return edgesIDStrategy.idxToId(eIdx);
			}

			@Override
			public int v() {
				int vIdx = it.v();
				return verticesIDStrategy.idxToId(vIdx);
			}

			@Override
			public int u() {
				int uIdx = it.u();
				return verticesIDStrategy.idxToId(uIdx);
			}

		}

	}

	private static class GraphCustomIDStrategiesDirected extends GraphCustomIDStrategies implements DiGraph {

		GraphCustomIDStrategiesDirected(GraphBaseContinues g, IDStrategy edgesIDStrategy) {
			super(g, edgesIDStrategy);
			if (!(g instanceof DiGraph))
				throw new IllegalArgumentException();

		}

		private DiGraph digraph() {
			return (DiGraph) g;
		}

		@Override
		public void reverseEdge(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			digraph().reverseEdge(eIdx);
		}

	}

	private static class GraphCustomIDStrategiesUndirected extends GraphCustomIDStrategies implements UGraph {

		GraphCustomIDStrategiesUndirected(GraphBaseContinues g, IDStrategy edgesIDStrategy) {
			super(g, edgesIDStrategy);
			if (!(g instanceof UGraph))
				throw new IllegalArgumentException();

		}

	}

}
