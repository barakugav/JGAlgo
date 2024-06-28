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

package com.jgalgo.alg.shortestpath;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;

public class ShortestPathSingleSourceCardinalityTest extends TestBase {

	@Test
	public void testSsspUndirectedCardinality() {
		final long seed = 0x1c2a5c66014ee5a2L;
		ShortestPathSingleSourceTestUtils.testSsspCardinality(new ShortestPathSingleSourceCardinality(), false, seed);
	}

	@Test
	public void testSsspDirectedCardinality() {
		final long seed = 0xf03c60e86faad030L;
		ShortestPathSingleSourceTestUtils.testSsspCardinality(new ShortestPathSingleSourceCardinality(), true, seed);
	}

	@SuppressWarnings("boxing")
	@Test
	public void nonCardinality() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 0);

		ShortestPathSingleSource algo = new ShortestPathSingleSourceCardinality();
		assertThrows(IllegalArgumentException.class, () -> algo.computeShortestPaths(g, e -> 5, 0));
	}

}
