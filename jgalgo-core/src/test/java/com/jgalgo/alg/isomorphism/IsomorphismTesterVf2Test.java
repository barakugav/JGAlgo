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
package com.jgalgo.alg.isomorphism;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;

public class IsomorphismTesterVf2Test extends TestBase {

	@Test
	public void directedFullIsomorphism() {
		IsomorphismTestUtils.graphsIsomorphism(new IsomorphismTesterVf2(), false, true, true);
	}

	@Test
	public void undirectedFullIsomorphism() {
		IsomorphismTestUtils.graphsIsomorphism(new IsomorphismTesterVf2(), false, true, false);
	}

	@Test
	public void directedInducedSubGraphIsomorphism() {
		IsomorphismTestUtils.graphsIsomorphism(new IsomorphismTesterVf2(), true, true, true);
	}

	@Test
	public void undirectedInducedSubGraphIsomorphism() {
		IsomorphismTestUtils.graphsIsomorphism(new IsomorphismTesterVf2(), true, true, false);
	}

	@Test
	public void directedSubGraphIsomorphism() {
		IsomorphismTestUtils.graphsIsomorphism(new IsomorphismTesterVf2(), true, false, true);
	}

	@Test
	public void undirectedSubGraphIsomorphism() {
		IsomorphismTestUtils.graphsIsomorphism(new IsomorphismTesterVf2(), true, false, false);
	}

	@Test
	public void noVertices() {
		IsomorphismTestUtils.noVertices(new IsomorphismTesterVf2());
	}

	@Test
	public void noEdges() {
		IsomorphismTestUtils.noEdges(new IsomorphismTesterVf2());
	}

	@Test
	public void differentDegrees() {
		IsomorphismTestUtils.differentDegrees(new IsomorphismTesterVf2());
	}

	@Test
	public void differentVerticesNum() {
		IsomorphismTestUtils.differentVerticesNum(new IsomorphismTesterVf2());
	}

	@Test
	public void differentEdgesNum() {
		IsomorphismTestUtils.differentEdgesNum(new IsomorphismTesterVf2());
	}

	@Test
	public void differentDirectedUndirected() {
		IsomorphismTestUtils.differentDirectedUndirected(new IsomorphismTesterVf2());
	}

	@Test
	public void defaultImpl() {
		IsomorphismTester defAlgo = IsomorphismTester.newInstance();
		assertEquals(defAlgo.getClass(), IsomorphismTesterVf2.class);
	}

	@Test
	public void iterThrowsException() {
		IntGraph g1 = IntGraph.newDirected();
		g1.addVertex(0);
		g1.addVertex(1);
		g1.addEdge(0, 1);
		IntGraph g2 = IntGraph.newDirected();
		g2.addVertex(0);
		g2.addVertex(1);
		g2.addEdge(0, 1);
		IsomorphismTester algo = new IsomorphismTesterVf2();
		Iterator<IsomorphismMapping<Integer, Integer, Integer, Integer>> it = algo.isomorphicMappingsIter(g1, g2);
		while (it.hasNext())
			it.next();
		assertThrows(NoSuchElementException.class, () -> it.next());
		assertThrows(NoSuchElementException.class, () -> it.next());
	}

}
