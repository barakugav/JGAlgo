package com.ugav.algo;

import org.junit.jupiter.api.Test;

import com.ugav.algo.GraphImplTestUtils.GraphImpl;

public class GraphLinkedTest extends TestUtils {

	private static GraphImpl linkedImpl() {
		return new GraphImpl() {

			@Override
			public Graph newGraph(boolean directed, int... vertices) {
				if (vertices.length != 1)
					throw new UnsupportedOperationException();
				int n = vertices[0];
				if (directed) {
					return new GraphLinkedDirected(n);
				} else {
					return new GraphLinkedUndirected(n);
				}
			}
		};
	}

	@Test
	public void testUndirectedMST() {
		GraphImplTestUtils.testUndirectedMST(linkedImpl());
	}

	@Test
	public void testDirectedMDST() {
		GraphImplTestUtils.testDirectedMDST(linkedImpl());
	}

	@Test
	public void testDirectedMaxFlow() {
		GraphImplTestUtils.testDirectedMaxFlow(linkedImpl());
	}

	@Test
	public void testUndirectedRandOps() {
		GraphImplTestUtils.testUndirectedRandOps(linkedImpl());
	}

}
