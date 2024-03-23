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
package com.jgalgo.alg.path;

import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;

class ShortestPathUtils {

	private ShortestPathUtils() {}

	static IWeightFunction potentialWeightFunc(IndexGraph g, IWeightFunction w, double[] potential) {
		return e -> w.weight(e) + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];
	}

	static IWeightFunctionInt potentialWeightFunc(IndexGraph g, IWeightFunctionInt w, int[] potential) {
		return e -> w.weightInt(e) + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];
	}

}
