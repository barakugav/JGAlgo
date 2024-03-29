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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.TestBase;

@SuppressWarnings("boxing")
public class KShortestPathsSTKatohIbarakiMineTest extends TestBase {

	@Test
	public void randGraphs() {
		KShortestPathsST.Builder builder = KShortestPathsST.builder();
		builder.setOption("impl", "katoh-ibaraki-mine");
		builder.setOption("fast-replacement-threshold", Integer.valueOf(5));
		KShortestPathsSTTestUtils.randGraphs(builder.build(), false, 0x3b8b75d281206314L);
	}

	@Test
	public void randGraphWithDefaultFastReplacementAlgoThreshold() {
		KShortestPathsST.Builder builder = KShortestPathsST.builder();
		builder.setOption("impl", "katoh-ibaraki-mine");
		KShortestPathsSTTestUtils.randGraph(builder.build(), false, 1000, 3000, 10, 0xc9c5279d5fd2e4acL);
	}

	@Test
	public void invalidArgsTest() {
		KShortestPathsSTTestUtils.invalidArgsTest(new KShortestPathsSTKatohIbarakiMine(), false);

		KShortestPathsST algo = new KShortestPathsSTKatohIbarakiMine();
		IntGraph g = IntGraph.newDirected();
		g.addVertices(range(2));
		assertThrows(IllegalArgumentException.class, () -> algo.computeKShortestPaths(g, null, 0, 1, 1));
	}

	@Test
	public void k1() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertices(range(2));
		g.addEdge(0, 1, 17);

		KShortestPathsST algo = new KShortestPathsSTKatohIbarakiMine();
		List<Path<Integer, Integer>> paths = algo.computeKShortestPaths(g, null, 0, 1, 1);
		assertEquals(List.of(IPath.valueOf(g, 0, 1, Fastutil.list(17))), paths);
	}

}
