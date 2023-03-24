package com.ugav.algo;

import java.util.function.IntSupplier;

import it.unimi.dsi.fastutil.ints.IntIterator;

public class EdgesWeights {

	private EdgesWeights() {
	}

	public static <E> Weights<E> ofObjs(Graph g, Object key) {
		return ofObjs(g, key, null);
	}

	public static <E> Weights<E> ofObjs(Graph g, Object key, E defVal) {
		return addWeights(g, key, new Builder(g, () -> g.edges().size()).ofObjs(defVal));
	}

	public static Weights.Int ofInts(Graph g, Object key) {
		return ofInts(g, key, -1);
	}

	public static Weights.Int ofInts(Graph g, Object key, int defVal) {
		return addWeights(g, key, new Builder(g, () -> g.edges().size()).ofInts(defVal));
	}

	public static Weights.Long ofLongs(Graph g, Object key) {
		return ofLongs(g, key, -1);
	}

	public static Weights.Long ofLongs(Graph g, Object key, long defVal) {
		return addWeights(g, key, new Builder(g, () -> g.edges().size()).ofLongs(defVal));
	}

	public static Weights.Double ofDoubles(Graph g, Object key) {
		return ofDoubles(g, key, -1);
	}

	public static Weights.Double ofDoubles(Graph g, Object key, double defVal) {
		return addWeights(g, key, new Builder(g, () -> g.edges().size()).ofDoubles(defVal));
	}

	public static Weights.Bool ofBools(Graph g, Object key) {
		return ofBools(g, key, false);
	}

	public static Weights.Bool ofBools(Graph g, Object key, boolean defVal) {
		return addWeights(g, key, new Builder(g, () -> g.edges().size()).ofBools(defVal));
	}

	@SuppressWarnings("unchecked")
	private static <E, WeightsT extends Weights<E>> WeightsT addWeights(Graph g, Object key, WeightsT weights) {
		for (IntIterator it = g.edges().iterator(); it.hasNext(); ) {
			int e = it.nextInt();
			((WeightsAbstract<E>) weights).keyAdd(e);
		}
		((GraphAbstract) g).addEdgesWeights(key, weights);
		return weights;
	}

	static class Builder {
		private final IntSupplier getExpectedSize;
		private final boolean isFixed;

		Builder(Graph g, IntSupplier getExpectedSize) {
			this.getExpectedSize = getExpectedSize != null ? getExpectedSize : () -> 0;
			isFixed = g.getEdgesIDStrategy() instanceof IDStrategy.Fixed;
		}

		<E> Weights<E> ofObjs(E defVal) {
			if (isFixed) {
				return WeightsMap.Obj.ofEdges(getExpectedSize.getAsInt(), defVal);
			} else {
				return WeightsArray.Obj.ofEdges(getExpectedSize.getAsInt(), defVal);
			}
		}

		Weights.Int ofInts(int defVal) {
			if (isFixed) {
				return WeightsMap.Int.ofEdges(getExpectedSize.getAsInt(), defVal);
			} else {
				return WeightsArray.Int.ofEdges(getExpectedSize.getAsInt(), defVal);
			}
		}

		Weights.Long ofLongs(long defVal) {
			if (isFixed) {
				return WeightsMap.Long.ofEdges(getExpectedSize.getAsInt(), defVal);
			} else {
				return WeightsArray.Long.ofEdges(getExpectedSize.getAsInt(), defVal);
			}
		}

		Weights.Double ofDoubles(double defVal) {
			if (isFixed) {
				return WeightsMap.Double.ofEdges(getExpectedSize.getAsInt(), defVal);
			} else {
				return WeightsArray.Double.ofEdges(getExpectedSize.getAsInt(), defVal);
			}
		}

		Weights.Bool ofBools(boolean defVal) {
			if (isFixed) {
				return WeightsMap.Bool.ofEdges(getExpectedSize.getAsInt(), defVal);
			} else {
				return WeightsArray.Bool.ofEdges(getExpectedSize.getAsInt(), defVal);
			}
		}
	}

}
