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

public class MinimumVertexCutSt2EdgeCutTest extends TestBase {

	@Test
	public void testRandGraphDirectedUnweighted() {
		final long seed = 0x36a317d1b8ab08b4L;
		MinimumVertexCutSTt2estUtils.testRandGraphs(new MinimumVertexCutSt2EdgeCut(), true, false, seed);
	}

	@Test
	public void testRandGraphUndirectedUnweighted() {
		final long seed = 0x431ccd689c0ecea9L;
		MinimumVertexCutSTt2estUtils.testRandGraphs(new MinimumVertexCutSt2EdgeCut(), false, false, seed);
	}

	@Test
	public void testRandGraphDirectedWeighted() {
		final long seed = 0x12df1d0277298ea3L;
		MinimumVertexCutSTt2estUtils.testRandGraphs(new MinimumVertexCutSt2EdgeCut(), true, true, seed);
	}

	@Test
	public void testRandGraphUndirectedWeighted() {
		final long seed = 0x54f7653238122aefL;
		MinimumVertexCutSTt2estUtils.testRandGraphs(new MinimumVertexCutSt2EdgeCut(), false, true, seed);
	}

}
