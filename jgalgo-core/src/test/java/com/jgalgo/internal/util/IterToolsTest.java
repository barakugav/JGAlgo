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
import java.util.Iterator;
import java.util.Random;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class IterToolsTest extends TestBase {

	@Test
	public void getByIndex() {
		assertEquals(1, IterTools.get(Fastutil.list(1, 2, 3).iterator(), 0));
		assertEquals(2, IterTools.get(Fastutil.list(1, 2, 3).iterator(), 1));
		assertEquals(3, IterTools.get(Fastutil.list(1, 2, 3).iterator(), 2));
		assertThrows(IndexOutOfBoundsException.class, () -> IterTools.get(Fastutil.list(1, 2, 3).iterator(), -1));
		assertThrows(IndexOutOfBoundsException.class, () -> IterTools.get(Fastutil.list(1, 2, 3).iterator(), 3));
	}

	@SuppressWarnings("boxing")
	@Test
	public void map() {
		final Random rand = new Random(0x2e716a7ed8d8937dL);
		for (int type : range(3)) {
			Function<IntIterator, Iterator<Integer>> createIter = orig0 -> {
				IntIterator orig = (IntIterator) orig0;
				if (type == 0) {
					return IterTools.map(orig, (IntFunction<Integer>) x -> 100 + x);
				} else if (type == 1) {
					return IterTools.map(orig, (Function<Integer, Integer>) x -> 100 + (Integer) x);
				} else {
					assert type == 2;
					return IterTools.mapInt(orig, x -> 100 + x);
				}
			};
			IntList list = new IntArrayList(range(10));

			{
				IntIterator it1 = list.iterator();
				Iterator<Integer> it2 = createIter.apply(list.iterator());
				for (;;) {
					assertEqualsBool(it1.hasNext(), it2.hasNext());
					if (!it1.hasNext())
						break;
					assertEquals(100 + it1.nextInt(), (int) it2.next());
				}
			}
			for (int repeat = 0; repeat < 10; repeat++) {
				IntIterator it1 = list.iterator();
				Iterator<Integer> it2 = createIter.apply(list.iterator());
				for (;;) {
					assertEqualsBool(it1.hasNext(), it2.hasNext());
					if (!it1.hasNext())
						break;
					if (rand.nextBoolean()) {
						assertEquals(100 + it1.nextInt(), it2.next());
					} else {
						int skip = rand.nextInt(5);
						int skipped1 = it1.skip(skip);
						int skipped2 = it2 instanceof IntIterator ? ((IntIterator) it2).skip(skip)
								: ((ObjectIterator<?>) it2).skip(skip);
						assertEquals(skipped1, skipped2);
					}
				}
			}

		}
	}

}
