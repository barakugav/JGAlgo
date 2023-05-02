package com.jgalgo;

import java.util.Comparator;

import org.junit.jupiter.api.Test;

public class SplayTreeTest extends TestBase {

	private static BinarySearchTree.Builder createBuilder() {
		return new BinarySearchTree.Builder() {

			@Override
			public <E> BinarySearchTree<E> build(Comparator<? super E> cmp) {
				return new SplayTree<>(cmp);
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
		final long seed = 0x29080f5f2aca1605L;
		HeapTestUtils.testRandOpsDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0xa92d44b8205fbbdeL;
		HeapTestUtils.testRandOpsCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x38f7e3242f52e2dcL;
		HeapTestUtils.testRandOpsAfterManyInserts(createBuilder(), seed);
	}

	@Test
	public void testExtractMax() {
		final long seed = 0xb6921b23fa734769L;
		BinarySearchTreeTestUtils.testExtractMax(createBuilder(), seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0xe882a11221a54a22L;
		HeapTestUtils.testMeldDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0xfeab1714f2e57cd4L;
		HeapTestUtils.testMeldCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testMeldWithOrderDefaultCompare() {
		final long seed = 0x0cb4bf9251d8145bL;
		HeapTestUtils.testMeldWithOrderedValuesDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testMeldWithOrderCustomCompare() {
		final long seed = 0x24c1f56e5fdbc5acL;
		HeapTestUtils.testMeldWithOrderedValuesCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testSplitDefaultCompare() {
		final long seed = 0x353e23967b348089L;
		BinarySearchTreeTestUtils.testSplitDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testSplitCustomCompare() {
		final long seed = 0x40b238cf34d778c0L;
		BinarySearchTreeTestUtils.testSplitCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testDecreaseKeyDefaultCompare() {
		final long seed = 0x2e6a8902f634f8caL;
		HeapTestUtils.testDecreaseKeyDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testDecreaseKeyCustomCompare() {
		final long seed = 0xb1db9f0001ff6a5aL;
		HeapTestUtils.testDecreaseKeyCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testFindSmallersDefaultCompare() {
		final long seed = 0x77f393a0a7508c84L;
		BinarySearchTreeTestUtils.testFindSmallersDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testFindSmallersCustomCompare() {
		final long seed = 0x99a37616f1023b0fL;
		BinarySearchTreeTestUtils.testFindSmallersCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testFindGreatersDefaultCompare() {
		final long seed = 0xf8ec8ed64600635fL;
		BinarySearchTreeTestUtils.testFindGreatersDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testFindGreaterCustomCompare() {
		final long seed = 0xf890218f3f5420a9L;
		BinarySearchTreeTestUtils.testFindGreatersCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testGetPredecessorDefaultCompare() {
		final long seed = 0x09395f66760a5c55L;
		BinarySearchTreeTestUtils.testGetPredecessorsDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testGetPredecessorCustomCompare() {
		final long seed = 0x2f8fd18ab64a2b15L;
		BinarySearchTreeTestUtils.testGetPredecessorsCustomCompare(createBuilder(), seed);
	}

	@Test
	public void testGetSuccessorDefaultCompare() {
		final long seed = 0x7ec6e57911f958c1L;
		BinarySearchTreeTestUtils.testGetSuccessorsDefaultCompare(createBuilder(), seed);
	}

	@Test
	public void testGetSuccessorCustomCompare() {
		final long seed = 0x782385e30e24c822L;
		BinarySearchTreeTestUtils.testGetSuccessorsCustomCompare(createBuilder(), seed);
	}

}
