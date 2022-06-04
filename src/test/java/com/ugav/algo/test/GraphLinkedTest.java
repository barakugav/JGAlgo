package com.ugav.algo.test;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.GraphLinkedDirected;
import com.ugav.algo.GraphLinkedUndirected;
import com.ugav.algo.test.GraphImplTestUtils.GraphImpl;

public class GraphLinkedTest extends TestUtils {

	private static GraphImpl linkedImpl() {
		return new GraphImpl() {

			@Override
			public <E> Graph<E> newGraph(DirectedType directedType, int... vertices) {
				if (vertices.length == 1) {
					int n = vertices[0];
					return directedType == DirectedType.Directed ? new GraphLinkedDirected<>(n)
							: new GraphLinkedUndirected<>(n);
				} else {
					throw new UnsupportedOperationException();
				}
			}
		};
	}

	@Test
	public static boolean testUndirectedMST() {
		return GraphImplTestUtils.testUndirectedMST(linkedImpl());
	}

	@Test
	public static boolean testDirectedMDST() {
		return GraphImplTestUtils.testDirectedMDST(linkedImpl());
	}

	@Test
	public static boolean testDirectedMaxFlow() {
		return GraphImplTestUtils.testDirectedMaxFlow(linkedImpl());
	}

}
