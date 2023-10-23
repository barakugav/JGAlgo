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

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class ShortestPathAllPairsCardinalityTest extends TestBase {

	private static ShortestPathAllPairs algo() {
		return new ShortestPathAllPairsCardinality();
	}

	@Test
	public void testRandGraphDirectedCardinality() {
		final long seed = 0xd44bfe45a5769997L;
		ShortestPathAllPairsTestUtils.testAPSPCardinality(algo(), true, true, seed);
	}

	@Test
	public void testRandGraphUndirectedCardinality() {
		final long seed = 0x59723abb525e643dL;
		ShortestPathAllPairsTestUtils.testAPSPCardinality(algo(), false, true, seed);
	}

	@Test
	public void testRandGraphDirectedCardinalityVerticesSubset() {
		final long seed = 0x48ac52279ba45290L;
		ShortestPathAllPairsTestUtils.testAPSPCardinality(algo(), true, false, seed);
	}

	@Test
	public void testRandGraphUndirectedCardinalityVerticesSubset() {
		final long seed = 0xd534ea2c78484622L;
		ShortestPathAllPairsTestUtils.testAPSPCardinality(algo(), false, false, seed);
	}

}
