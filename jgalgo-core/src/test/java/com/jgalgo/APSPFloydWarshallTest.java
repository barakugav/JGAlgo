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

class APSPFloydWarshallTest extends TestBase {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x80b8af9bfbd5e5d5L;
		APSPTestUtils.testAPSPDirectedPositiveInt(new APSPFloydWarshall(), seed);
	}

	@Test
	public void testSSSPUndirectedPositiveInt() {
		final long seed = 0x307fc7bb8684a8b5L;
		APSPTestUtils.testAPSPUndirectedPositiveInt(new APSPFloydWarshall(), seed);
	}

	@Test
	public void testRandGraphDirectedNegativeInt() {
		final long seed = 0xd3037473c85e47b3L;
		APSPTestUtils.testAPSPDirectedNegativeInt(new APSPFloydWarshall(), seed);
	}

	@Test
	public void testRandGraphDirectedCardinality() {
		final long seed = 0xefc29ae984ef7a07L;
		APSPTestUtils.testAPSPCardinality(new APSPFloydWarshall(), true, seed);
	}

	@Test
	public void testRandGraphUndirectedCardinality() {
		final long seed = 0xf301a8a350bea7c9L;
		APSPTestUtils.testAPSPCardinality(new APSPFloydWarshall(), false, seed);
	}

}
