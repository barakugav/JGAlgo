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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Finds all maximal cliques in a graph.
 * <p>
 * A clique is a subset of vertices of an undirected graph such that every two distinct vertices in the clique are
 * adjacent (connected by an edge). A maximal clique is a clique that cannot be extended by including one more adjacent
 * vertex.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * <pre> {@code
 * Graph g = ...;
 * MaximalCliques maxCliquesAlgo = MaximalCliques.newInstance();
 *
 * for (IntCollection clique : maxCliquesAlgo.findAllMaximalCliques(g)) {
 * 	System.out.println("Clique in the graph:");
 * 	for (int v : clique)
 * 		System.out.println("\t" + v);
 * }
 * }</pre>
 *
 * @author Barak Ugav
 */
public interface MaximalCliques {

	/**
	 * Finds all the maximal cliques in a graph.
	 * <p>
	 * The number of maximal cliques can be exponential in the number of vertices in the graph. If the graph is large,
	 * consider using the {@link #iterateMaximalCliques(IntGraph)} method instead, which may iterate the cliques one at
	 * a time without storing all them at the same time in memory.
	 *
	 * @param  g a graph
	 * @return   a collection containing all maximal cliques in the graph
	 */
	default Collection<IntSet> findAllMaximalCliques(IntGraph g) {
		List<IntSet> cliques = new ObjectArrayList<>();
		for (Iterator<IntSet> it = iterateMaximalCliques(g); it.hasNext();)
			cliques.add(it.next());
		return cliques;
	}

	/**
	 * Iterate over all maximal cliques in a graph.
	 * <p>
	 * In contrast to {@link #findAllMaximalCliques(IntGraph)}, this method may iterate the cliques one at a time and
	 * can be used to avoid storing all the cliques in memory at the the time.
	 *
	 * @param  g a graph
	 * @return   an iterator that iterates over all maximal cliques in the graph
	 */
	Iterator<IntSet> iterateMaximalCliques(IntGraph g);

	/**
	 * Create a new maximal cliques algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MaximalCliques} object. The
	 * {@link MaximalCliques.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link MaximalCliques}
	 */
	static MaximalCliques newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new builder for maximal cliques algorithms.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder for maximal cliques algorithms
	 */
	static MaximalCliques.Builder newBuilder() {
		return new MaximalCliques.Builder() {
			String impl;

			@Override
			public MaximalCliques build() {
				if (impl != null) {
					switch (impl) {
						case "bron-kerbosch":
							return new MaximalCliquesBronKerbosch();
						case "bron-kerbosch-pivot":
							return new MaximalCliquesBronKerboschPivot();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				return new MaximalCliquesBronKerbosch();
			}

			@Override
			public MaximalCliques.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link MaximalCliques} objects.
	 *
	 * @see    MaximalCliques#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Build a new {@link MaximalCliques} object.
		 *
		 * @return a new {@link MaximalCliques} object
		 */
		MaximalCliques build();

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
		default MaximalCliques.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
