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
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntArrays;

abstract class DynamicTreeSplayExtension implements DynamicTreeExtension {

	final ExtensionData data;
	DynamicTree dt;

	private DynamicTreeSplayExtension(ExtensionData data) {
		this.data = data;
	}

	void setDynamicTreeAlgo(DynamicTree dt) {
		if (this.dt != null)
			throw new IllegalStateException("extension was already used in some dynamic tree");
		this.dt = Objects.requireNonNull(dt);
	}

	void splay(SplayTree.BaseNode<?, ?> node) {
		if (dt instanceof DynamicTreeSplay)
			((DynamicTreeSplay) dt).splay((DynamicTreeSplay.SplayNode) node);
		else if (dt instanceof DynamicTreeSplayInt)
			((DynamicTreeSplayInt) dt).splay((DynamicTreeSplayInt.SplayNode) node);
		else
			throw new IllegalStateException();
	}

	abstract void initNode(SplayTree.BaseNode<?, ?> n);

	abstract void beforeCut(SplayTree.BaseNode<?, ?> n);

	abstract void afterLink(SplayTree.BaseNode<?, ?> n);

	abstract void beforeRotate(SplayTree.BaseNode<?, ?> n);

	static class TreeSize extends DynamicTreeSplayExtension.Int implements DynamicTreeExtension.TreeSize {

		/**
		 * Create a new Tree Size extensions.
		 * <p>
		 * Each instance of this extension should be used in a single dynamic tree object.
		 */
		TreeSize() {}

		@Override
		public int getTreeSize(DynamicTree.Node node) {
			SplayTree.BaseNode<?, ?> n = (SplayTree.BaseNode<?, ?>) node;
			splay(n);
			return getNodeData(n);
		}

		@Override
		void initNode(SplayTree.BaseNode<?, ?> n) {
			setNodeData(n, 1);
		}

		@Override
		void beforeCut(SplayTree.BaseNode<?, ?> n) {
			setNodeData(n, getNodeData(n) - getNodeData(n.right));
		}

		@Override
		void afterLink(SplayTree.BaseNode<?, ?> n) {
			SplayTree.BaseNode<?, ?> parent =
					(SplayTree.BaseNode<?, ?>) ((DynamicTree.Node) n).getParent();
			setNodeData(parent, getNodeData(parent) + getNodeData(n));
		}

		@Override
		void beforeRotate(SplayTree.BaseNode<?, ?> n) {
			SplayTree.BaseNode<?, ?> parent = n.parent;
			int nSize = getNodeData(n);
			int parentOldSize = getNodeData(parent);

			int parentNewSize;
			if (n.isLeftChild()) {
				parentNewSize = parentOldSize - nSize + (n.hasRightChild() ? getNodeData(n.right) : 0);
			} else {
				assert n.isRightChild();
				parentNewSize = parentOldSize - nSize + (n.hasLeftChild() ? getNodeData(n.left) : 0);
			}
			setNodeData(parent, parentNewSize);

			setNodeData(n, parentOldSize);
		}

	}

	private static abstract class Int extends DynamicTreeSplayExtension {

		Int() {
			super(new ExtensionData.Int());
		}

		private ExtensionData.Int data() {
			return (ExtensionData.Int) data;
		}

		int getNodeData(SplayTree.BaseNode<?, ?> n) {
			return data().get(((SplayNodeExtended) n).idx());
		}

		void setNodeData(SplayTree.BaseNode<?, ?> n, int data) {
			data().set(((SplayNodeExtended) n).idx(), data);
		}
	}

	static abstract class ExtensionData {
		abstract void swap(int idx1, int idx2);

		abstract void clear(int idx);

		abstract void expand(int newCapacity);

		static class Int extends ExtensionData {
			private int[] data;

			Int() {
				data = IntArrays.EMPTY_ARRAY;
			}

			int get(int idx) {
				return data[idx];
			}

			void set(int idx, int d) {
				data[idx] = d;
			}

			@Override
			void swap(int idx1, int idx2) {
				IntArrays.swap(data, idx1, idx2);
			}

			@Override
			void clear(int idx) {
				data[idx] = 0;
			}

			@Override
			void expand(int newCapacity) {
				data = Arrays.copyOf(data, newCapacity);
			}
		}
	}

	static interface SplayNodeExtended {
		int idx();
	}

}
