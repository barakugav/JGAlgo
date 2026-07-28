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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class JGAlgoUtilsTest extends TestBase {

	@Test
	public void variant2OfA() {
		Variant2<String, Integer> v = Variant2.ofA("hello");
		assertTrue(v.contains(String.class));
		assertFalse(v.contains(Integer.class));
		assertNotEquals(Optional.empty(), v.getOptional(String.class));
		assertEquals(Optional.empty(), v.getOptional(Integer.class));
		assertEquals("hello", v.get(String.class));
		assertEquals("hello", v.map(s -> s, x -> String.valueOf(x)));
	}

	@Test
	public void variant2OfB() {
		Variant2<String, Integer> v = Variant2.ofB(Integer.valueOf(55));
		assertFalse(v.contains(String.class));
		assertTrue(v.contains(Integer.class));
		assertEquals(Optional.empty(), v.getOptional(String.class));
		assertNotEquals(Optional.empty(), v.getOptional(Integer.class));
		assertEquals(55, v.get(Integer.class));
		assertEquals("55", v.map(s -> s, x -> String.valueOf(x)));
	}

	@Test
	public void iterSkip() {
		foreachBoolConfig(objIter -> {
			List<Integer> list = objIter ? new ObjectArrayList<>() : new ArrayList<>();
			list.addAll(range(10));

			Iterator<Integer> it1 = list.iterator();
			assertEquals(0, it1.next());
			assertEquals(1, it1.next());
			assertEquals(3, JGAlgoUtils.objIterSkip(it1, 3));
			assertEquals(5, it1.next());
			assertEquals(2, JGAlgoUtils.objIterSkip(it1, 2));
			assertEquals(8, it1.next());
			assertEquals(1, JGAlgoUtils.objIterSkip(it1, 17));
			assertFalse(it1.hasNext());

			assertThrows(IllegalArgumentException.class, () -> JGAlgoUtils.objIterSkip(it1, -1));
		});
	}

	@Test
	public void iterBack() {
		foreachBoolConfig(objIter -> {
			List<Integer> list = objIter ? new ObjectArrayList<>() : new ArrayList<>();
			list.addAll(range(10));

			ListIterator<Integer> it1 = list.listIterator(list.size());
			assertEquals(9, it1.previous());
			assertEquals(8, it1.previous());
			// assertEquals(3, JGAlgoUtils.objIterBack(it1, 3)); // TODO Bug in fastutil
			assertEquals(1, JGAlgoUtils.objIterBack(it1, 1));
			assertEquals(2, JGAlgoUtils.objIterBack(it1, 2));
			assertEquals(4, it1.previous());
			assertEquals(2, JGAlgoUtils.objIterBack(it1, 2));
			assertEquals(1, it1.previous());
			if (!objIter) { // TODO Bug in fastutil
				assertEquals(1, JGAlgoUtils.objIterBack(it1, 17));
				assertFalse(it1.hasPrevious());
			}

			assertThrows(IllegalArgumentException.class, () -> JGAlgoUtils.objIterBack(it1, -1));
		});
	}

}
