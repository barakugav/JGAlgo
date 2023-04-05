package com.jgalgo;

import java.util.Iterator;
import java.util.NoSuchElementException;

class LinkedList {

	static class Node<N extends Node<N>> {
		N next;
	}

	static <N extends Node<N>> N add(N head, N node) {
		node.next = head;
		return node;
	}

	static class Iter<N extends Node<N>> {
		N ptr;

		Iter(N head) {
			ptr = head;
		}

		boolean hasNextNode() {
			return ptr != null;
		}

		N nextNode() {
			if (!(hasNextNode()))
				throw new NoSuchElementException();
			N ret = ptr;
			ptr = ptr.next;
			return ret;
		}

		N pickNextNode() {
			if (!(hasNextNode()))
				throw new NoSuchElementException();
			return ptr;
		}
	}

	static class IterNodes<N extends Node<N>> extends Iter<N> implements Iterator<N> {

		IterNodes(N head) {
			super(head);
		}

		@Override
		public boolean hasNext() {
			return hasNextNode();
		}

		@Override
		public N next() {
			return nextNode();
		}

	}

}
