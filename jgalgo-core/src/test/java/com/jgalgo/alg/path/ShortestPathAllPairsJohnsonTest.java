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

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

class ShortestPathAllPairsJohnsonTest extends TestBase {

	private static ShortestPathAllPairs algo() {
		return new ShortestPathAllPairsJohnson();
	}

	@Test
	public void testRandGraphDirectedPositive() {
		final long seed = 0x0a3bf9517b5923b4L;
		ShortestPathAllPairsTestUtils.testApspPositive(algo(), true, true, seed);
	}

	@Test
	public void testRandGraphUndirectedPositive() {
		final long seed = 0x45c53265fe3ea65cL;
		ShortestPathAllPairsTestUtils.testApspPositive(algo(), false, true, seed);
	}

	@Test
	public void testRandGraphDirectedNegative() {
		final long seed = 0xbf0dd8e7294b5cecL;
		ShortestPathAllPairsTestUtils.testApspDirectedNegative(algo(), true, seed);
	}

	@Test
	public void testRandGraphDirectedCardinality() {
		final long seed = 0x9500f9de0d664ee6L;
		ShortestPathAllPairsTestUtils.testApspCardinality(algo(), true, true, seed);
	}

	@Test
	public void testRandGraphDirectedPositiveVerticesSubset() {
		final long seed = 0xdab34a03464cb638L;
		ShortestPathAllPairsTestUtils.testApspPositive(algo(), true, false, seed);
	}

	@Test
	public void testRandGraphUndirectedPositiveVerticesSubset() {
		final long seed = 0xca2c2c3d3eb686cfL;
		ShortestPathAllPairsTestUtils.testApspPositive(algo(), false, false, seed);
	}

	@Test
	public void testRandGraphDirectedNegativeVerticesSubset() {
		final long seed = 0xb370ec758d50a948L;
		ShortestPathAllPairsTestUtils.testApspDirectedNegative(algo(), false, seed);
	}

	@Test
	public void testRandGraphDirectedCardinalityVerticesSubset() {
		final long seed = 0x055b7d3d5da9d06bL;
		ShortestPathAllPairsTestUtils.testApspCardinality(algo(), true, false, seed);
	}

}
