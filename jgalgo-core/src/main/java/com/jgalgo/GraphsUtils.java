package com.jgalgo;

import java.util.Collections;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

class GraphsUtils {

	private GraphsUtils() {
	}

	static int getFullyBranchingTreeDepth(Graph t, int root) {
		for (int parent = -1, u = root, depth = 0;; depth++) {
			int v = parent;
			for (EdgeIter eit = t.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				v = eit.v();
				if (v != parent)
					break;
			}
			if (v == parent)
				return depth;
			parent = u;
			u = v;
		}
	}

	static int[] calcDegree(UGraph g, IntCollection edges) {
		int[] degree = new int[g.vertices().size()];
		for (IntIterator eit = edges.iterator(); eit.hasNext();) {
			int e = eit.nextInt();
			degree[g.edgeSource(e)]++;
			degree[g.edgeTarget(e)]++;
		}
		return degree;
	}

	static String formatAdjacencyMatrix(Graph g) {
		return formatAdjacencyMatrix(g, e -> e == -1 ? "0" : "1");
	}

	static String formatAdjacencyMatrixWeighted(Graph g, EdgeWeightFunc w) {
		double minWeight = Double.MAX_VALUE;
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			double ew = w.weight(e);
			if (ew < minWeight)
				minWeight = ew;
		}

		int unlimitedPrecision = 64;
		int precision = minWeight >= 1 ? 2 : Math.min(unlimitedPrecision, (int) -Math.log10(minWeight));

