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
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class IterToolsTest extends TestBase {

	@Test
	public void getByIndex() {
		assertEquals(1, IterTools.get(Fastutil.list(1, 2, 3).iterator(), 0));
		assertEquals(2, IterTools.get(Fastutil.list(1, 2, 3).iterator(), 1));
		assertEquals(3, IterTools.get(Fastutil.list(1, 2, 3).iterator(), 2));
		assertThrows(IndexOutOfBoundsException.class, () -> IterTools.get(Fastutil.list(1, 2, 3).iterator(), -1));
		assertThrows(IndexOutOfBoundsException.class, () -> IterTools.get(Fastutil.list(1, 2, 3).iterator(), 3));
	}

}
