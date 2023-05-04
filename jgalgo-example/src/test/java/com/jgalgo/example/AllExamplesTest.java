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
		ShortestPathExample.shortestPathExample();
	}

	@Test
	public void BfsDfsExample() {
		BfsDfsExample.BFSExample();
		BfsDfsExample.DFSExample();
	}

	@Test
	public void ColoringExample() {
		ColoringExample.coloringExample();
	}

	@Test
	public void EdgeIterationExample() {
		EdgeIterationExample.edgeIterationExample();
	}

	@Test
	public void MinimumSpanningTreeExample() {
		MinimumSpanningTreeExample.MSTExample();
	}

	@Test
	public void MaximumMatchingExample() {
		MaximumMatchingExample.maximumMatchingExample();
	}

	@Test
	public void LowestCommonAncestorExample() {
		LowestCommonAncestorExample.staticLCAExample();
		LowestCommonAncestorExample.dynamicLCAExample();
	}

}
