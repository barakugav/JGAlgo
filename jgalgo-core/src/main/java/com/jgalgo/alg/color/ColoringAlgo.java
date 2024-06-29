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

package com.jgalgo.alg.color;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;
import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.alg.common.VertexPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntAdapters;

/**
 * An algorithm that assign a color to each vertex in a graph such that the endpoints of each edge have different
 * colors.
 *
 * <p>
 * Given a graph \(G=(V,E)\) a valid coloring is a function \(C:v \rightarrow c\) for any vertex \(v\) in \(V\) where
 * each edge \((u,v)\) in \(E\) satisfy \(C(u) \neq C(v)\). The objective is to minimize the total number of different
 * colors. The problem is NP-hard, but various heuristics exists which give decent results for general graphs and
 * optimal results for special cases.
 *
 * <p>
 * There is not special result object for this interface, rather the result is a {@link VertexPartition}. Each 'block'
 * in the partition is a color, and the vertices in the block are the vertices that have the same color. The number of
 * blocks is the number of different colors used in the coloring.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
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
	 * Assign a color to each vertex of the given graph, while minimizing the number of different colors.
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
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			IntUnaryOperator mapping0 = IntAdapters.asIntUnaryOperator((ToIntFunction<Integer>) mapping);
			for (int v : range(n))
				vertexToColor[v] = mapping0.applyAsInt(v);

		} else if (g instanceof IntGraph) {
			ig = g.indexGraph();
			IntUnaryOperator mapping0 = IntAdapters.asIntUnaryOperator((ToIntFunction<Integer>) mapping);
			IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
			for (int v : range(n))
				vertexToColor[v] = mapping0.applyAsInt(viMap.indexToIdInt(v));

		} else {
			ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			for (int v : range(n))
				vertexToColor[v] = mapping.applyAsInt(viMap.indexToId(v));
		}

		if (Arrays.stream(vertexToColor).anyMatch(b -> b < 0))
			return false;
		int maxColor = Arrays.stream(vertexToColor).max().orElse(-1);
		if (maxColor > n)
			return false;

		final int colorNum = maxColor + 1;
		Bitmap seenColors = new Bitmap(colorNum);
		for (int b : vertexToColor)
			seenColors.set(b);
		if (seenColors.nextClearBit(0) != colorNum)
			return false;

		return range(ig.edges().size())
				.allMatch(e -> vertexToColor[ig.edgeSource(e)] != vertexToColor[ig.edgeTarget(e)]);
	}

	/**
	 * Create a new coloring algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link ColoringAlgo} object.
	 *
	 * @return a default implementation of {@link ColoringAlgo}
	 */
	static ColoringAlgo newInstance() {
		return new ColoringDSatur();
	}

}
