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
package com.jgalgo.graph;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import it.unimi.dsi.fastutil.ints.IntSet;

class IdBuilders {

	private IdBuilders() {}

	static final Supplier<IdBuilderInt> DefaultIntIdFactory = () -> new IdBuilderInt() {
		private int counter;

		@Override
		public int build(IntSet existingIds) {
			for (int id;;)
				if (!existingIds.contains(id = ++counter))
					return id;
		}
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <K> Supplier<IdBuilder<K>> defaultFactory(Class<K> idType) {
		if (idType == int.class || idType == Integer.class) {
			return (Supplier) DefaultIntIdFactory;

		} else if (idType == byte.class || idType == Byte.class) {
			return (Supplier) DefaultByteFactory.Instance;

		} else if (idType == short.class || idType == Short.class) {
			return (Supplier) DefaultShortFactory.Instance;

		} else if (idType == long.class || idType == Long.class) {
			return (Supplier) DefaultLongFactory.Instance;

		} else if (idType == float.class || idType == Float.class) {
			return (Supplier) DefaultFloatFactory.Instance;

		} else if (idType == double.class || idType == Double.class) {
			return (Supplier) DefaultDoubleFactory.Instance;

		} else if (idType == String.class) {
			return (Supplier) DefaultStringFactory.Instance;
		}

		try {
			final Constructor<K> constructor = idType.getDeclaredConstructor();
			boolean isPublic =
					Modifier.isPublic(idType.getModifiers()) && Modifier.isPublic(constructor.getModifiers());
			if (!isPublic && !constructor.canAccess(null))
				constructor.setAccessible(true);
			Object testId = constructor.newInstance();
			assert testId != null;

			return () -> existingIds -> {
				K newId;
				try {
					newId = constructor.newInstance();
				} catch (ReflectiveOperationException ex) {
					throw new RuntimeException("Id builder failed to instantiate a new id", ex);
				}
				if (existingIds.contains(newId))
					throw new IllegalArgumentException(
							"Default builder failed to generate a *unique* new id: " + newId);
				return newId;
			};
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("no default builder for type: " + idType, e);
		}
	}

	/* we want to have a single instance of bytes default factory, using static nested class create it lazily */
	private static class DefaultByteFactory {
		static final Supplier<IdBuilder<Byte>> Instance;

		static {
			long min = Byte.MIN_VALUE, max = Byte.MAX_VALUE, maxEdgesSize = 1 << Byte.SIZE;
			Instance = defaultPrimitiveFactory(min, max, maxEdgesSize, x -> Byte.valueOf((byte) x));
		}
	}

	/* we want to have a single instance of shorts default factory, using static nested class create it lazily */
	private static class DefaultShortFactory {
		static final Supplier<IdBuilder<Short>> Instance;

		static {
			long min = Short.MIN_VALUE, max = Short.MAX_VALUE, maxEdgesSize = 1 << Short.SIZE;
			Instance = defaultPrimitiveFactory(min, max, maxEdgesSize, x -> Short.valueOf((short) x));
		}
	}

	/* we want to have a single instance of longs default factory, using static nested class create it lazily */
	private static class DefaultLongFactory {
		static final Supplier<IdBuilder<Long>> Instance;

		static {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			Instance = defaultPrimitiveFactory(min, max, maxEdgesSize, x -> Long.valueOf(x));
		}
	}

	/* we want to have a single instance of floats default factory, using static nested class create it lazily */
	private static class DefaultFloatFactory {
		static final Supplier<IdBuilder<Float>> Instance;

		static {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			Instance = defaultPrimitiveFactory(min, max, maxEdgesSize, x -> Float.valueOf(x));
		}
	}

	/* we want to have a single instance of doubles default factory, using static nested class create it lazily */
	private static class DefaultDoubleFactory {
		static final Supplier<IdBuilder<Double>> Instance;

		static {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			Instance = defaultPrimitiveFactory(min, max, maxEdgesSize, x -> Double.valueOf(x));
		}
	}

	/* we want to have a single instance of strings default factory, using static nested class create it lazily */
	private static class DefaultStringFactory {
		static final Supplier<IdBuilder<String>> Instance;

		static {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			Instance = defaultPrimitiveFactory(min, max, maxEdgesSize, x -> Long.toString(x));
		}
	}

	private static <E> Supplier<IdBuilder<E>> defaultPrimitiveFactory(long minVal, long maxVal, long maxEdgesSize,
			LongFunction<E> idBuilder) {
		return () -> new IdBuilder<>() {
			long nextId;

			@Override
			public E build(Set<E> existingEdges) {
				if (existingEdges.size() >= maxEdgesSize)
					throw new IllegalArgumentException("too many edges");
				for (E id;;)
					if (!existingEdges.contains(id = idBuilder.apply(getAndInc())))
						return id;
			}

			private long getAndInc() {
				long ret = nextId;
				if (nextId < maxVal) {
					nextId++;
				} else {
					nextId = minVal;
				}
				return ret;
			}
		};
	}

}
