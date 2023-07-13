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

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import com.jgalgo.GraphsUtils;
import com.jgalgo.JGAlgoConfig;

class Assertions {

	static class Graphs {

		private static final boolean AssertIdChecks = getBoolConfig("AssertionsGraphIdCheck");

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

		static void checkId(int id, int length) {
			if (!AssertIdChecks)
				return;
			if (id < 0 || id >= length)
				throw new IndexOutOfBoundsException(
						"No such vertex/edge: " + id + " valid range [" + 0 + ", " + length + ")");
		}

		static void checkVertex(int vertex, int n) {
			if (!AssertIdChecks)
				return;
			if (vertex < 0 || vertex >= n)
				throw new IndexOutOfBoundsException("No such vertex: " + vertex);
		}

		static void checkEdge(int edge, int m) {
			if (!AssertIdChecks)
				return;
			if (edge < 0 || edge >= m)
				throw new IndexOutOfBoundsException("No such edge: " + edge);
		}

	}

	static class Iters {

		private static final boolean AssertNotEmpty = getBoolConfig("AssertionsIterNotEmpty");

		static final String ERR_NO_NEXT = "Iterator has no next element";
		static final String ERR_NO_PREVIOUS = "Iterator has no previous element";

		static void hasNext(Iterator<?> it) {
			if (!AssertNotEmpty)
				return;
			if (!it.hasNext())
				throw new NoSuchElementException(ERR_NO_NEXT);
		}

		static void hasPrevious(ListIterator<?> it) {
			if (!AssertNotEmpty)
				return;
			if (!it.hasPrevious())
				throw new NoSuchElementException(ERR_NO_PREVIOUS);
		}

	}

	private static boolean getBoolConfig(String name) {
		return ((Boolean) JGAlgoConfig.getOption(name).get()).booleanValue();
	}

}
