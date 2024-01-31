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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;

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

}
