package com.ugav.algo;

import java.util.function.IntSupplier;

import it.unimi.dsi.fastutil.ints.IntIterator;

public class VerticesWeights {

	private VerticesWeights() {
	}

	public static <E> Weights<E> ofObjs(Graph g, Object key) {
		return ofObjs(g, key, null);
	}

	public static <E> Weights<E> ofObjs(Graph g, Object key, E defVal) {
		return addWeights(g, key, new Builder(g, () -> g.vertices().size()).ofObjs(defVal));
	}

	public static Weights.Int ofInts(Graph g, Object key) {
		return ofInts(g, key, -1);
	}

	public static Weights.Int ofInts(Graph g, Object key, int defVal) {
		return addWeights(g, key, new Builder(g, () -> g.vertices().size()).ofInts(defVal));
	}

	public static Weights.Long ofLongs(Graph g, Object key) {
		return ofLongs(g, key, -1);
	}

	public static Weights.Long ofLongs(Graph g, Object key, long defVal) {
		return addWeights(g, key, new Builder(g, () -> g.vertices().size()).ofLongs(defVal));
	}

	public static Weights.Double ofDoubles(Graph g, Object key) {
		return ofDoubles(g, key, -1);
	}

	public static Weights.Double ofDoubles(Graph g, Object key, double defVal) {
		return addWeights(g, key, new Builder(g, () -> g.vertices().size()).ofDoubles(defVal));
	}

	public static Weights.Bool ofBools(Graph g, Object key) {
		return ofBools(g, key, false);
	}

	public static Weights.Bool ofBools(Graph g, Object key, boolean defVal) {
		return addWeights(g, key, new Builder(g, () -> g.vertices().size()).ofBools(defVal));
	}

	@SuppressWarnings("unchecked")
	private static <E, WeightsT extends Weights<E>> WeightsT addWeights(Graph g, Object key, WeightsT weights) {
		for (IntIterator it = g.vertices().iterator(); it.hasNext(); ) {
			int v = it.nextInt();
			((WeightsAbstract<E>) weights).keyAdd(v);
		}
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

	static class Builder {
		private final IntSupplier getExpectedSize;
		final boolean isFixed;

		Builder(Graph g, IntSupplier getExpectedSize) {
			this.getExpectedSize = getExpectedSize != null ? getExpectedSize : () -> 0;
			isFixed = g.getEdgesIDStrategy() instanceof IDStrategy.Fixed;
		}

		<E> Weights<E> ofObjs(E defVal) {
			if (isFixed) {
				return WeightsMap.Obj.ofVertices(getExpectedSize.getAsInt(), defVal);
			} else {
				return WeightsArray.Obj.ofVertices(getExpectedSize.getAsInt(), defVal);
			}
		}

		Weights.Int ofInts(int defVal) {
			if (isFixed) {
				return WeightsMap.Int.ofVertices(getExpectedSize.getAsInt(), defVal);
			} else {
				return WeightsArray.Int.ofVertices(getExpectedSize.getAsInt(), defVal);
			}
		}

		Weights.Long ofLongs(long defVal) {
			if (isFixed) {
				return WeightsMap.Long.ofVertices(getExpectedSize.getAsInt(), defVal);
			} else {
				return WeightsArray.Long.ofVertices(getExpectedSize.getAsInt(), defVal);
			}
		}

		Weights.Double ofDoubles(double defVal) {
			if (isFixed) {
				return WeightsMap.Double.ofVertices(getExpectedSize.getAsInt(), defVal);
			} else {
				return WeightsArray.Double.ofVertices(getExpectedSize.getAsInt(), defVal);
			}
		}

		Weights.Bool ofBools(boolean defVal) {
			if (isFixed) {
				return WeightsMap.Bool.ofVertices(getExpectedSize.getAsInt(), defVal);
			} else {
				return WeightsArray.Bool.ofVertices(getExpectedSize.getAsInt(), defVal);
			}
		}
	}

}
