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

public class RedBlackTreeTest extends TestBase {

	private static BinarySearchTree.Builder createBuilder() {
		return new BinarySearchTree.Builder() {

			@Override
			public <E> BinarySearchTree<E> build(Comparator<? super E> cmp) {
				return new RedBlackTree<>(cmp);
			}

			@Override
			public BinarySearchTree.Builder setSplits(boolean enable) {
				throw new UnsupportedOperationException("Unimplemented method 'setSplits'");
			}

			@Override
			public BinarySearchTree.Builder setMelds(boolean enable) {
				throw new UnsupportedOperationException("Unimplemented method 'setMelds'");
			}
		};
	}

	@Test
	public void testRandOpsDefaultCompare() {
		final long seed = 0x445a02434b767d0fL;
		HeapTestUtils.testRandOpsDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0xdf0d20c96b18e76bL;
		HeapTestUtils.testRandOpsCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0xe00c6e6c7bbdf827L;
		HeapTestUtils.testRandOpsAfterManyInserts(createBuilder(), seed);
	}

	@Test
	public void testExtractMax() {
		final long seed = 0x51537cb2cbca4774L;
		BinarySearchTreeTestUtils.testExtractMax(createBuilder(), seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0x3fbbd70b1c035dedL;
		HeapTestUtils.testMeldDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0xb8f57d62b2818583L;
		HeapTestUtils.testMeldCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testDecreaseKeyDefaultCompare() {
		final long seed = 0x3ad0758602c2f656L;
		HeapTestUtils.testDecreaseKeyDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testDecreaseKeyCustomCompare() {
		final long seed = 0x0edcd3dd97c5f867L;
		HeapTestUtils.testDecreaseKeyCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testFindSmallersDefaultCompare() {
		final long seed = 0x4c57d0d410b7e9d6L;
		BinarySearchTreeTestUtils.testFindSmallersDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testFindSmallersCustomCompare() {
		final long seed = 0x73810ca9d38884ecL;
		BinarySearchTreeTestUtils.testFindSmallersCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testFindGreatersDefaultCompare() {
		final long seed = 0xec49fe38ca3ca19bL;
		BinarySearchTreeTestUtils.testFindGreatersDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testFindGreatersCustomCompare() {
		final long seed = 0x5939e3cdc2c3fb8fL;
		BinarySearchTreeTestUtils.testFindGreatersCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testGetPredecessorDefaultCompare() {
		final long seed = 0x941838c5890fae32L;
		BinarySearchTreeTestUtils.testGetPredecessorsDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testGetPredecessorCustomCompare() {
		final long seed = 0x6eec0c5157093ce4L;
		BinarySearchTreeTestUtils.testGetPredecessorsCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testGetSuccessorDefaultCompare() {
		final long seed = 0xc68b22dd9e9afed9L;
		BinarySearchTreeTestUtils.testGetSuccessorsDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testGetSuccessorCustomCompare() {
		final long seed = 0x89f85333bcca14e0L;
		BinarySearchTreeTestUtils.testGetSuccessorsCustomCompare(createBuilder(), seed);
	}

}
