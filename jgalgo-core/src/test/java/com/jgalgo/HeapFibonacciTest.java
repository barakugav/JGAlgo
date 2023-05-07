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

import java.util.Comparator;
import org.junit.jupiter.api.Test;

public class HeapFibonacciTest extends TestBase {

	private static Heap.Builder heapBuilder() {
		return new Heap.Builder() {
			@Override
			public <E> Heap<E> build(Comparator<? super E> cmp) {
				return new HeapFibonacci<E, Object>(cmp).asHeap();
			}
		};
	}

	@Test
	public void testRandOpsDefaultCompare() {
		final long seed = 0xc5b0d9f99444efc5L;
		HeapTestUtils.testRandOpsDefaultCompare(heapBuilder(), seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0x73f5ac001997955cL;
		HeapTestUtils.testRandOpsCustomCompare(heapBuilder(), seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x194fe80fdf0b3e1bL;
		HeapTestUtils.testRandOpsAfterManyInserts(heapBuilder(), seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0xc801a24c2405c42dL;
		HeapTestUtils.testMeldDefaultCompare(heapBuilder(), seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0x631ed5c1813636efL;
		HeapTestUtils.testMeldCustomCompare(heapBuilder(), seed);
	}

	@Test
	public void testDecreaseKeyDefaultCompare() {
		final long seed = 0xcadbabb0e01d6ea5L;
		HeapReferenceableTestUtils.testDecreaseKeyDefaultCompare(HeapFibonacci::new, seed);
	}

	@Test
	public void testDecreaseKeyCustomCompare() {
		final long seed = 0x0a7f3203577b4cefL;
		HeapReferenceableTestUtils.testDecreaseKeyCustomCompare(HeapFibonacci::new, seed);
	}

}
