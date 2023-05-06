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

import it.unimi.dsi.fastutil.ints.IntCollection;

interface MaximumMatchingCardinality extends MaximumMatching {

	/**
	 * {@inheritDoc}
	 * <p>
	 * Compute the maximum <b>cardinality</b> matching of a undirected graph.
	 *
	 * @throws IllegalArgumentException if {@code w} is not {@code null}
	 */
	@Override
	default IntCollection computeMaximumWeightedMatching(Graph g, EdgeWeightFunc w) {
		if (w != null)
			throw new IllegalArgumentException("Only cardinality matching is supported by this algorithm");
		return computeMaximumCardinalityMatching(g);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Compute the maximum <b>cardinality</b> matching of a undirected graph.
	 *
	 * @throws IllegalArgumentException if {@code w} is not {@code null}
	 */
	@Override
	default IntCollection computeMaximumWeightedPerfectMatching(Graph g, EdgeWeightFunc w) {
		if (w != null)
			throw new IllegalArgumentException("Only cardinality matching is supported by this algorithm");
		return computeMaximumCardinalityMatching(g);
	}

}
