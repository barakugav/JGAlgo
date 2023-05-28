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
package com.jgalgo;

import org.junit.jupiter.api.Test;

public class ShortestPathAllPairsCardinalityTest extends TestBase {

	@Test
	public void testRandGraphDirectedCardinality() {
		final long seed = 0xd44bfe45a5769997L;
		ShortestPathAllPairsTestUtils.testAPSPCardinality(new ShortestPathAllPairsCardinality(), true, seed);
	}

	@Test
	public void testRandGraphUndirectedCardinality() {
		final long seed = 0x59723abb525e643dL;
		ShortestPathAllPairsTestUtils.testAPSPCardinality(new ShortestPathAllPairsCardinality(), false, seed);
	}

}
