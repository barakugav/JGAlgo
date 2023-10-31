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

import java.util.Iterator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;

/**
 * An algorithm that finds all cycles in a graph.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @author Barak Ugav
 */
public interface CyclesFinder {

	/**
	 * Find all cycles in the given graph.
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned iterator will iterate over {@link IPath} objects.
	 *
	 * @param  g a graph
	 * @return   an iterator that iteration over all cycles in the graph
	 */
	public <V, E> Iterator<Path<V, E>> findAllCycles(Graph<V, E> g);

	/**
	 * Create a new algorithm for cycles finding.
	 * <p>
	 * This is the recommended way to instantiate a new {@link CyclesFinder} object. The {@link CyclesFinder.Builder}
	 * might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link CyclesFinder}
	 */
	static CyclesFinder newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new cycles finder algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link CyclesFinder} objects
	 */
	static CyclesFinder.Builder newBuilder() {
		return new CyclesFinder.Builder() {
			String impl;

			@Override
			public CyclesFinder build() {
				if (impl != null) {
					switch (impl) {
						case "johnson":
							return new CyclesFinderJohnson();
						case "tarjan":
							return new CyclesFinderTarjan();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				return new CyclesFinderTarjan();
			}

			@Override
			public CyclesFinder.Builder setOption(String key, Object value) {
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
	 * A builder for {@link CyclesFinder} objects.
	 *
	 * @see    CyclesFinder#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for cycles computation.
		 *
		 * @return a new cycles finder algorithm
		 */
		CyclesFinder build();

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
		default CyclesFinder.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
