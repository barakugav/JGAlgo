package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.GraphArrayDirected;
import com.ugav.jgalgo.GraphArrayUndirected;
import com.ugav.jgalgo.test.GraphImplTestUtils.GraphImpl;

public class GraphArrayTest extends TestUtils {

	private static GraphImpl graphImpl() {
		return new GraphImpl() {

			@Override
			public Graph newGraph(boolean directed, int vertices) {
				if (directed) {
					return new GraphArrayDirected(vertices);
				} else {
					return new GraphArrayUndirected(vertices);
				}
			}
		};
	}

	@Test
	public void testVertexAdd() {
		GraphImplTestUtils.testVertexAdd(graphImpl());
	}

	@Test
	public void testCreateWithNVertices() {
		GraphImplTestUtils.testCreateWithNVertices(graphImpl());
	}

	@Test
	public void testAddEdge() {
		GraphImplTestUtils.testAddEdge(graphImpl());
	}

	@Test
	public void testGetEdge() {
		GraphImplTestUtils.testGetEdge(graphImpl());
	}

	@Test
	public void testGetEdges() {
		GraphImplTestUtils.testGetEdges(graphImpl());
	}

	@Test
	public void testEdgeIter() {
		GraphImplTestUtils.testEdgeIter(graphImpl());
	}

	@Test
	public void testDgree() {
		GraphImplTestUtils.testDgree(graphImpl());
	}

	@Test
	public void testUndirectedMST() {
		final long seed = 0x6519a3d6cfdcaa15L;
		GraphImplTestUtils.testUndirectedMST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMDST() {
		final long seed = 0x93159a7004fabaabL;
		GraphImplTestUtils.testDirectedMDST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0xb3bd816cf5395102L;
		GraphImplTestUtils.testDirectedMaxFlow(graphImpl(), seed);
	}

	@Test
	public void testUndirectedBipartiteMatching() {
		final long seed = 0x40f90cfba5f21f7cL;
		GraphImplTestUtils.testUndirectedBipartiteMatching(graphImpl(), seed);
	}

	@Test
	public void testUndirectedBipartiteMatchingWeighted() {
		final long seed = 0x6a1920e2c7e46291L;
		GraphImplTestUtils.testUndirectedBipartiteMatchingWeighted(graphImpl(), seed);
	}

	@Test
	public void testUndirectedRandOps() {
		final long seed = 0xb49ae602bf9f9896L;
		GraphImplTestUtils.testUndirectedRandOps(graphImpl(), seed);
	}

}
