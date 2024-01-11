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

package com.jgalgo.alg;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Objects;

/**
 * Gabow implementation for dynamic LCA data structure with \(O(\log^2 n)\) amortized time for {@code addLeaf()}
 * operation.
 *
 * <p>
 * The running time of this algorithm for \(m\) operations over \(n\) vertices is \(O(m + n \log^2 n)\) and it uses
 * linear space. More specifically, the {@link #addLeaf(LowestCommonAncestorDynamic.Vertex)} operation is perform in
 * \(O(\log^2 n)\) amortized time and
 * {@link #findLowestCommonAncestor(LowestCommonAncestorDynamic.Vertex, LowestCommonAncestorDynamic.Vertex)} is perform
 * in constant time.
 *
 * <p>
 * This implementation is used by the better linear LCA algorithm {@link LowestCommonAncestorDynamicGabowInts} and
 * {@link LowestCommonAncestorDynamicGabowLongs} and rarely should be used directly.
 *
 * <p>
 * Based on the simpler data structure presented in 'Data Structures for Weighted Matching and Nearest Common Ancestors
 * with Linking' by Harold N. Gabow (1990).
 *
 * @author Barak Ugav
 */
class LowestCommonAncestorDynamicGabowSimple implements LowestCommonAncestorDynamic {

	private int verticesNum;
	private final CharacteristicAncestors reusedResultObj = new CharacteristicAncestors(null, null, null);

	/* Hyper parameters */
	private static final double alpha = 6.0 / 5.0;
	private static final double beta = 2 / (2 * alpha - 1);
	private static final int e = 4;
	private static final int c = 5;

	private static final VertexImpl[] EMPTY_VERTICES_ARR = new VertexImpl[0];

	/**
	 * Create a new dynamic LCA data structure that contains zero vertices.
	 */
	LowestCommonAncestorDynamicGabowSimple() {
		verticesNum = 0;
	}

	@Override
	public Vertex initTree() {
		if (size() != 0)
			throw new IllegalStateException("Tree already initialized");
		return newVertex(null);
	}

	@Override
	public Vertex addLeaf(Vertex parent) {
		return newVertex((VertexImpl) Objects.requireNonNull(parent));
	}

	private Vertex newVertex(VertexImpl parent) {
		verticesNum++;
		VertexImpl vertex = new VertexImpl(parent);

		vertex.isApex = true;
		if (parent != null) {
			parent.addChild(vertex);
			vertex.cParent = parent.getPathApex();
		}

		VertexImpl lastAncestorRequireCompress = null;
		for (VertexImpl a = vertex; a != null; a = a.cParent) {
			a.size++;
			if (a.isRequireRecompress())
				lastAncestorRequireCompress = a;
		}
		assert lastAncestorRequireCompress != null;
		recompress(lastAncestorRequireCompress);

		return vertex;
	}

	private void recompress(VertexImpl subtreeRoot) {
		/* first, compute the size of each vertex subtree */
		computeSize(subtreeRoot);

		/* actual recompress */
		buildCompressedTree(subtreeRoot);
		assert subtreeRoot.isApex;

		/* recompute ancestor tables */
		computeAncestorTables(subtreeRoot);
	}

	private int computeSize(VertexImpl vertex) {
		int size = 1;
		for (int i : range(vertex.childrenNum))
			size += computeSize(vertex.children[i]);
		return vertex.size = size;
	}

