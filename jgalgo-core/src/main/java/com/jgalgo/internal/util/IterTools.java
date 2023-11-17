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
package com.jgalgo.internal.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class IterTools {

	private IterTools() {}

	/* syntax sugar to iterator for loops */
	public static <E> Iterable<E> foreach(Iterator<E> it) {
		return new Iterable<>() {
			@Override
			public Iterator<E> iterator() {
				return it;
			}
		};
	}

	/* syntax sugar to iterator for loops */
	public static IntIterable foreach(IntIterator it) {
		return new IntIterable() {
			@Override
			public IntIterator iterator() {
				return it;
			}
		};
	}

	public static interface IterPeekable<E> extends Iterator<E> {

		E peekNext();

		static interface Int extends IntIterator {

			int peekNextInt();
		}
	}

	static class IterPeekableImpl<E> implements IterPeekable<E> {

		private final Iterator<? super E> it;
		private Object nextElm;
		private static final Object nextNone = new Object();

		IterPeekableImpl(Iterator<? super E> it) {
			this.it = Objects.requireNonNull(it);
			advance();
		}

		private void advance() {
			if (it.hasNext()) {
				nextElm = it.next();
			} else {
				nextElm = nextNone;
			}
		}

		@Override
		public boolean hasNext() {
			return nextElm != null;
		}

		@Override
		public E next() {
			Assertions.Iters.hasNext(this);
			@SuppressWarnings("unchecked")
			E ret = (E) nextElm;
			advance();
			return ret;
		}

		@Override
		@SuppressWarnings("unchecked")
		public E peekNext() {
			Assertions.Iters.hasNext(this);
			return (E) nextElm;
		}

		static class Int implements IterPeekable.Int {

			private final IntIterator it;
			private int next;
			private boolean isNextValid;

			Int(IntIterator it) {
				this.it = Objects.requireNonNull(it);
				advance();
			}

			private void advance() {
				if (isNextValid = it.hasNext())
					next = it.nextInt();
			}

			@Override
			public boolean hasNext() {
				return isNextValid;
			}

			@Override
			public int nextInt() {
				Assertions.Iters.hasNext(this);
				int ret = next;
				advance();
				return ret;
			}

			@Override
			public int peekNextInt() {
				Assertions.Iters.hasNext(this);
				return next;
			}

		}

	}

	private static class MappedIter<A, B> implements Iterator<B> {
		private final Iterator<A> it;
		private final Function<A, B> map;

		MappedIter(Iterator<A> it, Function<A, B> map) {
			this.it = Objects.requireNonNull(it);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public B next() {
			return map.apply(it.next());
		}
	}

	public static <A, B> Iterator<B> map(Iterator<A> it, Function<A, B> map) {
		return new MappedIter<>(it, map);
	}

	private static class MappedIntIter implements IntIterator {
		private final IntIterator it;
		private final IntUnaryOperator map;

		MappedIntIter(IntIterator it, IntUnaryOperator map) {
			this.it = Objects.requireNonNull(it);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			return map.applyAsInt(it.nextInt());
		}
	}

	public static IntIterator mapInt(IntIterator it, IntUnaryOperator map) {
		return new MappedIntIter(it, map);
	}

}
