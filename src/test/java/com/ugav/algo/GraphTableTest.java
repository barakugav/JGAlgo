package com.ugav.algo;

import org.junit.jupiter.api.Test;

import com.ugav.algo.GraphImplTestUtils.GraphImpl;

public class GraphTableTest extends TestUtils {

	private static GraphImpl tableImpl() {
		return new GraphImpl() {

			@Override
			public Graph newGraph(boolean directed, int... vertices) {
				if (vertices.length != 1)
					throw new UnsupportedOperationException();
				int n = vertices[0];
				return directed ? new GraphTableDirected(n) : new GraphTableUndirected(n);
			}
		};
	}

	@Test
	public void testUndirectedMST() {
		GraphImplTestUtils.testUndirectedMST(tableImpl());
	}

	@Test
	public void testDirectedMDST() {
		GraphImplTestUtils.testDirectedMDST(tableImpl());
	}

	@Test
	public void testDirectedMaxFlow() {
		GraphImplTestUtils.testDirectedMaxFlow(tableImpl());
	}

	@Test
	public void testUndirectedRandOps() {
		GraphImplTestUtils.testUndirectedRandOps(tableImpl());
	}

}
