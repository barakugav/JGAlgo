package com.ugav.algo;

import com.ugav.algo.GraphImplTestUtils.GraphImpl;

public class GraphLinkedTest extends TestUtils {

	private static GraphImpl linkedImpl() {
		return new GraphImpl() {

			@Override
			public <E> Graph<E> newGraph(boolean directed, int... vertices) {
				if (vertices.length != 1)
					throw new UnsupportedOperationException();
				int n = vertices[0];
				if (directed) {
					return new GraphLinkedDirectedOld<>(n);
				} else {
					return new GraphLinkedUndirectedOld<>(n);
				}
			}
		};
	}

	@Test
	public static void testUndirectedMST() {
		GraphImplTestUtils.testUndirectedMST(linkedImpl());
	}

	@Test
	public static void testDirectedMDST() {
		GraphImplTestUtils.testDirectedMDST(linkedImpl());
	}

	@Test
	public static void testDirectedMaxFlow() {
		GraphImplTestUtils.testDirectedMaxFlow(linkedImpl());
	}

}
