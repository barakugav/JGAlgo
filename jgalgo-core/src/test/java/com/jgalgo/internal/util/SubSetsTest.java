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
package com.jgalgo.internal.util;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class SubSetsTest extends TestBase {

	@Test
	public void subsets0() {
		foreachBoolConfig(intSet -> {
			Set<IntList> subsets = collectSubsetsAndAssertUnique(SubSets.of(rangeSet(0, intSet)));
			Set<IntList> expected = new ObjectOpenHashSet<>();
			assertEquals(expected, subsets);
		});
	}

	@Test
	public void subsets4() {
		foreachBoolConfig(intSet -> {
			Set<IntList> subsets = collectSubsetsAndAssertUnique(SubSets.of(rangeSet(4, intSet)));
			Set<IntList> expected = new ObjectOpenHashSet<>();
			expected.add(IntList.of(0));
			expected.add(IntList.of(1));
			expected.add(IntList.of(2));
			expected.add(IntList.of(3));
			expected.add(IntList.of(0, 1));
			expected.add(IntList.of(0, 2));
			expected.add(IntList.of(0, 3));
			expected.add(IntList.of(1, 2));
			expected.add(IntList.of(1, 3));
			expected.add(IntList.of(2, 3));
			expected.add(IntList.of(0, 1, 2));
			expected.add(IntList.of(0, 1, 3));
			expected.add(IntList.of(0, 2, 3));
			expected.add(IntList.of(1, 2, 3));
			expected.add(IntList.of(0, 1, 2, 3));
			assertEquals(expected, subsets);
		});
	}

	@Test
	public void subsets4_1() {
		foreachBoolConfig(intSet -> {
			Set<IntList> subsets = collectSubsetsAndAssertUnique(SubSets.of(rangeSet(4, intSet), 1));
			Set<IntList> expected = new ObjectOpenHashSet<>();
			expected.add(IntList.of(0));
			expected.add(IntList.of(1));
			expected.add(IntList.of(2));
			expected.add(IntList.of(3));
			assertEquals(expected, subsets);
		});
	}

	@Test
	public void subsets4_2() {
		foreachBoolConfig(intSet -> {
			Set<IntList> subsets = collectSubsetsAndAssertUnique(SubSets.of(rangeSet(4, intSet), 2));
			Set<IntList> expected = new ObjectOpenHashSet<>();
			expected.add(IntList.of(0, 1));
			expected.add(IntList.of(0, 2));
			expected.add(IntList.of(0, 3));
			expected.add(IntList.of(1, 2));
			expected.add(IntList.of(1, 3));
			expected.add(IntList.of(2, 3));
			assertEquals(expected, subsets);
		});
	}

	@Test
	public void subsets4_3() {
		foreachBoolConfig(intSet -> {
			Set<IntList> subsets = collectSubsetsAndAssertUnique(SubSets.of(rangeSet(4, intSet), 3));
			Set<IntList> expected = new ObjectOpenHashSet<>();
			expected.add(IntList.of(0, 1, 2));
			expected.add(IntList.of(0, 1, 3));
			expected.add(IntList.of(0, 2, 3));
			expected.add(IntList.of(1, 2, 3));
			assertEquals(expected, subsets);
		});
	}

	@Test
	public void subsets5() {
		foreachBoolConfig(intSet -> {
			Set<IntList> subsets = collectSubsetsAndAssertUnique(SubSets.of(rangeSet(5, intSet)));
			Set<IntList> expected = new ObjectOpenHashSet<>();
			expected.add(IntList.of(0));
			expected.add(IntList.of(1));
			expected.add(IntList.of(2));
			expected.add(IntList.of(3));
			expected.add(IntList.of(4));
			expected.add(IntList.of(0, 1));
			expected.add(IntList.of(0, 2));
			expected.add(IntList.of(0, 3));
			expected.add(IntList.of(0, 4));
			expected.add(IntList.of(1, 2));
			expected.add(IntList.of(1, 3));
			expected.add(IntList.of(1, 4));
			expected.add(IntList.of(2, 3));
			expected.add(IntList.of(2, 4));
			expected.add(IntList.of(3, 4));
			expected.add(IntList.of(0, 1, 2));
			expected.add(IntList.of(0, 1, 3));
			expected.add(IntList.of(0, 1, 4));
			expected.add(IntList.of(0, 2, 3));
			expected.add(IntList.of(0, 2, 4));
			expected.add(IntList.of(0, 3, 4));
			expected.add(IntList.of(1, 2, 3));
			expected.add(IntList.of(1, 2, 4));
			expected.add(IntList.of(1, 3, 4));
			expected.add(IntList.of(2, 3, 4));
			expected.add(IntList.of(0, 1, 2, 3));
			expected.add(IntList.of(0, 1, 2, 4));
			expected.add(IntList.of(0, 1, 3, 4));
			expected.add(IntList.of(0, 2, 3, 4));
			expected.add(IntList.of(1, 2, 3, 4));
			expected.add(IntList.of(0, 1, 2, 3, 4));
			assertEquals(expected, subsets);
		});
	}

	@Test
	public void subsets5_2() {
		foreachBoolConfig(intSet -> {
			Set<IntList> subsets = collectSubsetsAndAssertUnique(SubSets.of(rangeSet(5, intSet), 2));
			Set<IntList> expected = new ObjectOpenHashSet<>();
			expected.add(IntList.of(0, 1));
			expected.add(IntList.of(0, 2));
			expected.add(IntList.of(0, 3));
			expected.add(IntList.of(0, 4));
			expected.add(IntList.of(1, 2));
			expected.add(IntList.of(1, 3));
			expected.add(IntList.of(1, 4));
			expected.add(IntList.of(2, 3));
			expected.add(IntList.of(2, 4));
			expected.add(IntList.of(3, 4));
			assertEquals(expected, subsets);
		});
	}

	@Test
	public void subsets5_3() {
		foreachBoolConfig(intSet -> {
			Set<IntList> subsets = collectSubsetsAndAssertUnique(SubSets.of(rangeSet(5, intSet), 3));
			Set<IntList> expected = new ObjectOpenHashSet<>();
			expected.add(IntList.of(0, 1, 2));
			expected.add(IntList.of(0, 1, 3));
			expected.add(IntList.of(0, 1, 4));
			expected.add(IntList.of(0, 2, 3));
			expected.add(IntList.of(0, 2, 4));
			expected.add(IntList.of(0, 3, 4));
			expected.add(IntList.of(1, 2, 3));
			expected.add(IntList.of(1, 2, 4));
			expected.add(IntList.of(1, 3, 4));
			expected.add(IntList.of(2, 3, 4));
			assertEquals(expected, subsets);
		});
	}

	@Test
	public void subsets5_4() {
		foreachBoolConfig(intSet -> {
			Set<IntList> subsets = collectSubsetsAndAssertUnique(SubSets.of(rangeSet(5, intSet), 4));
			Set<IntList> expected = new ObjectOpenHashSet<>();
			expected.add(IntList.of(0, 1, 2, 3));
			expected.add(IntList.of(0, 1, 2, 4));
			expected.add(IntList.of(0, 1, 3, 4));
			expected.add(IntList.of(0, 2, 3, 4));
			expected.add(IntList.of(1, 2, 3, 4));
			assertEquals(expected, subsets);
		});
	}

	@Test
	public void subsets5_5() {
		foreachBoolConfig(intSet -> {
			Set<IntList> subsets = collectSubsetsAndAssertUnique(SubSets.of(rangeSet(5, intSet), 5));
			Set<IntList> expected = new ObjectOpenHashSet<>();
			expected.add(IntList.of(0, 1, 2, 3, 4));
			assertEquals(expected, subsets);
		});
	}

	@Test
	public void subsets5_0() {
		foreachBoolConfig(intSet -> {
			Set<IntList> subsets = collectSubsetsAndAssertUnique(SubSets.of(rangeSet(5, intSet), 0));
			Set<IntList> expected = new ObjectOpenHashSet<>();
			assertEquals(expected, subsets);
		});
	}

	@Test
	public void invalidK() {
		assertThrows(IllegalArgumentException.class, () -> SubSets.of(rangeSet(5, true), -1));
		assertThrows(IllegalArgumentException.class, () -> SubSets.of(rangeSet(5, true), 8));
	}

	private static Set<IntList> collectSubsetsAndAssertUnique(Iterable<List<Integer>> subsetsIterable) {
		Set<IntList> subsets = new ObjectOpenHashSet<>();
		for (List<Integer> subset : subsetsIterable) {
			boolean modified = subsets.add(new IntArrayList(subset));
			assertTrue(modified);
		}
		return subsets;
	}

	private static List<Integer> rangeSet(int n, boolean intSet) {
		List<Integer> set = range(n).asList();
		if (!intSet)
			set = new ArrayList<>(set);
		return set;
	}

}
