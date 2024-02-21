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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.TestBase;

public class KShortestPathsSTYenTest extends TestBase {

	@Test
	public void randGraphUndirected() {
		final long seed = 0xaa039caa5dd2c1c3L;
		KShortestPathsSTTestUtils.randGraphs(new KShortestPathsSTYen(), false, seed);
	}

	@Test
	public void randGraphDirected() {
		final long seed = 0x4c41ddd4454cf5f8L;
		KShortestPathsSTTestUtils.randGraphs(new KShortestPathsSTYen(), true, seed);
	}

	@Test
	public void invalidArgsTest() {
		KShortestPathsSTTestUtils.invalidArgsTest(new KShortestPathsSTYen(), true);
	}

	@SuppressWarnings("boxing")
	@Test
	public void k1() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertices(range(2));
		g.addEdge(0, 1, 17);

		KShortestPathsST algo = new KShortestPathsSTYen();
		List<Path<Integer, Integer>> paths = algo.computeKShortestPaths(g, null, 0, 1, 1);
		assertEquals(List.of(IPath.valueOf(g, 0, 1, Fastutil.list(17))), paths);
	}

}
