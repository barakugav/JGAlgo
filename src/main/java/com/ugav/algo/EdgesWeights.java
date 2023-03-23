package com.ugav.algo;

public class EdgesWeights {

	private EdgesWeights() {
	}

	public static <E> Weights.Obj<E> ofObjs(Graph g, Object key) {
		return ofObjs(g, key, null);
	}

	public static <E> Weights.Obj<E> ofObjs(Graph g, Object key, E defVal) {
		return addWeights(g, key, Weights.Obj.ofEdges(g.edgesNum(), defVal));
	}

	public static Weights.Int ofInts(Graph g, Object key) {
		return ofInts(g, key, -1);
	}

	public static Weights.Int ofInts(Graph g, Object key, int defVal) {
		return addWeights(g, key, Weights.Int.ofEdges(g.edgesNum(), defVal));
	}

	public static Weights.Double ofDoubles(Graph g, Object key) {
		return ofDoubles(g, key, -1);
	}

	public static Weights.Double ofDoubles(Graph g, Object key, double defVal) {
		return addWeights(g, key, Weights.Double.ofEdges(g.edgesNum(), defVal));
	}

	public static Weights.Bool ofBools(Graph g, Object key) {
		return ofBools(g, key, false);
	}

	public static Weights.Bool ofBools(Graph g, Object key, boolean defVal) {
		return addWeights(g, key, Weights.Bool.ofEdges(g.edgesNum(), defVal));
	}

	private static <E, WeightsT extends Weights<E>> WeightsT addWeights(Graph g, Object key, WeightsT weights) {
		int m = g.edgesNum();
		for (int e = 0; e < m; e++)
			weights.keyAdd(e);
		((GraphAbstract) g).addEdgesWeights(key, weights);
		return weights;
	}

}