	private void buildCompressedTree(VertexImpl vertex) {
		vertex.cParent = vertex.isRoot() ? null : vertex.parent.getPathApex();

		vertex.path = vertex.isApex ? EMPTY_VERTICES_ARR : null;
		vertex.pathSize = 0;
		vertex.pathIdx = vertex.getPathApex().pathSize;
		vertex.getPathApex().addToPath(vertex);

		vertex.sigma = vertex.isApex ? vertex.size : 1;
		double /* integer */ v = pow(vertex.sigma, e);
		vertex.idxLowerFat = vertex.isRoot() ? 0 : vertex.cParent.idxUpperFatMaxChild;
		vertex.idxUpperFat = vertex.idxLowerFat + c * v;
		vertex.idxLower = vertex.idxLowerFat + v;
		vertex.idxUpper = vertex.idxUpperFat - v;
		assert vertex.isRoot() || vertex.idxUpperFat <= vertex.cParent.idxUpperFat;
		if (!vertex.isRoot())
			vertex.cParent.idxUpperFatMaxChild = vertex.idxUpperFat;

		vertex.idxUpperFatMaxChild = vertex.idxLower + 1;

		for (int i : range(vertex.childrenNum)) {
			VertexImpl child = vertex.children[i];
			child.isApex = child.size <= vertex.size / 2;
			buildCompressedTree(child);
		}
	}

	private void computeAncestorTables(VertexImpl vertex) {
		int ancestorTableSize = logBetaFloor(c * pow(verticesNum, e));
		vertex.ancestorTableInit(ancestorTableSize);

		int tableIdx = 0;
		for (VertexImpl a = vertex, last = null;; a = a.cParent) {
			for (; (c - 2) * pow(a.sigma, e) >= pow(beta, tableIdx); tableIdx++)
				if (last != null)
					vertex.ancestorTable[tableIdx] = last;
			if (a.isRoot()) {
				for (; tableIdx < ancestorTableSize; tableIdx++)
					if ((c - 2) * pow(a.sigma, e) < pow(beta, tableIdx))
						vertex.ancestorTable[tableIdx] = a;
				break;
			}
			last = a;
		}

		for (int i : range(vertex.childrenNum))
			computeAncestorTables(vertex.children[i]);
	}

	private static double /* integer */ pow(double /* integer */ a, double /* integer */ b) {
		return assertOverflowDouble(Math.pow(a, b));
	}

	private static final double LogBetaInv = 1 / Math.log(beta);

	private static int logBetaFloor(double x) {
		return assertOverflowInt(Math.floor(Math.log(x) * LogBetaInv));
	}

	private static double assertOverflowDouble(double x) {
		assert Double.isFinite(x);
		return x;
	}

	private static int assertOverflowInt(double x) {
		assert Integer.MIN_VALUE <= x && x <= Integer.MAX_VALUE;
		return (int) x;
	}

	private static void calcCACompressed(VertexImpl x, VertexImpl y, CharacteristicAncestors res) {
		if (x == y) {
			res.a = res.ax = res.ay = x;
			return;
		}
		int i = logBetaFloor(Math.abs(x.idxLower - y.idxLower));

		VertexImpl[] a = new VertexImpl[2];
		VertexImpl[] az = new VertexImpl[2];
		for (int zIdx : range(2)) {
			VertexImpl z = zIdx == 0 ? x : y;
			VertexImpl z0 = zIdx == 0 ? y : x;

			VertexImpl v = z.ancestorTable[i];
			VertexImpl w = v != null ? v.cParent : z;

			VertexImpl b, bz;
			if ((c - 2) * pow(w.sigma, e) > Math.abs(x.idxLower - y.idxLower)) {
				b = w;
				bz = v != null ? v : z;
			} else {
				b = w.cParent;
				bz = w;
			}

			if (b.idxLower <= z0.idxLower && z0.idxLower < b.idxUpper) { /* b is an ancestor of z0 */
				a[zIdx] = b;
				az[zIdx] = bz;
			} else {
				a[zIdx] = b.cParent;
				az[zIdx] = b;
			}
		}

		VertexImpl ax = az[0], ay = az[1];
		assert a[0] == a[1];
		assert ax == a[0] || ax.cParent == a[0];
		assert ay == a[0] || ay.cParent == a[0];
		res.a = a[0];
		res.ax = ax;
		res.ay = ay;
	}

