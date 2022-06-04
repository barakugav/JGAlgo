package com.ugav.algo.test;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.GraphArray;
import com.ugav.algo.GraphBipartiteArray;
import com.ugav.algo.test.GraphImplTestUtils.GraphImpl;

public class GraphArrayTest extends TestUtils {

	private static GraphImpl arrayImpl() {
		return new GraphImpl() {

			@Override
			public <E> Graph<E> newGraph(DirectedType directedType, int... vertices) {
				if (vertices.length == 1) {
					return new GraphArray<>(directedType, vertices[0]);
				} else {
					return new GraphBipartiteArray<>(directedType, vertices[0], vertices[1]);
				}
			}
		};
	}

	@Test
	public static boolean testUndirectedMST() {
		return GraphImplTestUtils.testUndirectedMST(arrayImpl());
	}

	@Test
	public static boolean testDirectedMDST() {
		return GraphImplTestUtils.testDirectedMDST(arrayImpl());
	}

	@Test
	public static boolean testDirectedMaxFlow() {
		return GraphImplTestUtils.testDirectedMaxFlow(arrayImpl());
	}

	@Test
	public static boolean testUndirectedBipartiteMatching() {
		return GraphImplTestUtils.testUndirectedBipartiteMatching(arrayImpl());
	}

	@Test
	public static boolean testUndirectedBipartiteMatchingWeighted() {
		return GraphImplTestUtils.testUndirectedBipartiteMatchingWeighted(arrayImpl());
	}

}
