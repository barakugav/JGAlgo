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

public class KShortestPathsStHershbergerMaxelSuriTest extends TestBase {

	@Test
	public void randGraphs() {
		KShortestPathsSt.Builder builder = KShortestPathsSt.builder();
		builder.setOption("impl", "hershberger-maxel-suri");
		builder.setOption("fast-replacement-threshold", Integer.valueOf(5));
		KShortestPathsStTestUtils.randGraphs(builder.build(), true, 0x81541a81321bcabcL);
	}

	@Test
	public void randGraphWithDefaultFastReplacementAlgoThreshold() {
		KShortestPathsSt.Builder builder = KShortestPathsSt.builder();
		builder.setOption("impl", "hershberger-maxel-suri");
		KShortestPathsStTestUtils.randGraph(builder.build(), true, 1000, 3000, 10, 0x337126dc7194280bL);
	}

	@SuppressWarnings("boxing")
	@Test
	public void invalidArgsTest() {
		KShortestPathsStTestUtils.invalidArgsTest(new KShortestPathsStHershbergerMaxelSuri(), true);

		KShortestPathsSt algo = new KShortestPathsStHershbergerMaxelSuri();
		IntGraph g = IntGraph.newUndirected();
		g.addVertices(range(2));
		assertThrows(IllegalArgumentException.class, () -> algo.computeKShortestPaths(g, null, 0, 1, 1));
	}

	@Test
	public void k1() {
		IntGraph g = IntGraph.newDirected();
		g.addVertices(range(2));
		g.addEdge(0, 1, 17);

		KShortestPathsSt algo = new KShortestPathsStHershbergerMaxelSuri();
		@SuppressWarnings("boxing")
		List<Path<Integer, Integer>> paths = algo.computeKShortestPaths(g, null, 0, 1, 1);
		assertEquals(List.of(IPath.valueOf(g, 0, 1, Fastutil.list(17))), paths);
	}

}
