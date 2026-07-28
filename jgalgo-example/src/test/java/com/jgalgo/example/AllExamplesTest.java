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

package com.jgalgo.example;

import org.junit.jupiter.api.Test;

public class AllExamplesTest {

	@Test
	public void ShortestPathExample() {
		ShortestPathExample.main(mainArgs());
	}

	@Test
	public void BfsDfsExample() {
		BfsDfsExample.main(mainArgs());
		BfsDfsExample.main(mainArgs());
	}

	@Test
	public void ColoringExample() {
		ColoringExample.main(mainArgs());
	}

	@Test
	public void EdgeIterationExample() {
		EdgeIterationExample.main(mainArgs());
	}

	@Test
	public void MinimumSpanningTreeExample() {
		MinimumSpanningTreeExample.main(mainArgs());
	}

	@Test
	public void MaximumMatchingExample() {
		MaximumMatchingExample.main(mainArgs());
	}

	@Test
	public void LowestCommonAncestorExample() {
		LowestCommonAncestorExample.main(mainArgs());
		LowestCommonAncestorExample.main(mainArgs());
	}

	private static String[] mainArgs() {
		return new String[] { "executable_name" };
	}

}
