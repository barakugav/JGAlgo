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
package com.jgalgo.alg.cover;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class EdgeCoverWeightedTest extends TestBase {

	@Test
	public void testRandGraphsUndirectedUnweighted() {
		final long seed = 0x65ce7bdd4feebe24L;
		EdgeCoverTestUtils.testRandGraphs(new EdgeCoverWeighted(), false, false, seed);
	}

	@Test
	public void testRandGraphsDirectedUnweighted() {
		final long seed = 0xd7950706843b1ed8L;
		EdgeCoverTestUtils.testRandGraphs(new EdgeCoverWeighted(), true, false, seed);
	}

	@Test
	public void testRandGraphsUndirectedWeighted() {
		final long seed = 0xb9ad2d3aaa77ed58L;
		EdgeCoverTestUtils.testRandGraphs(new EdgeCoverWeighted(), false, true, seed);
	}

	@Test
	public void testRandGraphsDirectedWeighted() {
		final long seed = 0x29b61db44248c0f2L;
		EdgeCoverTestUtils.testRandGraphs(new EdgeCoverWeighted(), true, true, seed);
	}

	@Test
	public void testNoValidCover() {
		final long seed = 0x2708750bcce981f0L;
		EdgeCoverTestUtils.testNoValidCover(new EdgeCoverWeighted(), seed);
	}

}
