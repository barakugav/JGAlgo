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

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class MaximumMatchingWeightedBlossoms {

	private MaximumMatchingWeightedBlossoms() {}

	static class Blossom<B extends Blossom<B>> {

		/* base vertex of this blossom */
		int base;

		/* parent blossom, null if top blossom */
		B parent;

		/* child blossom, null if trivial blossom (blossom of one vertex) */
		B child;

		/*
		 * left brother in current sub blossoms level (share parent), null if top blossom
		 */
		B left;

		/*
		 * right brother in current sub blossoms level (share parent), null if top blossom
		 */
		B right;

		/* field used to keep track which blossoms were visited during traverse */
		int lastVisitIdx;

		boolean isSingleton() {
			return child == null;
		}

		Iterable<B> children() {
			return new Iterable<>() {

				@Override
				public Iterator<B> iterator() {
					if (child == null)
						return Collections.emptyIterator();
					return new Iterator<>() {

						final B begin = child;
						B c = begin;

						@Override
						public boolean hasNext() {
							return c != null;
						}

						@Override
						public B next() {
							if (!hasNext())
								throw new NoSuchElementException();
							B ret = c;
							c = ret.right;
							if (c == begin)
								c = null;
							return ret;
						}
					};
				}
			};
		}

		IntIterator vertices() {
			if (isSingleton())
				return IntIterators.singleton(base);
			return new IntIterator() {

				int next;
				final Stack<Iterator<B>> stack = new ObjectArrayList<>();
				{
					for (Iterator<B> it = children().iterator();;) {
						B c = it.next();
						if (it.hasNext())
							stack.push(it);
						if (c.isSingleton()) {
							next = c.base;
							break;
						}
						it = c.children().iterator();
					}
				}

				@Override
				public boolean hasNext() {
					return next != -1;
				}

				@Override
				public int nextInt() {
					if (!hasNext())
						throw new NoSuchElementException();
					int ret = next;

					if (!stack.isEmpty()) {
						for (Iterator<B> it = stack.pop();;) {
							B c = it.next();
							if (it.hasNext())
								stack.push(it);
							if (c.isSingleton()) {
								next = c.base;
								break;
							}
							it = c.children().iterator();
						}
					} else {
						next = -1;
					}

					return ret;
				}

			};
		}

	}

	static <B extends Blossom<B>> Iterator<B> allBlossoms(B[] blossoms, final int visitIdx) {
		return new Iterator<>() {
			final int n = blossoms.length;
			int v = 0;
			B b = blossoms[v];

			@Override
			public boolean hasNext() {
				return b != null;
			}

			@Override
			public B next() {
				if (!hasNext())
					throw new NoSuchElementException();
				B ret = b;
				ret.lastVisitIdx = visitIdx;

				b = b.parent;
				if (b == null || b.lastVisitIdx == visitIdx) {
					if (++v < n) {
						b = blossoms[v];
					} else {
						b = null;
					}
				}
				return ret;
			}
		};
	}

	static <B extends Blossom<B>> Iterator<B> topBlossoms(B[] blossoms, final int visitIdx) {
		return new Iterator<>() {

			final int n = blossoms.length;
			int v = 0;
			B b;

			{
				for (b = blossoms[v]; b.parent != null;) {
					b.lastVisitIdx = visitIdx;
					b = b.parent;
				}
			}

			@Override
			public boolean hasNext() {
				return b != null;
			}

			@Override
			public B next() {
				if (!hasNext())
					throw new NoSuchElementException();
				B ret = b;
				ret.lastVisitIdx = visitIdx;

				nextSingleton: for (;;) {
					if (++v >= n) {
						b = null;
						break;
					}
					for (b = blossoms[v]; b.parent != null;) {
						b.lastVisitIdx = visitIdx;
						b = b.parent;
						if (b.lastVisitIdx == visitIdx)
							continue nextSingleton;
					}
					break;
				}

				return ret;
			}
		};
	}

	static class Evens<B extends Blossom<B>> {

		/*
		 * Union find data structure for even blossoms, used with findToBlossoms: findToBlossoms[uf.find(v)]
		 */
		final UnionFind uf;

		/* uf result -> blossom */
		final Object[] findToBlossoms;

		Evens(int n) {
			uf = new UnionFindArray(n);
			findToBlossoms = new Object[n];
		}

		void init(int n) {
			uf.clear();
			for (int i = 0; i < n; i++)
				uf.make();
		}

		void union(int u, int v) {
			uf.union(u, v);
		}

		void setBlossom(int v, B b) {
			findToBlossoms[uf.find(v)] = b;
		}

		@SuppressWarnings("unchecked")
		B findBlossom(int v) {
			return (B) findToBlossoms[uf.find(v)];
		}

	}

}
