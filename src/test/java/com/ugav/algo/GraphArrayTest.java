package com.ugav.algo;

import org.junit.jupiter.api.Test;

import com.ugav.algo.GraphImplTestUtils.GraphImpl;

public class GraphArrayTest extends TestUtils {

	private static GraphImpl arrayImpl() {
		return new GraphImpl() {

			@Override
			public Graph newGraph(boolean directed, int... vertices) {
				if (directed) {
					if (vertices.length == 1) {
						return new GraphArrayDirected(vertices[0]);
					} else {
						return new GraphBipartiteArrayDirected(vertices[0], vertices[1]);
					}
				} else {
					if (vertices.length == 1) {
						return new GraphArrayUndirected(vertices[0]);
					} else {
						return new GraphBipartiteArrayUndirected(vertices[0], vertices[1]);
					}
				}
			}
		};

	}

	@Test
	public void testUndirectedMST() {
		GraphImplTestUtils.testUndirectedMST(arrayImpl());
	}

	@Test
	public void testDirectedMDST() {
		GraphImplTestUtils.testDirectedMDST(arrayImpl());
	}

	@Test
	public void testDirectedMaxFlow() {
		GraphImplTestUtils.testDirectedMaxFlow(arrayImpl());
	}

	@Test
	public void testUndirectedBipartiteMatching() {
		GraphImplTestUtils.testUndirectedBipartiteMatching(arrayImpl());
	}

	@Test
	public void testUndirectedBipartiteMatchingWeighted() {
		GraphImplTestUtils.testUndirectedBipartiteMatchingWeighted(arrayImpl());
	}

	@Test
	public void testUndirectedRandOps() {
		GraphImplTestUtils.testUndirectedRandOps(arrayImpl());
	}

}
