/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo.alg.tree;

import java.util.Arrays;
import java.util.Objects;
import com.jgalgo.alg.tree.LowestCommonAncestorDynamicGabowSimple.CharacteristicAncestors;

/**
 * Gabow linear dynamic LCA data structure with 64bit word operations.
 *
 * <p>
 * The algorithm use {@link LowestCommonAncestorDynamicGabowSimple} as a base, but uses two layers of bit tricks to
 * remove the \(O(\log^2 n)\) factor of the simpler data structure. Each layer have less vertices than the previous one
 * by a factor of \(O(\log n)\), until the simpler data structure is used on \(O(n / \log^2 n)\) vertices. This
 * implementation is much faster in practice and always should be used over the simpler one.
 *
 * <p>
 * The running time of this algorithm for \(m\) operations is \(O(n + m)\) and it uses linear space. More specifically,
 * the {@link #addLeaf(LowestCommonAncestorDynamic.Vertex)} operation is perform in \(O(1)\) amortized time and
 * {@link #findLowestCommonAncestor(LowestCommonAncestorDynamic.Vertex, LowestCommonAncestorDynamic.Vertex)} is perform
 * in constant time.
 *
 * <p>
 * This class implements all the bit operations on {@code long} primitive. For a version that uses {@code int} see
 * {@link LowestCommonAncestorDynamicGabowInts}, which usually perform worse, but may perform better for (significantly)
 * smaller graphs.
 *
 * <p>
 * Based on 'Data Structures for Weighted Matching and Nearest Common Ancestors with Linking' by Harold N. Gabow (1990).
 *
 * @author Barak Ugav
 */
public class LowestCommonAncestorDynamicGabowLongs implements LowestCommonAncestorDynamic {

	/*
	 * implementation note: in the original paper, Gabow stated to use look tables for the bit tricks (lsb, msb). It's
	 * possible to do so, using BitsLookupTable, but the standard Java implementation already perform these operations
	 * in constant time (less than 10 operations).
	 */

	private int vertices2Num;
	private final LowestCommonAncestorDynamicGabowSimple lca0;
	private final CharacteristicAncestors lca0Result = new CharacteristicAncestors(null, null, null);

	private static final int SUB_TREE_MAX_SIZE = Long.SIZE;

	/**
	 * Create a new dynamic LCA data structure that contains zero vertices.
	 *
	 * <p>
	 * Please prefer using {@link LowestCommonAncestorDynamic#newInstance()} to get a default implementation for the
	 * {@link LowestCommonAncestorDynamic} interface.
	 */
	public LowestCommonAncestorDynamicGabowLongs() {
		lca0 = new LowestCommonAncestorDynamicGabowSimple();
	}

	@Override
	public Vertex initTree() {
		if (size() != 0)
			throw new IllegalStateException("Tree already initialized");
		return newVertex2(null);
	}

	@Override
	public Vertex addLeaf(Vertex parent) {
		return newVertex2((Vertex2) Objects.requireNonNull(parent));
	}

	private Vertex2 newVertex2(Vertex2 parent) {
		Vertex2 vertex = new Vertex2(parent);

		if (parent == null || parent.subTree.isFull()) {
			/* make the new vertex a root of a new sub tree */
			vertex.subTree = new Vertex1(vertex);
		} else {
			/* add new vertex to the parent sub tree */
			vertex.subTree = parent.subTree;
			vertex.ancestorsBitmap = parent.ancestorsBitmap;
		}

		vertex.subTree.addVertex(vertex);
		vertex.ancestorsBitmap |= ithBit(vertex.idWithinSubTree);

		if (vertex.subTree.isFull()) {
			/* new full sub tree, add to next level tree */
			Vertex2 topParent = vertex.subTree.top.parent;
			Vertex1 tparent = topParent != null ? topParent.subTree : null;
			addFullVertex1(vertex.subTree, tparent);
		}

		vertices2Num++;
		return vertex;
	}

	private void addFullVertex1(Vertex1 vertex, Vertex1 parent) {
		if (parent == null || parent.subTree.isFull()) {
			/* make the new vertex a root of a new sub tree */
			vertex.subTree = new Vertex0(vertex);
		} else {
			/* add new vertex to the parent sub tree */
			vertex.subTree = parent.subTree;
			vertex.ancestorsBitmap = parent.ancestorsBitmap;
		}

		vertex.subTree.addVertex(vertex);
		vertex.ancestorsBitmap |= ithBit(vertex.idWithinSubTree);

		if (vertex.subTree.isFull()) {
			/* new full sub tree, add to next level tree */
			Vertex1 topParent = vertex.subTree.top.getParent();
			Vertex0 tparent = topParent != null ? topParent.subTree : null;
			addFullVertex0(vertex.subTree, tparent);
		}
	}

	private void addFullVertex0(Vertex0 vertex, Vertex0 parent) {
		if (parent == null) {
			(vertex.lcaId = lca0.initTree()).setData(vertex);
		} else {
			(vertex.lcaId = lca0.addLeaf(parent.lcaId)).setData(vertex);
		}
	}

