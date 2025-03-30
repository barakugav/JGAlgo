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
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

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

	public static interface Peek<E> extends Iterator<E> {

		static <E> Peek<E> of(Iterator<E> iter) {
			return new PeekImpl<>(iter);
		}

		E peekNext();

		static interface Int extends IntIterator, Peek<Integer> {

			static Peek.Int of(IntIterator iter) {
				return new PeekImpl.Int(iter);
			}

			int peekNextInt();

			@Deprecated
			@Override
			default Integer peekNext() {
				return Integer.valueOf(peekNextInt());
			}
		}
	}

	private static class PeekImpl<E> implements Peek<E> {

		private final Iterator<? super E> it;
		private Object nextElm;
		private static final Object nextNone = new Object();

		PeekImpl(Iterator<? super E> it) {
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
			return nextElm != nextNone;
		}

		@Override
		public E next() {
			Assertions.hasNext(this);
			@SuppressWarnings("unchecked")
			E ret = (E) nextElm;
			advance();
			return ret;
		}

		@Override
		@SuppressWarnings("unchecked")
		public E peekNext() {
			Assertions.hasNext(this);
			return (E) nextElm;
		}

		private static class Int implements Peek.Int {

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
				Assertions.hasNext(this);
				int ret = next;
				advance();
				return ret;
			}

			@Override
			public int peekNextInt() {
				Assertions.hasNext(this);
				return next;
			}

		}

	}

	private static class IterMapObjObj<A, B> implements ObjectIterator<B> {
		private final Iterator<A> it;
		private final Function<A, B> map;

		IterMapObjObj(Iterator<A> it, Function<A, B> map) {
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

		@Override
		public int skip(final int n) {
			return JGAlgoUtils.objIterSkip(it, n);
		}
	}

	public static <A, B> Iterator<B> map(Iterator<A> it, Function<A, B> map) {
		return new IterMapObjObj<>(it, map);
	}

	private static class IterMapIntObj<B> implements ObjectIterator<B> {
		private final IntIterator it;
		private final IntFunction<B> map;

		IterMapIntObj(IntIterator it, IntFunction<B> map) {
			this.it = Objects.requireNonNull(it);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public B next() {
			return map.apply(it.nextInt());
		}

		@Override
		public int skip(final int n) {
			return it.skip(n);
		}
	}

	public static <B> Iterator<B> map(IntIterator it, IntFunction<B> map) {
		return new IterMapIntObj<>(it, map);
	}

	private static class IterMapIntInt implements IntIterator {
		private final IntIterator it;
		private final IntUnaryOperator map;

		IterMapIntInt(IntIterator it, IntUnaryOperator map) {
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

		@Override
		public int skip(final int n) {
			return it.skip(n);
		}
	}

	public static IntIterator mapInt(IntIterator it, IntUnaryOperator map) {
		return new IterMapIntInt(it, map);
	}

	public static IntIterator filter(IntIterator it, IntPredicate pred) {
		return new IntIterator() {

			private int next;
			private boolean isNextValid;

			{
				advance();
			}

			private void advance() {
				for (isNextValid = false; it.hasNext();) {
					next = it.nextInt();
					if (pred.test(next)) {
						isNextValid = true;
						break;
					}
				}
			}

			@Override
			public boolean hasNext() {
				return isNextValid;
			}

			@Override
			public int nextInt() {
				Assertions.hasNext(this);
				int ret = next;
				advance();
				return ret;
			}
		};
	}

	public static <T> Stream<T> stream(Iterator<T> it) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false);
	}

	public static IntStream stream(IntIterator it) {
		return StreamSupport.intStream(Spliterators.spliteratorUnknownSize(it, 0), false);
	}

	public static IntStream stream(IntIterable it) {
		return stream(it.iterator());
	}

	public static int get(IntIterator it, int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("Index must be non-negative");
		if (index != 0)
			it.skip(index);
		if (!it.hasNext())
			throw new IndexOutOfBoundsException("Index out of bounds");
		return it.nextInt();
	}

}
