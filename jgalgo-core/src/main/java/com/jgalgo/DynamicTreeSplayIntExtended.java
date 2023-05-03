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

package com.jgalgo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import com.jgalgo.DynamicTreeSplayExtension.SplayNodeExtended;

/**
 * Dynamic trees with integer edges weights implementation using splay trees that support various extensions.
 * <p>
 * Some extensions such as {@link DynamicTreeSplayExtension.TreeSize} can be added to the tree without increasing the
 * asymptotical running time of any operation.
 *
 * @see    DynamicTreeSplayExtension
 * @author Barak Ugav
 */
class DynamicTreeSplayIntExtended extends DynamicTreeSplayInt {

	private Node[] nodes;
	private int nodesCount;
	private static final Node[] EmptyNodes = new Node[0];
	private final DynamicTreeSplayExtension[] extensions;

	/**
	 * Create a new empty dynamic tree data structure with extensions.
	 *
	 * @param  weightLimit              a limit on the weights of the edges. The limit is an upper bound on the sum of
	 *                                      each edge weight and the weights modification that are performed using
	 *                                      {@link #addWeight(com.jgalgo.DynamicTree.Node, double)}.
	 * @param  extensions               a collection of extensions the data structure will use. Each extension must not
	 *                                      be used in any other tree than this one.
	 * @throws IllegalArgumentException if the extensions collection is empty. In such a case, the regular
	 *                                      {@link DynamicTreeSplay} should be used
	 */
	DynamicTreeSplayIntExtended(int weightLimit, Collection<? extends DynamicTreeSplayExtension> extensions) {
		this(weightLimit, extensions.toArray(len -> new DynamicTreeSplayExtension[len]));
	}

	private DynamicTreeSplayIntExtended(int weightLimit, DynamicTreeSplayExtension[] extensions) {
		super(new SplayImplWithExtensions(extensions), weightLimit);
		nodes = EmptyNodes;
		if (extensions.length == 0)
			throw new IllegalArgumentException("No extensions provided. Use the regular Red Black tree.");
		this.extensions = extensions;
		for (DynamicTreeSplayExtension extension : extensions)
			extension.setDynamicTreeAlgo(this);
	}

	@Override
	Node newNode() {
		int idx = nodesCount++;
		if (idx >= nodes.length) {
			int newLen = Math.max(2, nodes.length * 2);
			nodes = Arrays.copyOf(nodes, newLen);
			for (DynamicTreeSplayExtension extension : extensions)
				extension.data.expand(newLen);
		}
		assert nodes[idx] == null;
		Node n = nodes[idx] = new Node(idx);
		for (DynamicTreeSplayExtension extension : extensions)
			extension.initNode(n);
		return n;
	}

	@Override
	public void clear() {
		Arrays.fill(nodes, 0, nodesCount, null);
		nodesCount = 0;
		super.clear();
	}

	@Override
	void beforeCut(SplayNode n) {
		super.beforeCut(n);
		for (DynamicTreeSplayExtension extension : extensions)
			extension.beforeCut(n);
	}

	@Override
	void afterLink(SplayNode n) {
		super.afterLink(n);
		for (DynamicTreeSplayExtension extension : extensions)
			extension.afterLink(n);
	}

	private static class Node extends DynamicTreeSplayInt.SplayNode implements SplayNodeExtended {
		int idx;

		Node(int idx) {
			this.idx = idx;
		}

		@Override
		public int idx() {
			return idx;
		}
	}

	static class SplayImplWithExtensions extends DynamicTreeSplayInt.SplayImplWithRelativeWeights {

		private final DynamicTreeSplayExtension[] extensions;

		SplayImplWithExtensions(DynamicTreeSplayExtension[] extensions) {
			this.extensions = Objects.requireNonNull(extensions);
		}

		@Override
		void beforeRotate(SplayNode n) {
			super.beforeRotate(n);
			for (DynamicTreeSplayExtension extension : extensions)
				extension.beforeRotate(n);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <Ext extends DynamicTreeExtension> Ext getExtension(Class<Ext> extensionType) {
		for (DynamicTreeSplayExtension ext : extensions)
			if (extensionType.isAssignableFrom(ext.getClass()))
				return (Ext) ext;
		return null;
	}

}
