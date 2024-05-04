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

import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;

/**
 * Abstract class for computing matching in graphs, based on a minimum matching solution.
 *
 * <p>
 * The {@link MatchingAlgo} interface expose a large number of methods of different variants of the matching problem.
 * This abstract class implements some of these methods by reducing to a minimum matching problem,
 * {@link #computeMinimumWeightedMatching(IndexGraph, IWeightFunction)} and
 * {@link #computeMinimumWeightedPerfectMatching(IndexGraph, IWeightFunction)}, which is left to the subclasses to
 * implement.
 *
 * @author Barak Ugav
 */
public abstract class MatchingAlgoAbstractBasedMinimum extends MatchingAlgoAbstract {

	/**
	 * Default constructor.
	 */
	public MatchingAlgoAbstractBasedMinimum() {}

	@Override
	protected IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w) {
		return computeMinimumWeightedMatching(g, negate(w));
	}

	@Override
	protected IMatching computeMaximumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
		if (WeightFunction.isCardinality(w)) {
			/* minimum and maximum weighted perfect matching are equivalent for unweighed graphs */
			return computeMinimumWeightedPerfectMatching(g, null);
		} else {
			return computeMinimumWeightedPerfectMatching(g, negate(w));
		}
	}

}