	void calcCA(Vertex x0, Vertex y0, CharacteristicAncestors res) {
		VertexImpl x = (VertexImpl) x0, y = (VertexImpl) y0;
		if (x == y) {
			res.a = res.ax = res.ay = x;
			return;
		}
		calcCACompressed(x, y, res);
		CharacteristicAncestors cac = res;

		/* c is an apex of path P */
		VertexImpl c = (VertexImpl) cac.a, cx = (VertexImpl) cac.ax, cy = (VertexImpl) cac.ay;
		assert c == c.getPathApex();

		/* bz is the first ancestor of cz on P */
		VertexImpl bx = cx != c && cx.isApex ? cx.parent : cx;
		VertexImpl by = cy != c && cy.isApex ? cy.parent : cy;
		assert c == bx.getPathApex();
		assert c == by.getPathApex();

		/* a is the shallower vertex of bx and by */
		VertexImpl a = bx.pathIdx < by.pathIdx ? bx : by;

		VertexImpl ax = a != bx ? a.getPathApex().path[a.pathIdx + 1] : cx;
		VertexImpl ay = a != by ? a.getPathApex().path[a.pathIdx + 1] : cy;

		assert ax == a || ax.parent == a;
		assert ay == a || ay.parent == a;
		res.a = a;
		res.ax = ax;
		res.ay = ay;
	}

	@Override
	public Vertex findLowestCommonAncestor(Vertex x, Vertex y) {
		calcCA(x, y, reusedResultObj);
		return reusedResultObj.a;
	}

	@Override
	public int size() {
		return verticesNum;
	}

	@Override
	public void clear() {
		verticesNum = 0;
	}

	private static class VertexImpl implements Vertex {
		/* --- user tree data --- */
		Object vertexData;
		/* tree parent */
		final VertexImpl parent;
		/* children vertices of this vertex */
		VertexImpl[] children;
		int childrenNum;
		/* number of vertices in subtree */
		int size;

		/* --- compressed tree data --- */
		/* parent in the compressed tree */
		VertexImpl cParent;
		/* If the vertex is apex, contains all the vertices in it's path, else null */
		VertexImpl[] path;
		int pathSize;
		/* Index of the vertex within it's path */
		int pathIdx;
		/* p */
		double /* integer */ idxLower;
		/* q */
		double /* integer */ idxUpper;
		/* p bar */
		double /* integer */ idxLowerFat;
		/* q bar */
		double /* integer */ idxUpperFat;
		/* Q bar */
		double /* integer */ idxUpperFatMaxChild;
		/* sigma */
		int sigma;
		/* flag for head (shallower) of path vertex */
		boolean isApex;
		/* ancestor table */
		VertexImpl[] ancestorTable;

		VertexImpl(VertexImpl parent) {
			this.parent = parent;
			children = EMPTY_VERTICES_ARR;
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

		boolean isRoot() {
			assert !(parent == null ^ cParent == null);
			return parent == null;
		}

		void addChild(VertexImpl c) {
			if (childrenNum >= children.length)
				children = Arrays.copyOf(children, Math.max(children.length * 2, 2));
			children[childrenNum++] = c;
		}

		VertexImpl getPathApex() {
			return isApex ? this : cParent;
		}

		void addToPath(VertexImpl c) {
			if (pathSize >= path.length)
				path = Arrays.copyOf(path, Math.max(path.length * 2, 2));
			path[pathSize++] = c;
		}

		boolean isRequireRecompress() {
			return size >= alpha * sigma;
		}

		void ancestorTableInit(int size) {
			if (ancestorTable != null && ancestorTable.length >= size)
				Arrays.fill(ancestorTable, null);
			else
				ancestorTable = new VertexImpl[size];
		}

		@Override
		public String toString() {
			return "V" + (isApex ? "*" : "") + "<" + getData() + ">";
		}

		// void clear() {
		// parent = cParent = null;
		// Arrays.fill(children, 0, childrenNum, null);
		// Arrays.fill(path, 0, pathSize, null);
		// Arrays.fill(ancestorTable, null);
		// children = path = ancestorTable = null;
		// }

	}

	static class CharacteristicAncestors {
		Vertex a, ax, ay;

		CharacteristicAncestors(Vertex a, Vertex ax, Vertex ay) {
			this.a = a;
			this.ax = ax;
			this.ay = ay;
		}
	}

}
