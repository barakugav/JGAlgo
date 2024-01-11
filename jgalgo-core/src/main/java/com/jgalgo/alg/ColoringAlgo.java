/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo.alg;

import static com.jgalgo.internal.util.Range.range;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntAdapters;

/**
 * An algorithm that assign a color to each vertex in a graph while avoiding identical color for any pair of adjacent
 * vertices.
 *
 * <p>
 * Given a graph \(G=(V,E)\) a valid coloring is a function \(C:v \rightarrow c\) for any vertex \(v\) in \(V\) where
 * each edge \((u,v)\) in \(E\) satisfy \(C(u) \neq C(v)\). The objective is to minimize the total number of different
 * colors. The problem is NP-hard, but various heuristics exists which give decent results for general graphs and
 * optimal results for special cases.
 *
 * <p>
 * Each color is represented as an integer in range \([0, \textit{colorsNum})\).
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #builder()} may support different options to obtain different implementations.
 *
 * <pre> {@code
 * Graph<String, Integer> g = Graph.newUndirected();
 * g.addVertex("Alice");
 * g.addVertex("Bob");
 * g.addVertex("Charlie");
 * g.addVertex("David");
 * g.addEdge("Alice", "Bob");
 * g.addEdge("Bob", "Charlie");
 * g.addEdge("Charlie", "Alice");
 * g.addEdge("Charlie", "David");
 *
 * Coloring coloringAlg = Coloring.newInstance();
 * VertexPartition<String, Integer> colors = coloringAlg.computeColoring(g);
 * System.out.println("A valid coloring with " + colors.numberOfBlocks() + " colors was found");
 * for (String u : g.vertices()) {
 * 	System.out.println("The color of vertex " + u + " is " + colors.vertexBlock(u));
 * 	for (EdgeIter<String, Integer> eit = g.outEdges(u).iterator(); eit.hasNext();) {
 * 		eit.next();
 * 		String v = eit.target();
 * 		assert colors.vertexBlock(u) != colors.vertexBlock(v);
 * 	}
 * }
 * }</pre>
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Graph_coloring">Wikipedia</a>
 * @author Barak Ugav
 */
public interface ColoringAlgo {

	/**
	 * Assign a color to each vertex of the given graph, resulting in a valid coloring.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link IVertexPartition}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @return                          a valid coloring with (hopefully) small number of different colors
	 * @throws IllegalArgumentException if {@code g} is directed
	 */
	<V, E> VertexPartition<V, E> computeColoring(Graph<V, E> g);

	/**
	 * Check whether a given mapping is a valid coloring of a graph.
	 *
	 * <p>
	 * A valid coloring is first of all a valid {@link VertexPartition}, but also for each edge \((u,v)\) in the graph
	 * the color of \(u\) is different than the color of \(v\).
	 *
	 * @param  <V>     the vertices type
	 * @param  <E>     the edges type
	 * @param  g       a graph
	 * @param  mapping a mapping from the vertices of {@code g} to colors in range \([0, \textit{colorsNum})\)
	 * @return         {@code true} if {@code mapping} is a valid coloring of {@code g}, {@code false} otherwise
	 */
	@SuppressWarnings("unchecked")
	static <V, E> boolean isColoring(Graph<V, E> g, ToIntFunction<V> mapping) {
		final int n = g.vertices().size();
		IndexGraph ig;
		int[] vertexToColor = new int[n];
		int maxColor = -1;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			IntUnaryOperator mapping0 = IntAdapters.asIntUnaryOperator((ToIntFunction<Integer>) mapping);
			for (int v : range(n)) {
				vertexToColor[v] = mapping0.applyAsInt(v);
				maxColor = Math.max(maxColor, vertexToColor[v]);
			}

		} else if (g instanceof IntGraph) {
			ig = g.indexGraph();
			IntUnaryOperator mapping0 = IntAdapters.asIntUnaryOperator((ToIntFunction<Integer>) mapping);
			IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
			for (int v : range(n)) {
				vertexToColor[v] = mapping0.applyAsInt(viMap.indexToIdInt(v));
				maxColor = Math.max(maxColor, vertexToColor[v]);
			}

		} else {
			ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			for (int v : range(n)) {
				vertexToColor[v] = mapping.applyAsInt(viMap.indexToId(v));
				maxColor = Math.max(maxColor, vertexToColor[v]);
			}
		}
		final int colorNum = maxColor + 1;
		if (maxColor > n)
			return false;
		Bitmap seenColors = new Bitmap(colorNum);
		for (int b : vertexToColor) {
			if (b < 0)
				return false;
			seenColors.set(b);
		}
		if (seenColors.nextClearBit(0) != colorNum)
			return false;
		for (int e : range(ig.edges().size()))
			if (vertexToColor[ig.edgeSource(e)] == vertexToColor[ig.edgeTarget(e)])
				return false;
		return true;
	}

	/**
	 * Create a new coloring algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link ColoringAlgo} object. The {@link ColoringAlgo.Builder}
	 * might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link ColoringAlgo}
	 */
	static ColoringAlgo newInstance() {
		return builder().build();
	}

	/**
	 * Create a new coloring algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link ColoringAlgo} objects
	 */
	static ColoringAlgo.Builder builder() {
		return new ColoringAlgo.Builder() {
			String impl;

			@Override
			public ColoringAlgo build() {
				if (impl != null) {
					switch (impl) {
						case "greedy":
							return new ColoringGreedy();
						case "dsatur":
							return new ColoringDSatur();
						case "rlf":
							return new ColoringRecursiveLargestFirst();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				return new ColoringDSatur();
			}

			@Override
			public void setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						ColoringAlgo.Builder.super.setOption(key, value);
				}
			}
		};
	}

	/**
	 * A builder for {@link ColoringAlgo} objects.
	 *
	 * @see    ColoringAlgo#builder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for coloring computation.
		 *
		 * @return a new coloring algorithm
		 */
		ColoringAlgo build();
	}

}
