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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Weights;

class Bipartite {
	private Bipartite() {}

	static boolean isValidBipartitePartition(Graph g, Weights.Bool partition) {
		for (int e : g.edges()) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			if (partition.getBool(u) == partition.getBool(v))
				return false;
		}
		return true;
	}

}