		return precision == unlimitedPrecision
				? formatAdjacencyMatrix(g, e -> e == -1 ? "-" : Double.toString(w.weight(e)))
				: formatAdjacencyMatrix(g,
						e -> e == -1 ? "-" : String.format("%." + precision + "f", Double.valueOf(w.weight(e))));
	}

	static String formatAdjacencyMatrixWeightedInt(Graph g, EdgeWeightFunc.Int w) {
		return formatAdjacencyMatrix(g, e -> e == -1 ? "-" : Integer.toString(w.weightInt(e)));
	}

	static String formatAdjacencyMatrix(Graph g, Int2ObjectFunction<String> formatter) {
		int n = g.vertices().size();
		if (n == 0)
			return "[]";

		/* format all edges */
		String[][] strs = new String[n][n];
		for (int u = 0; u < n; u++) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				strs[u][eit.v()] = formatter.apply(e);
			}
		}

		/* calculate cell size */
		int maxStr = 0;
		for (int u = 0; u < n; u++) {
			for (int v = 0; v < n; v++) {
				if (strs[u][v] == null)
					strs[u][v] = formatter.apply(null);
				if (strs[u][v].length() > maxStr)
					maxStr = strs[u][v].length();
			}
		}
		int vertexLabelCellSize = String.valueOf(n - 1).length() + 1;
		int cellSize = Math.max(maxStr + 1, vertexLabelCellSize);

		/* format header row */
		StringBuilder s = new StringBuilder();
		s.append(strMult(" ", vertexLabelCellSize));
		for (int v = 0; v < n; v++)
			s.append(String.format("% " + cellSize + "d", Integer.valueOf(v)));
		s.append('\n');

		/* format adjacency matrix */
		for (int u = 0; u < n; u++) {
			s.append(String.format("% " + vertexLabelCellSize + "d", Integer.valueOf(u)));
			for (int v = 0; v < n; v++) {
				if (strs[u][v].length() < cellSize)
					s.append(strMult(" ", cellSize - strs[u][v].length()));
				s.append(strs[u][v]);
			}
			s.append('\n');
		}

		return s.toString();
	}

	private static String strMult(String s, int n) {
		return String.join("", Collections.nCopies(n, s));
	}

	static DiGraph referenceGraph(DiGraph g, Object refEdgeWeightKey) {
		DiGraph g0 = new GraphArrayDirected(g.vertices().size());
		Weights.Int edgeRef = g0.addEdgesWeights(refEdgeWeightKey, int.class);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int e0 = g0.addEdge(g.edgeSource(e), g.edgeTarget(e));
			edgeRef.set(e0, e);
		}
		return g0;
	}

	static UGraph referenceGraph(UGraph g, Object refEdgeWeightKey) {
		UGraph g0 = new GraphArrayUndirected(g.vertices().size());
		Weights.Int edgeRef = g0.addEdgesWeights(refEdgeWeightKey, int.class);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int e0 = g0.addEdge(g.edgeSource(e), g.edgeTarget(e));
			edgeRef.set(e0, e);
		}
		return g0;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static UGraph subGraph(UGraph g, IntCollection edgeSet) {
		final Object edgeMappingKey = new Object();
		UGraph subG = new GraphArrayUndirected(g.vertices().size());

		Weights.Int sub2Edge = subG.addEdgesWeights(edgeMappingKey, int.class);
		for (IntIterator it = edgeSet.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int s = subG.addEdge(u, v);
			sub2Edge.set(s, e);
		}
		for (Object key : g.getEdgesWeightsKeys()) {
			Weights<?> data0 = g.edgesWeight(key);
			Weights dataSub0 = subG.addEdgesWeights(key, ((WeightsImpl) data0).container.getTypeClass(),
					data0.defaultWeight());

			if (data0 instanceof Weights.Byte) {
				Weights.Byte data = (Weights.Byte) data0;
				Weights.Byte dataSub = (Weights.Byte) dataSub0;
				byte defVal = data.defaultWeightByte();
				for (IntIterator it = subG.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					byte w = data.getByte(sub2Edge.getInt(s));
					if (w != defVal)
						dataSub.set(s, w);
				}

			} else if (data0 instanceof Weights.Short) {
				Weights.Short data = (Weights.Short) data0;
				Weights.Short dataSub = (Weights.Short) dataSub0;
				short defVal = data.defaultWeightShort();
				for (IntIterator it = subG.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					short w = data.getShort(sub2Edge.getInt(s));
					if (w != defVal)
						dataSub.set(s, w);
				}

			} else if (data0 instanceof Weights.Int) {
				Weights.Int data = (Weights.Int) data0;
				Weights.Int dataSub = (Weights.Int) dataSub0;
				int defVal = data.defaultWeightInt();
				for (IntIterator it = subG.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					int w = data.getInt(sub2Edge.getInt(s));
					if (w != defVal)
						dataSub.set(s, w);
				}

			} else if (data0 instanceof Weights.Long) {
				Weights.Long data = (Weights.Long) data0;
				Weights.Long dataSub = (Weights.Long) dataSub0;
				long defVal = data.defaultWeightLong();
				for (IntIterator it = subG.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					long w = data.getLong(sub2Edge.getInt(s));
					if (w != defVal)
						dataSub.set(s, w);
				}

			} else if (data0 instanceof Weights.Float) {
				Weights.Float data = (Weights.Float) data0;
				Weights.Float dataSub = (Weights.Float) dataSub0;
				float defVal = data.defaultWeightFloat();
				for (IntIterator it = subG.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					float w = data.getFloat(sub2Edge.getInt(s));
					if (w != defVal)
						dataSub.set(s, w);
				}

			} else if (data0 instanceof Weights.Double) {
				Weights.Double data = (Weights.Double) data0;
				Weights.Double dataSub = (Weights.Double) dataSub0;
				double defVal = data.defaultWeightDouble();
				for (IntIterator it = subG.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					double w = data.getDouble(sub2Edge.getInt(s));
					if (w != defVal)
						dataSub.set(s, w);
				}

			} else if (data0 instanceof Weights.Bool) {
				Weights.Bool data = (Weights.Bool) data0;
				Weights.Bool dataSub = (Weights.Bool) dataSub0;
				boolean defVal = data.defaultWeightBool();
				for (IntIterator it = subG.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					boolean w = data.getBool(sub2Edge.getInt(s));
					if (w != defVal)
						dataSub.set(s, w);
				}

			} else if (data0 instanceof Weights.Char) {
				Weights.Char data = (Weights.Char) data0;
				Weights.Char dataSub = (Weights.Char) dataSub0;
				char defVal = data.defaultWeightChar();
				for (IntIterator it = subG.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					char w = data.getChar(sub2Edge.getInt(s));
					if (w != defVal)
						dataSub.set(s, w);
				}

			} else {
				Object defVal = data0.defaultWeight();
				for (IntIterator it = subG.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					Object w = data0.get(sub2Edge.getInt(s));
					if (w != defVal)
						dataSub0.set(s, w);
				}
			}
		}
		subG.removeEdgesWeights(edgeMappingKey);
		return subG;
	}

	static boolean containsSelfLoops(Graph g) {
		if (!g.getCapabilities().selfEdges())
			return false;
		int n = g.vertices().size();
		for (int u = 0; u < n; u++) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				if (u == eit.v())
					return true;
			}
		}
		return false;
	}

	static boolean containsParallelEdges(Graph g) {
		if (!g.getCapabilities().parallelEdges())
			return false;
		int n = g.vertices().size();
		int[] lastVisit = new int[n];
		for (int u = 0; u < n; u++) {
			final int visitIdx = u + 1;
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				if (lastVisit[v] == visitIdx)
					return true;
				lastVisit[v] = visitIdx;
			}
		}
		return false;
	}

}
