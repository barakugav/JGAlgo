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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.IterTools;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.ObjectIterables;

@SuppressWarnings("boxing")
public class RedBlackTreeExtendedTest extends TestBase {

	@Test
	public void testExtensionSizeRandOps() {
		final long seed = 0xe5136a0085e719d1L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final IntComparator compare = null;
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(256);
		tester.addPhase().withArgs(64, 128).repeat(128);
		tester.addPhase().withArgs(512, 1024).repeat(64);
		tester.addPhase().withArgs(4096, 8096).repeat(16);
		tester.addPhase().withArgs(16384, 32768).repeat(8);
		tester.run((n, m) -> {
			RedBlackTreeExtension.Size<Integer, Void> sizeExt = new RedBlackTreeExtension.Size<>();
			ObjObjRedBlackTree<Integer, Void> tree = new RedBlackTreeExtended<>(compare, List.of(sizeExt));

			IntReferenceableHeapTestUtils
					.testHeap(intRefHeapFromObjObjRefHeap(tree), n, m, IntReferenceableHeapTestUtils.TestMode.Normal,
							false, compare, seedGen.nextSeed());

			for (ObjObjReferenceableHeap.Ref<Integer, Void> node : tree) {
				int expectedSize = (int) ObjectIterables.size(IterTools.foreach(tree.subTreeIterator(node)));
				int actualSize = sizeExt.getSubTreeSize(node);
				assertEquals(expectedSize, actualSize, "Size extension reported wrong value");
			}
		});
	}

	@Test
	public void testExtensionMinRandOps() {
		final long seed = 0xe5136a0085e719d1L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final IntComparator compare = null;
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(64);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(32);
		tester.addPhase().withArgs(4096, 8096).repeat(8);
		tester.addPhase().withArgs(16384, 32768).repeat(4);
		tester.run((n, m) -> {
			RedBlackTreeExtension.Min<Integer, Void> minExt = new RedBlackTreeExtension.Min<>();
			ObjObjRedBlackTree<Integer, Void> tree = new RedBlackTreeExtended<>(compare, List.of(minExt));

			IntReferenceableHeapTestUtils
					.testHeap(intRefHeapFromObjObjRefHeap(tree), n, m, IntReferenceableHeapTestUtils.TestMode.Normal,
							false, compare, seedGen.nextSeed());

			for (ObjObjReferenceableHeap.Ref<Integer, Void> node : tree) {
				int expectedMin = Integer.MAX_VALUE;
				for (ObjObjReferenceableHeap.Ref<Integer, Void> descendant : IterTools
						.foreach(tree.subTreeIterator(node)))
					expectedMin = Math.min(expectedMin, descendant.key());

				int actualMin = minExt.getSubTreeMin(node).key();
				assertEquals(expectedMin, actualMin, "Min extension reported wrong value");
			}
		});
	}

	@Test
	public void testExtensionMaxRandOps() {
		final long seed = 0x7674bddef0a0863bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final IntComparator compare = null;
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(64);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(32);
		tester.addPhase().withArgs(4096, 8096).repeat(8);
		tester.addPhase().withArgs(16384, 32768).repeat(4);
		tester.run((n, m) -> {
			RedBlackTreeExtension.Max<Integer, Void> maxExt = new RedBlackTreeExtension.Max<>();
			ObjObjRedBlackTree<Integer, Void> tree = new RedBlackTreeExtended<>(compare, List.of(maxExt));

			IntReferenceableHeapTestUtils
					.testHeap(intRefHeapFromObjObjRefHeap(tree), n, m, IntReferenceableHeapTestUtils.TestMode.Normal,
							false, compare, seedGen.nextSeed());
			for (ObjObjReferenceableHeap.Ref<Integer, Void> node : tree) {
				int expectedMax = Integer.MIN_VALUE;
				for (ObjObjReferenceableHeap.Ref<Integer, Void> descendant : IterTools
						.foreach(tree.subTreeIterator(node)))
					expectedMax = Math.max(expectedMax, descendant.key());

				int actualMax = maxExt.getSubTreeMax(node).key();
				assertEquals(expectedMax, actualMax, "Max extension reported wrong value");
			}
		});
	}

	private static IntReferenceableHeap intRefHeapFromObjObjRefHeap(ObjObjReferenceableHeap<Integer, Void> h) {
		return new IntReferenceableHeap() {

			final Map<IntReferenceableHeap.Ref, ObjObjReferenceableHeap.Ref<Integer, Void>> refIntToObj =
					new IdentityHashMap<>();
			final Map<ObjObjReferenceableHeap.Ref<Integer, Void>, IntReferenceableHeap.Ref> refObjToRef =
					new IdentityHashMap<>();

			@Override
			public void clear() {
				h.clear();
			}

			@Override
			public boolean isEmpty() {
				return h.isEmpty();
			}

			@Override
			public boolean isNotEmpty() {
				return h.isNotEmpty();
			}

			@Override
			public Iterator<IntReferenceableHeap.Ref> iterator() {
				return IterTools.map(h.iterator(), refObjToRef::get);
			}

			@Override
			public IntReferenceableHeap.Ref insert(int key) {
				ObjObjReferenceableHeap.Ref<Integer, Void> objRef = h.insert(key);
				IntReferenceableHeap.Ref intRef = new IntReferenceableHeap.Ref() {
					@Override
					public int key() {
						return objRef.key().intValue();
					}

					@Override
					public String toString() {
						return String.valueOf(key());
					}
				};
				refObjToRef.put(objRef, intRef);
				refIntToObj.put(intRef, objRef);
				return intRef;
			}

			@Override
			public IntReferenceableHeap.Ref findMin() {
				return refObjToRef.get(h.findMin());
			}

			@Override
			public IntReferenceableHeap.Ref extractMin() {
				ObjObjReferenceableHeap.Ref<Integer, Void> objRef = h.extractMin();
				IntReferenceableHeap.Ref intRef = refObjToRef.remove(objRef);
				refIntToObj.remove(intRef);
				return intRef;
			}

			@Override
			public void meld(IntReferenceableHeap heap) {
				throw new UnsupportedOperationException("Unimplemented method 'meld'");
			}

			@Override
			public IntComparator comparator() {
				return (IntComparator) h.comparator();
			}

			@Override
			public void decreaseKey(IntReferenceableHeap.Ref ref, int newKey) {
				h.decreaseKey(refIntToObj.get(ref), newKey);
			}

			@Override
			public void increaseKey(IntReferenceableHeap.Ref ref, int newKey) {
				h.increaseKey(refIntToObj.get(ref), newKey);
			}

			@Override
			public void remove(IntReferenceableHeap.Ref ref) {
				ObjObjReferenceableHeap.Ref<Integer, Void> objRef = refIntToObj.remove(ref);
				refObjToRef.remove(objRef);
				h.remove(objRef);
			}

			@Override
			public IntReferenceableHeap.Ref find(int key) {
				ObjObjReferenceableHeap.Ref<Integer, Void> objRef = h.find(key);
				return objRef == null ? null : refObjToRef.get(objRef);
			}
		};
	}

}
