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
package com.jgalgo.gen;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;

/**
 * A generator of graphs.
 *
 * <p>
 * The generator can be used to generate with different distributions and structures. A generator may be used to
 * generate only trees, only bipartite graphs, dense or sparse graphs, etc.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public interface GraphGenerator<V, E> {

	/**
	 * Generates an immutable graph.
	 *
	 * <p>
	 * For mutable graphs use {@link #generateMutable()}.
	 *
	 * @return a new immutable graph generated with the generator parameters
	 */
	default Graph<V, E> generate() {
		return generateIntoBuilder().build();
	}

	/**
	 * Generates a mutable graph.
	 *
	 * <p>
	 * For immutable graphs use {@link #generate()}.
	 *
	 * @return a new mutable graph generated with the generator parameters
	 */
	default Graph<V, E> generateMutable() {
		return generateIntoBuilder().buildMutable();
	}

	/**
	 * Generates a graph into a builder.
	 *
	 * <p>
	 * This is the a more flexible way to generate a graph. The builder can be used to generate a mutable or immutable
	 * graph, or to add additional vertices or edges on top of the generated ones.
	 *
	 * @return a new graph builder populated by the generator with the generator parameters
	 */
	GraphBuilder<V, E> generateIntoBuilder();

}
