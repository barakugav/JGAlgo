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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class FIFOQueueNoReduceTest extends TestBase {

	@Test
	public void intQueueIterator() {
		FIFOQueueIntNoReduce queue = new FIFOQueueIntNoReduce();
		queue.enqueue(1);
		queue.enqueue(24);
		queue.enqueue(378);
		assertEquals(IntSet.of(1, 24, 378), intSet(queue.iterator()));
		queue.dequeueInt();
		assertEquals(IntSet.of(24, 378), intSet(queue.iterator()));
		queue.enqueue(5);
		assertEquals(IntSet.of(24, 378, 5), intSet(queue.iterator()));
		queue.dequeueInt();
		queue.dequeueInt();
		assertEquals(IntSet.of(5), intSet(queue.iterator()));
		queue.dequeueInt();
		assertEquals(IntSet.of(), intSet(queue.iterator()));

		queue.enqueue(1);
		queue.enqueue(24);
		queue.enqueue(378);

		IntIterator it1 = queue.iterator();
		assertEquals(2, it1.skip(2));
		assertTrue(it1.hasNext());
		assertEquals(378, it1.nextInt());
		assertFalse(it1.hasNext());

		IntIterator it2 = queue.iterator();
		assertEquals(1, it2.nextInt());
		assertEquals(2, it2.skip(2));
		assertFalse(it2.hasNext());

		IntIterator it3 = queue.iterator();
		assertEquals(1, it3.nextInt());
		assertEquals(24, it3.nextInt());
		assertEquals(1, it3.skip(2));
		assertFalse(it3.hasNext());

		IntIterator it4 = queue.iterator();
		assertThrows(IllegalArgumentException.class, () -> it4.skip(-1));
	}

	private static IntSet intSet(IntIterator it) {
		IntSet set = new IntOpenHashSet();
		while (it.hasNext())
			assertTrue(set.add(it.nextInt()));
		return set;
	}

	@Test
	public void longQueueIterator() {
		FIFOQueueLongNoReduce queue = new FIFOQueueLongNoReduce();
		queue.enqueue(1);
		queue.enqueue(24);
		queue.enqueue(378);
		assertEquals(LongSet.of(1, 24, 378), longSet(queue.iterator()));
		queue.dequeueLong();
		assertEquals(LongSet.of(24, 378), longSet(queue.iterator()));
		queue.enqueue(5);
		assertEquals(LongSet.of(24, 378, 5), longSet(queue.iterator()));
		queue.dequeueLong();
		queue.dequeueLong();
		assertEquals(LongSet.of(5), longSet(queue.iterator()));
		queue.dequeueLong();
		assertEquals(LongSet.of(), longSet(queue.iterator()));

		queue.enqueue(1);
		queue.enqueue(24);
		queue.enqueue(378);

		LongIterator it1 = queue.iterator();
		assertEquals(2, it1.skip(2));
		assertTrue(it1.hasNext());
		assertEquals(378, it1.nextLong());
		assertFalse(it1.hasNext());

		LongIterator it2 = queue.iterator();
		assertEquals(1, it2.nextLong());
		assertEquals(2, it2.skip(2));
		assertFalse(it2.hasNext());

		LongIterator it3 = queue.iterator();
		assertEquals(1, it3.nextLong());
		assertEquals(24, it3.nextLong());
		assertEquals(1, it3.skip(2));
		assertFalse(it3.hasNext());

		LongIterator it4 = queue.iterator();
		assertThrows(IllegalArgumentException.class, () -> it4.skip(-1));
	}

	private static LongSet longSet(LongIterator it) {
		LongSet set = new LongOpenHashSet();
		while (it.hasNext())
			assertTrue(set.add(it.nextLong()));
		return set;
	}

}
