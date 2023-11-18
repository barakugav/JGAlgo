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

package com.jgalgo.internal.ds;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class HeapBinaryTest extends TestBase {

	private static Heap.Builder heapBuilder() {
		return HeapBinary::new;
	}

	@Test
	public void testRandOpsDefaultCompare() {
		final long seed = 0x28f9c6a5d5270049L;
		HeapTestUtils.testRandOpsDefaultCompare(heapBuilder(), seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0xb3c93b9771081239L;
		HeapTestUtils.testRandOpsCustomCompare(heapBuilder(), seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x3df4bf23b32c1d8bL;
		HeapTestUtils.testRandOpsAfterManyInserts(heapBuilder(), seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0xab3cf8bc3aeb0d18L;
		HeapTestUtils.testMeldDefaultCompare(heapBuilder(), seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0xff8e992c40b921d9L;
		HeapTestUtils.testMeldCustomCompare(heapBuilder(), seed);
	}

}
