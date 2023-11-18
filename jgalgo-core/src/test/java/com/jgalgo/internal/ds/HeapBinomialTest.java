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
import it.unimi.dsi.fastutil.doubles.DoubleComparator;
import it.unimi.dsi.fastutil.ints.IntComparator;

public class HeapBinomialTest extends TestBase {

	private static Heap.Builder<Integer> heapBuilder() {
		return ReferenceableHeapTestUtils.heapBuilderFromReferenceableHeapBuilder(heapReferenceableBuilder());
	}

	private static ReferenceableHeap.Builder heapReferenceableBuilder() {
		return (keyType, valueType, comparator) -> {
			if (keyType == int.class && valueType == int.class)
				return new IntIntBinomialHeap((IntComparator) comparator);
			if (keyType == int.class && valueType == void.class)
				return new IntBinomialHeap((IntComparator) comparator);
			if (keyType == double.class && valueType == int.class)
				return new DoubleIntBinomialHeap((DoubleComparator) comparator);
			if (keyType == double.class && valueType == Object.class)
				return new DoubleObjBinomialHeap<>((DoubleComparator) comparator);
			if (keyType == Object.class && valueType == void.class)
				return new ObjBinomialHeap<>(comparator);
			throw new UnsupportedOperationException("Unsupported heap type: " + keyType + ", " + valueType);
		};
	}

	@Test
	public void testRandOpsDefaultCompare() {
		final long seed = 0x0600b0c18d6d97d4L;
		HeapTestUtils.testRandOpsDefaultCompare(heapBuilder(), seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0xe71c7e9f2765e4eaL;
		HeapTestUtils.testRandOpsCustomCompare(heapBuilder(), seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x55bb8b5e3f70d05aL;
		HeapTestUtils.testRandOpsAfterManyInserts(heapBuilder(), seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0x98473460a5541235L;
		HeapTestUtils.testMeldDefaultCompare(heapBuilder(), seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0xbc597576efd52ccfL;
		HeapTestUtils.testMeldCustomCompare(heapBuilder(), seed);
	}

	@Test
	public void testDecreaseKeyDefaultCompare() {
		final long seed = 0x553462f097149dc1L;
		ReferenceableHeapTestUtils.testDecreaseKeyDefaultCompare(heapReferenceableBuilder(), seed);
	}

	@Test
	public void testDecreaseKeyCustomCompare() {
		final long seed = 0xd7d8cf9389480696L;
		ReferenceableHeapTestUtils.testDecreaseKeyCustomCompare(heapReferenceableBuilder(), seed);
	}

}
