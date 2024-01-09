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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class PermutationsTest extends TestBase {

	@Test
	public void permutation0() {
		foreachBoolConfig(intList -> {
			IntList l = IntList.of();
			Iterable<List<Integer>> permutations;
			if (intList) {
				permutations = Permutations.of((List<Integer>) l);
			} else {
				permutations = Permutations.of(new ArrayList<>(l));
			}
			Set<List<Integer>> actual = new ObjectOpenHashSet<>(permutations.iterator());
			Set<IntList> expected = Set.of();
			assertEquals(expected, actual);
		});
	}

	@Test
	public void permutation1() {
		foreachBoolConfig(intList -> {
			IntList l = IntList.of(3);
			Iterable<List<Integer>> permutations;
			if (intList) {
				permutations = Permutations.of((List<Integer>) l);
			} else {
				permutations = Permutations.of(new ArrayList<>(l));
			}
			Set<List<Integer>> actual = new ObjectOpenHashSet<>(permutations.iterator());
			Set<IntList> expected = Set.of(IntList.of(3));
			assertEquals(expected, actual);
		});
	}

	@Test
	public void permutation2() {
		foreachBoolConfig(intList -> {
			IntList l = IntList.of(3, 7);
			Iterable<List<Integer>> permutations;
			if (intList) {
				permutations = Permutations.of((List<Integer>) l);
			} else {
				permutations = Permutations.of(new ArrayList<>(l));
			}
			Set<List<Integer>> actual = new ObjectOpenHashSet<>(permutations.iterator());
			Set<IntList> expected = Set.of(IntList.of(3, 7), IntList.of(7, 3));
			assertEquals(expected, actual);
		});
	}

	@Test
	public void permutation3() {
		foreachBoolConfig(intList -> {
			IntList l = IntList.of(3, 7, 12);
			Iterable<List<Integer>> permutations;
			if (intList) {
				permutations = Permutations.of((List<Integer>) l);
			} else {
				permutations = Permutations.of(new ArrayList<>(l));
			}
			Set<List<Integer>> actual = new ObjectOpenHashSet<>(permutations.iterator());
			Set<IntList> expected = new HashSet<>();
			expected.add(IntList.of(3, 7, 12));
			expected.add(IntList.of(3, 12, 7));
			expected.add(IntList.of(7, 3, 12));
			expected.add(IntList.of(7, 12, 3));
			expected.add(IntList.of(12, 3, 7));
			expected.add(IntList.of(12, 7, 3));
			assertEquals(expected, actual);
		});
	}

	@Test
	public void permutation4() {
		foreachBoolConfig(intList -> {
			IntList l = IntList.of(3, 7, 12, 8);
			Iterable<List<Integer>> permutations;
			if (intList) {
				permutations = Permutations.of((List<Integer>) l);
			} else {
				permutations = Permutations.of(new ArrayList<>(l));
			}
			Set<IntList> actual = new TreeSet<>();
			for (List<Integer> p : permutations) {
				boolean added = actual.add(new IntArrayList(p));
				assertTrue(added, "duplicate permutation: " + p);
			}
			Set<IntList> expected = new TreeSet<>();
			expected.add(IntList.of(3, 7, 12, 8));
			expected.add(IntList.of(3, 7, 8, 12));
			expected.add(IntList.of(3, 12, 7, 8));
			expected.add(IntList.of(3, 12, 8, 7));
			expected.add(IntList.of(3, 8, 7, 12));
			expected.add(IntList.of(3, 8, 12, 7));
			expected.add(IntList.of(7, 3, 12, 8));
			expected.add(IntList.of(7, 3, 8, 12));
			expected.add(IntList.of(7, 12, 3, 8));
			expected.add(IntList.of(7, 12, 8, 3));
			expected.add(IntList.of(7, 8, 3, 12));
			expected.add(IntList.of(7, 8, 12, 3));
			expected.add(IntList.of(12, 3, 7, 8));
			expected.add(IntList.of(12, 3, 8, 7));
			expected.add(IntList.of(12, 7, 3, 8));
			expected.add(IntList.of(12, 7, 8, 3));
			expected.add(IntList.of(12, 8, 3, 7));
			expected.add(IntList.of(12, 8, 7, 3));
			expected.add(IntList.of(8, 3, 7, 12));
			expected.add(IntList.of(8, 3, 12, 7));
			expected.add(IntList.of(8, 7, 3, 12));
			expected.add(IntList.of(8, 7, 12, 3));
			expected.add(IntList.of(8, 12, 3, 7));
			expected.add(IntList.of(8, 12, 7, 3));
			assertEquals(expected, actual);
		});
	}

}
