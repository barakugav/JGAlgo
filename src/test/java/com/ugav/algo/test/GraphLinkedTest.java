package com.ugav.algo.test;

import com.ugav.algo.Graph;
import com.ugav.algo.GraphLinkedDirected;
import com.ugav.algo.GraphLinkedUndirected;
import com.ugav.algo.test.GraphImplTestUtils.GraphImpl;

public class GraphLinkedTest extends TestUtils {

	private static GraphImpl linkedImpl() {
		return new GraphImpl() {

			@Override
			public <E> Graph<E> newGraph(boolean directed, int... vertices) {
				if (vertices.length != 1)
					throw new UnsupportedOperationException();
				int n = vertices[0];
				if (directed) {
					return new GraphLinkedDirected<>(n);
				} else {
					return new GraphLinkedUndirected<>(n);
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
