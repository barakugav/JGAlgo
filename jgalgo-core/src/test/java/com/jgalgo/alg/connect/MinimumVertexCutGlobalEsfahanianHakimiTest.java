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
package com.jgalgo.alg.connect;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class MinimumVertexCutGlobalEsfahanianHakimiTest extends TestBase {

	@Test
	public void testRandGraphDirected() {
		final long seed = 0x459c5c727962a7d2L;
		MinimumVertexCutGlobalTestUtils.testRandGraphs(new MinimumVertexCutGlobalEsfahanianHakimi(), true, false, seed);
	}

	@Test
	public void testRandGraphUndirected() {
		final long seed = 0x614412691bc1bfbaL;
		MinimumVertexCutGlobalTestUtils
				.testRandGraphs(new MinimumVertexCutGlobalEsfahanianHakimi(), false, false, seed);
	}

	@Test
	public void testCliqueDirected() {
		final long seed = 0xf6bb2a713c860e5bL;
		MinimumVertexCutGlobalTestUtils.testClique(new MinimumVertexCutGlobalEsfahanianHakimi(), true, false, seed);
	}

	@Test
	public void testCliqueUndirected() {
		final long seed = 0xe574d331eab4ab36L;
		MinimumVertexCutGlobalTestUtils.testClique(new MinimumVertexCutGlobalEsfahanianHakimi(), false, false, seed);
	}

	@Test
	public void testEmptyGraph() {
		MinimumVertexCutGlobalTestUtils.testEmptyGraph(new MinimumVertexCutGlobalEsfahanianHakimi());
	}

}
