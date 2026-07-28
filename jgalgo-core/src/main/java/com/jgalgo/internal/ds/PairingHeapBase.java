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
package com.jgalgo.internal.ds;

import java.util.Iterator;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class PairingHeapBase<NodeT extends PairingHeapBase.NodeBase<NodeT>> {

	NodeT minRoot;

	public boolean isEmpty() {
		return minRoot == null;
	}

	public boolean isNotEmpty() {
		return minRoot != null;
	}

	void cut(NodeT n) {
		NodeT next = n.next;
		if (next != null) {
			next.prevOrParent = n.prevOrParent;
			n.next = null;
		}
		if (n.prevOrParent.child == n) { /* n.parent.child == n */
			n.prevOrParent.child = next;
		} else {
			n.prevOrParent.next = next;
		}
		n.prevOrParent = null;
	}

	void addChild(NodeT parent, NodeT newChild) {
		assert newChild.prevOrParent == null;
		assert newChild.next == null;
		NodeT oldChild = parent.child;
		if (oldChild != null) {
			oldChild.prevOrParent = newChild;
			newChild.next = oldChild;
		}
		parent.child = newChild;
		newChild.prevOrParent = parent;
	}

	public void clear() {
		if (minRoot == null)
			return;

		for (NodeT p = minRoot;;) {
			while (p.child != null) {
				p = p.child;
				while (p.next != null)
					p = p.next;
			}
			p.clearKeyData();
			NodeT prev = p.prevOrParent;
			if (prev == null)
				break;
			p.prevOrParent = null;
			if (prev.next == p) {
				prev.next = null;
			} else {
				prev.child = null;
			}
			p = prev;
		}

		minRoot = null;
	}

	static class PreOrderIter<NodeT extends NodeBase<NodeT>> implements Iterator<NodeT> {

		private final Stack<NodeT> path = new ObjectArrayList<>();

		PreOrderIter(NodeT p) {
			if (p != null)
				path.push(p);
		}

		@Override
		public boolean hasNext() {
			return !path.isEmpty();
		}

		@Override
		public NodeT next() {
			Assertions.hasNext(this);
			final NodeT ret = path.top();

			NodeT next;
			if ((next = ret.child) != null) {
				path.push(next);
			} else {
				NodeT p0;
				do {
					p0 = path.pop();
					if ((next = p0.next) != null) {
						path.push(next);
						break;
					}
				} while (!path.isEmpty());
			}

			return ret;
		}

	}

	static class NodeBase<NodeT extends NodeBase<NodeT>> {
		NodeT prevOrParent;
		NodeT next;
		NodeT child;

		void clearKeyData() {}
	}

}
