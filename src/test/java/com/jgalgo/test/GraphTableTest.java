package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.Graph;
import com.jgalgo.GraphTableDirected;
import com.jgalgo.GraphTableUndirected;
import com.jgalgo.test.GraphImplTestUtils.GraphImpl;

public class GraphTableTest extends TestBase {

	private static GraphImpl graphImpl() {
		return new GraphImpl() {

			@Override
			public Graph newGraph(boolean directed, int vertices) {
				return directed ? new GraphTableDirected(vertices) : new GraphTableUndirected(vertices);
			}
		};
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
		final long seed = 0x63a396934a49021cL;
		GraphImplTestUtils.testUndirectedMST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMDST() {
		final long seed = 0xcebe72e8015778c1L;
		GraphImplTestUtils.testDirectedMDST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0xe2e6e3d11dfaa9dfL;
		GraphImplTestUtils.testDirectedMaxFlow(graphImpl(), seed);
	}

	@Test
	public void testRandOpsUndirected() {
		final long seed = 0x2aee685276834043L;
		GraphImplTestUtils.testRandOps(graphImpl(), false, seed);
	}

	@Test
	public void testRandOpsDirected() {
		final long seed = 0x4cd9a3bcb63cf8f8L;
		GraphImplTestUtils.testRandOps(graphImpl(), true, seed);
	}

}
