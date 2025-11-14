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

public class EdgeCoverCardinalityTest extends TestBase {

	@Test
	public void testRandGraphsUndirected() {
		final long seed = 0x1e30267ee5e7daebL;
		EdgeCoverTestUtils.testRandGraphs(new EdgeCoverCardinality(), false, false, seed);
	}

	@Test
	public void testRandGraphsDirected() {
		final long seed = 0xae0951d73b6c66c2L;
		EdgeCoverTestUtils.testRandGraphs(new EdgeCoverCardinality(), true, false, seed);
	}

	@Test
	public void testNoValidCover() {
		final long seed = 0xd1b695f92a470527L;
		EdgeCoverTestUtils.testNoValidCover(new EdgeCoverCardinality(), seed);
	}

}
