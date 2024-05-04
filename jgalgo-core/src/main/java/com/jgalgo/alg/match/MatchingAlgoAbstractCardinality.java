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
package com.jgalgo.alg.match;

import java.util.Arrays;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;

/**
 * Abstract class for computing (only) cardinality matching in a graph.
 *
 * <p>
 * The {@link MatchingAlgo} interface expose a large number of methods of different variants of the matching problem.
 * This abstract class implements these methods by restricting the input weight function to be the
 * {@linkplain IWeightFunction#CardinalityWeightFunction cardinality weight function} or {@code null}, and solving only
 * a relatively simply maximum cardinality matching, {@link #computeMaximumCardinalityMatching(IndexGraph)}, which is
 * left to the subclasses to implement.
 *
 * @author Barak Ugav
 */
public abstract class MatchingAlgoAbstractCardinality extends MatchingAlgoAbstract {

	/**
	 * Default constructor.
	 */
	public MatchingAlgoAbstractCardinality() {}

	protected abstract IMatching computeMaximumCardinalityMatching(IndexGraph g);

	@Override
	protected IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w) {
		Assertions.onlyCardinality(w);
		return computeMaximumCardinalityMatching(g);
	}

	@Override
	protected IMatching computeMinimumWeightedMatching(IndexGraph g, IWeightFunction w) {
		Assertions.onlyCardinality(w);
		int[] matched = new int[g.vertices().size()];
		Arrays.fill(matched, -1);
		return new IndexMatching(g, matched);
	}

	@Override
	protected IMatching computeMaximumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
		Assertions.onlyCardinality(w);
		return computeMaximumCardinalityMatching(g);
	}

	@Override
	protected IMatching computeMinimumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
		Assertions.onlyCardinality(w);
		return computeMaximumCardinalityMatching(g);
	}

}
