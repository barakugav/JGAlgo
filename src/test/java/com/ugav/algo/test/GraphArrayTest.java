package com.ugav.algo.test;

import com.ugav.algo.Graph;
import com.ugav.algo.GraphArrayDirected;
import com.ugav.algo.GraphArrayUndirected;
import com.ugav.algo.GraphBipartiteArrayDirected;
import com.ugav.algo.GraphBipartiteArrayUndirected;
import com.ugav.algo.test.GraphImplTestUtils.GraphImpl;

public class GraphArrayTest extends TestUtils {

	private static GraphImpl arrayImpl() {
		return new GraphImpl() {

			@Override
			public <E> Graph<E> newGraph(boolean directed, int... vertices) {
				if (directed) {
					if (vertices.length == 1) {
						return new GraphArrayDirected<>(vertices[0]);
					} else {
						return new GraphBipartiteArrayDirected<>(vertices[0], vertices[1]);
					}
				} else {
					if (vertices.length == 1) {
						return new GraphArrayUndirected<>(vertices[0]);
					} else {
						return new GraphBipartiteArrayUndirected<>(vertices[0], vertices[1]);
					}
				}
			}
		};

	}

	@Test
	public static void testUndirectedMST() {
		GraphImplTestUtils.testUndirectedMST(arrayImpl());
	}

	@Test
	public static void testDirectedMDST() {
		GraphImplTestUtils.testDirectedMDST(arrayImpl());
	}

	@Test
	public static void testDirectedMaxFlow() {
		GraphImplTestUtils.testDirectedMaxFlow(arrayImpl());
	}

	@Test
	public static void testUndirectedBipartiteMatching() {
		GraphImplTestUtils.testUndirectedBipartiteMatching(arrayImpl());
	}

	@Test
	public static void testUndirectedBipartiteMatchingWeighted() {
		GraphImplTestUtils.testUndirectedBipartiteMatchingWeighted(arrayImpl());
	}

}
