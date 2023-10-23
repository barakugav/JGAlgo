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

import java.util.Random;
import com.jgalgo.graph.Graph;

/**
 * An algorithm that assign a color to each vertex in a graph while avoiding identical color for any pair of adjacent
 * vertices.
 * <p>
 * Given a graph \(G=(V,E)\) a valid coloring is a function \(C:v \rightarrow c\) for any vertex \(v\) in \(V\) where
 * each edge \((u,v)\) in \(E\) satisfy \(C(u) \neq C(v)\). The objective is to minimize the total number of different
 * colors. The problem is NP-hard, but various heuristics exists which give decent results for general graphs and
 * optimal results for special cases.
 * <p>
 * Each color is represented as an integer in range \([0, \textit{colorsNum})\).
 *
 * <pre> {@code
 * Graph g = GraphFactory.newUndirected().newGraph();
 * int v1 = g.addVertex();
 * int v2 = g.addVertex();
 * int v3 = g.addVertex();
 * int v4 = g.addVertex();
 * g.addEdge(v1, v2);
 * g.addEdge(v2, v3);
 * g.addEdge(v3, v1);
 * g.addEdge(v3, v4);
 *
 * Coloring coloringAlg = Coloring.newBuilder().build();
 * Coloring.Result colors = coloringAlg.computeColoring(g);
 * System.out.println("A valid coloring with " + colors.colorsNum() + " colors was found");
 * for (int u : g.vertices()) {
 * 	System.out.println("The color of vertex " + u + " is " + colors.colorOf(u));
 * 	for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
 * 		eit.nextInt();
 * 		int v = eit.target();
 * 		assert colors.colorOf(u) != colors.colorOf(v);
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
	 * @param  g                        a graph
	 * @return                          a valid coloring with (hopefully) small number of different colors
	 * @throws IllegalArgumentException if {@code g} is directed
	 */
	ColoringAlgo.Result computeColoring(Graph g);

	/**
	 * A coloring result containing a color for each vertex.
	 *
	 * @author Barak Ugav
	 */
	interface Result {

		/**
		 * The total number of different colors used in the coloring.
		 *
		 * @return number of different colors
		 */
		int colorsNum();

		/**
		 * Get the color assigned to a vertex.
		 *
		 * @param  vertex a vertex identifier in the graph
		 * @return        a color of the vertex, represented as integer
		 */
		int colorOf(int vertex);

	}

	/**
	 * Create a new coloring algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link ColoringAlgo} object.
	 *
	 * @return a new builder that can build {@link ColoringAlgo} objects
	 */
	static ColoringAlgo.Builder newBuilder() {
		return new ColoringAlgo.Builder() {
			String impl;
			Random rand;

			@Override
			public ColoringAlgo build() {
				if (impl != null) {
					switch (impl) {
						case "greedy":
							return rand == null ? new ColoringGreedy() : new ColoringGreedy(rand.nextLong());
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
			public ColoringAlgo.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					case "seed":
						rand = value == null ? null : new Random(((Long) value).longValue());
						break;
					default:
						throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link ColoringAlgo} objects.
	 *
	 * @see    ColoringAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for coloring computation.
		 *
		 * @return a new coloring algorithm
		 */
		ColoringAlgo build();

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default ColoringAlgo.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
