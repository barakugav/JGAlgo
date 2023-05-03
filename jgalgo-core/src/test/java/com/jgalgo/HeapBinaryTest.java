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

public class HeapBinaryTest extends TestBase {

	@Test
	public void testRandOpsDefaultCompare() {
		final long seed = 0xce76e918bde66ee3L;
		HeapTestUtils.testRandOpsDefaultCompare(HeapBinary::new, seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0xc8c79b6e3d880041L;
		HeapTestUtils.testRandOpsCustomCompare(HeapBinary::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x2c46712aa83d74a2L;
		HeapTestUtils.testRandOpsAfterManyInserts(HeapBinary::new, seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0xa7e09a00be04a88bL;
		HeapTestUtils.testMeldDefaultCompare(HeapBinary::new, seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0x92cd7f63c8322849L;
		HeapTestUtils.testMeldCustomCompare(HeapBinary::new, seed);
	}

}
