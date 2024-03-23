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
package com.jgalgo.alg.hamilton;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.path.Path;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;

public class HamiltonianPathRubinTest extends TestBase {

	@Test
	public void hamiltonianPathsUndirected() {
		HamiltonianPathTestUtils.hamiltonianPaths(new HamiltonianPathRubin(), false);
	}

	@Test
	public void hamiltonianPathsDirected() {
		HamiltonianPathTestUtils.hamiltonianPaths(new HamiltonianPathRubin(), true);
	}

	@Test
	public void hamiltonianPathWithSourceTargetUndirected() {
		HamiltonianPathTestUtils.hamiltonianPathsWithSourceTarget(new HamiltonianPathRubin(), false);
	}

	@Test
	public void hamiltonianPathWithSourceTargetDirected() {
		HamiltonianPathTestUtils.hamiltonianPathsWithSourceTarget(new HamiltonianPathRubin(), true);
	}

	@Test
	public void hamiltonianCyclesUndirected() {
		HamiltonianPathTestUtils.hamiltonianCycles(new HamiltonianPathRubin(), false);
	}

	@Test
	public void hamiltonianCyclesDirected() {
		HamiltonianPathTestUtils.hamiltonianCycles(new HamiltonianPathRubin(), true);
	}

	@Test
	public void iterThrowsException() {
		HamiltonianPathAlgo algo = new HamiltonianPathRubin();
		IntGraph g = IntGraph.newDirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addEdge(0, 1);
		Iterator<Path<Integer, Integer>> it = algo.hamiltonianCyclesIter(g);
		while (it.hasNext())
			it.next();
		assertThrows(NoSuchElementException.class, () -> it.next());
		assertThrows(NoSuchElementException.class, () -> it.next());
	}

}
