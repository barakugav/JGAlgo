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
package com.jgalgo.graph;

import com.jgalgo.GraphsUtils;

class ArgumentCheck {
	private ArgumentCheck() {}

	static void onlyDirected(Graph g) {
		if (!g.getCapabilities().directed())
			throw new IllegalArgumentException("only directed graphs are supported");
	}

	static void onlyUndirected(Graph g) {
		if (g.getCapabilities().directed())
			throw new IllegalArgumentException("only undirected graphs are supported");
	}

	static void noSelfEdges(Graph g, String msg) {
		if (GraphsUtils.containsSelfEdges(g))
			throw new IllegalArgumentException(msg);
	}

	static void noParallelEdges(Graph g, String msg) {
		if (GraphsUtils.containsParallelEdges(g))
			throw new IllegalArgumentException(msg);
	}

}