	private Vertex2 calcLca(Vertex2 x2, Vertex2 y2) {
		if (x2.subTree != y2.subTree) {
			if (!x2.subTree.isFull())
				x2 = x2.subTree.top.parent;
			if (!y2.subTree.isFull())
				y2 = y2.subTree.top.parent;

			/* Calculate CAs in the next level tree */
			Vertex1 x1 = x2.subTree, y1 = y2.subTree;
			Vertex1 ax1 = null, ay1 = null;
			if (x1.subTree != y1.subTree) {
				if (!x1.subTree.isFull()) {
					ax1 = x1.subTree.top;
					x1 = x1.subTree.top.getParent();
				}
				if (!y1.subTree.isFull()) {
					ay1 = y1.subTree.top;
					y1 = y1.subTree.top.getParent();
				}

				/* Calculate CAs in the next level tree */
				Vertex0 x0 = x1.subTree, y0 = y1.subTree;
				lca0.calcCA(x0.lcaId, y0.lcaId, lca0Result);
				CharacteristicAncestors ca0 = lca0Result;
				Vertex0 a0 = ca0.a.getData(), ax0 = ca0.ax.getData(), ay0 = ca0.ay.getData();

				if (a0 != ax0) {
					ax1 = ax0.top;
					x1 = ax0.top.getParent();
				}
				if (a0 != ay0) {
					ay1 = ay0.top;
					y1 = ay0.top.getParent();
				}
			}

			assert x1.subTree == y1.subTree;
			/* calculate LCA within sub tree */
			long commonAncestors1 = x1.ancestorsBitmap & y1.ancestorsBitmap;
			Vertex1 a1 = x1.subTree.vertices[63 - Long.numberOfLeadingZeros(commonAncestors1)];
			if (a1 != x1) {
				long x1UncommonAncestors = x1.ancestorsBitmap & ~y1.ancestorsBitmap;
				ax1 = x1.subTree.vertices[Long.numberOfTrailingZeros(x1UncommonAncestors)];
			} else if (ax1 == null) {
				ax1 = x1;
			}
			if (a1 != y1) {
				long x1UncommonAncestors = ~x1.ancestorsBitmap & y1.ancestorsBitmap;
				ay1 = x1.subTree.vertices[Long.numberOfTrailingZeros(x1UncommonAncestors)];
			} else if (ay1 == null) {
				ay1 = y1;
			}

			if (a1 != ax1)
				x2 = ax1.top.parent;
			if (a1 != ay1)
				y2 = ay1.top.parent;
		}

		assert x2.subTree == y2.subTree;
		/* calculate LCA within sub tree */
		long commonAncestors = x2.ancestorsBitmap & y2.ancestorsBitmap;
		return x2.subTree.vertices[63 - Long.numberOfLeadingZeros(commonAncestors)];
	}

	@Override
	public Vertex findLowestCommonAncestor(Vertex x, Vertex y) {
		return calcLca((Vertex2) x, (Vertex2) y);
	}

	@Override
	public int size() {
		return vertices2Num;
	}

	@Override
	public void clear() {
		lca0.clear();
		vertices2Num = 0;
	}

	private static class Vertex2 implements LowestCommonAncestorDynamic.Vertex {
		Object vertexData;

		/* level 2 info */
		final Vertex2 parent;
		Vertex1 subTree;
		byte idWithinSubTree;
		long ancestorsBitmap;

		Vertex2(Vertex2 parent) {
			this.parent = parent;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <D> D getData() {
			return (D) vertexData;
		}

		@Override
		public void setData(Object data) {
			vertexData = data;
		}

		@Override
		public Vertex getParent() {
			return parent;
		}

		// void clear() {
		// subTree = null;
		// }
	}

	private static class Vertex1 {
		/* level 2 info */
		final Vertex2 top;
		Vertex2[] vertices;
		byte size;

		/* level 1 info */
		Vertex0 subTree;
		byte idWithinSubTree;
		long ancestorsBitmap;

		Vertex1(Vertex2 top) {
			this.top = top;
			vertices = new Vertex2[4];
		}

		Vertex1 getParent() {
			return top.parent != null ? top.parent.subTree : null;
		}

		void addVertex(Vertex2 vertex) {
			assert size < SUB_TREE_MAX_SIZE;
			vertex.idWithinSubTree = size++;
			if (vertex.idWithinSubTree >= vertices.length)
				vertices = Arrays.copyOf(vertices, Math.max(vertices.length * 2, 2));
			vertices[vertex.idWithinSubTree] = vertex;
		}

		boolean isFull() {
			return size == SUB_TREE_MAX_SIZE;
		}

		// void clear() {
		// vertices = null;
		// subTree = null;
		// }
	}

	private static class Vertex0 {
		/* level 1 info */
		final Vertex1 top;
		Vertex1[] vertices;
		byte size;

		/* level 0 info */
		LowestCommonAncestorDynamic.Vertex lcaId;

		Vertex0(Vertex1 top) {
			this.top = top;
			vertices = new Vertex1[4];
		}

		void addVertex(Vertex1 vertex) {
			assert size < SUB_TREE_MAX_SIZE;
			vertex.idWithinSubTree = size++;
			if (vertex.idWithinSubTree >= vertices.length)
				vertices = Arrays.copyOf(vertices, Math.max(vertices.length * 2, 2));
			vertices[vertex.idWithinSubTree] = vertex;
		}

		boolean isFull() {
			return size == SUB_TREE_MAX_SIZE;
		}

		// void clear() {
		// vertices = null;
		// }
	}

	private static long ithBit(long b) {
		assert 0 <= b && b < Long.SIZE;
		return 1L << b;
	}

}
