package com.ugav.algo;

import com.ugav.algo.GraphImplTestUtils.GraphImpl;

public class GraphTableTest extends TestUtils {

	private static GraphImpl tableImpl() {
		return new GraphImpl() {

			@Override
			public <E> Graph<E> newGraph(boolean directed, int... vertices) {
				if (vertices.length != 1)
					throw new UnsupportedOperationException();
				int n = vertices[0];
				return directed ? new GraphTableDirected<>(n) : new GraphTableUndirected<>(n);
			}
		};
	}

	@Test
	public static void testUndirectedMST() {
		GraphImplTestUtils.testUndirectedMST(tableImpl());
	}

	@Test
	public static void testDirectedMDST() {
		GraphImplTestUtils.testDirectedMDST(tableImpl());
	}

	@Test
	public static void testDirectedMaxFlow() {
		GraphImplTestUtils.testDirectedMaxFlow(tableImpl());
	}

}
