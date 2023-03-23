package com.ugav.algo;

public class VerticesWeights {

	private VerticesWeights() {
	}

	public static <E> Weights<E> ofObjs(Graph g, Object key) {
		return ofObjs(g, key, null);
	}

	public static <E> Weights<E> ofObjs(Graph g, Object key, E defVal) {
		return addWeights(g, key, WeightsArray.Obj.ofEdges(g.verticesNum(), defVal));
	}

	public static Weights.Int ofInts(Graph g, Object key) {
		return ofInts(g, key, -1);
	}

	public static Weights.Int ofInts(Graph g, Object key, int defVal) {
		return addWeights(g, key, WeightsArray.Int.ofEdges(g.verticesNum(), defVal));
	}

	public static Weights.Double ofDoubles(Graph g, Object key) {
		return ofDoubles(g, key, -1);
	}

	public static Weights.Double ofDoubles(Graph g, Object key, double defVal) {
		return addWeights(g, key, WeightsArray.Double.ofEdges(g.verticesNum(), defVal));
	}

	public static Weights.Bool ofBools(Graph g, Object key) {
		return ofBools(g, key, false);
	}

	public static Weights.Bool ofBools(Graph g, Object key, boolean defVal) {
		return addWeights(g, key, WeightsArray.Bool.ofEdges(g.verticesNum(), defVal));
	}

	@SuppressWarnings("unchecked")
	private static <E, WeightsT extends Weights<E>> WeightsT addWeights(Graph g, Object key, WeightsT weights) {
		int n = g.verticesNum();
		for (int v = 0; v < n; v++)
			((WeightsAbstract<E>) weights).keyAdd(v);
		((GraphAbstract) g).addVerticesWeights(key, weights);
		return weights;
	}

	/**
	 * The default vertices weight key of the bipartite property.
	 * <p>
	 * A bipartite graph is a graph in which the vertices are partitioned into two
	 * sets V1,V2 and there are no edges between two vertices u,v if they are both
	 * in V1 or both in V2. Some algorithms expect a bipartite graph as an input,
	 * and the partition V1,V2 is expected to be a vertex boolean weight keyed by
	 * {@link #DefaultBipartiteWeightKey}. To use a different key, the algorithms
	 * expose a {@code setBipartiteVerticesWeightKey(Object)} function.
	 */
	public static final Object DefaultBipartiteWeightKey = new Object() {
		@Override
		public String toString() {
			return "DefaultBipartiteVerticesWeightKey";
		}
	};

}
