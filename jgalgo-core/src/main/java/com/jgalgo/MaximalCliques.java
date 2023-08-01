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
package com.jgalgo;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.jgalgo.graph.Graph;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Finds all maximal cliques in a graph.
 * <p>
 * A clique is a subset of vertices of an undirected graph such that every two distinct vertices in the clique are
 * adjacent (connected by an edge). A maximal clique is a clique that cannot be extended by including one more adjacent
 * vertex.
 * <p>
 *
 * <pre> {@code
 * Graph g = ...;
 * MaximalCliques maxCliquesAlgo = MaximalCliques.newBuilder().build();
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
	 * consider using the {@link #iterateMaximalCliques(Graph)} method instead, which may iterate the cliques one at a
	 * time without storing all them at the same time in memory.
	 *
	 * @param  g a graph
	 * @return   a collection containing all maximal cliques in the graph
	 */
	default Collection<IntCollection> findAllMaximalCliques(Graph g) {
		List<IntCollection> cliques = new ObjectArrayList<>();
		for (Iterator<IntCollection> it = iterateMaximalCliques(g); it.hasNext();)
			cliques.add(it.next());
		return cliques;
	}

	/**
	 * Iterate over all maximal cliques in a graph.
	 * <p>
	 * In contrast to {@link #findAllMaximalCliques(Graph)}, this method may iterate the cliques one at a time and can
	 * be used to avoid storing all the cliques in memory at the the time.
	 *
	 * @param  g a graph
	 * @return   an iterator that iterates over all maximal cliques in the graph
	 */
	Iterator<IntCollection> iterateMaximalCliques(Graph g);

	/**
	 * Create a new builder for maximal cliques algorithms.
	 *
	 * @return a new builder for maximal cliques algorithms
	 */
	static MaximalCliques.Builder newBuilder() {
		return MaximalCliquesBronKerbosch::new;
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

	}

}
