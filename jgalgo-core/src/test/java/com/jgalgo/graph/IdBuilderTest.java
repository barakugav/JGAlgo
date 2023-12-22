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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class IdBuilderTest extends TestBase {

	@Test
	public void defaultBuilderByte() {
		foreachBoolConfig(boxedType -> {
			Set<Byte> ids = new ByteOpenHashSet();
			IdBuilder<Byte> builder = IdBuilder.defaultBuilder(boxedType ? Byte.class : byte.class);
			for (int i = 0; i < 100; i++)
				ids.add(builder.build(ids));
			assertEquals(100, ids.size());
		});
		foreachBoolConfig(boxedType -> {
			Set<Byte> ids = new ByteOpenHashSet();
			IdBuilder<Byte> builder = IdBuilder.defaultBuilder(boxedType ? Byte.class : byte.class);
			for (int i = 0; i < 256; i++)
				ids.add(builder.build(ids));
			assertEquals(256, ids.size());
			assertThrows(IllegalArgumentException.class, () -> builder.build(ids));
		});
	}

	@Test
	public void defaultBuilderShort() {
		foreachBoolConfig(boxedType -> {
			Set<Short> ids = new ShortOpenHashSet();
			IdBuilder<Short> builder = IdBuilder.defaultBuilder(boxedType ? Short.class : short.class);
			for (int i = 0; i < 100; i++)
				ids.add(builder.build(ids));
			assertEquals(100, ids.size());
		});
	}

	@Test
	public void defaultBuilderInt() {
		foreachBoolConfig(boxedType -> {
			Set<Integer> ids = new IntOpenHashSet();
			IdBuilder<Integer> builder = IdBuilder.defaultBuilder(boxedType ? Integer.class : int.class);
			for (int i = 0; i < 100; i++)
				ids.add(builder.build(ids));
			assertEquals(100, ids.size());
		});

		Set<Integer> ids = new IntOpenHashSet();
		ids.addAll(IntList.of(4, 65, 84));
		IdBuilder<Integer> builder = IdBuilderInt.defaultBuilder();
		for (int i = 0; i < 100; i++)
			ids.add(builder.build(ids));
		assertEquals(103, ids.size());
	}

	@Test
	public void defaultBuilderLong() {
		foreachBoolConfig(boxedType -> {
			Set<Long> ids = new LongOpenHashSet();
			IdBuilder<Long> builder = IdBuilder.defaultBuilder(boxedType ? Long.class : long.class);
			for (int i = 0; i < 100; i++)
				ids.add(builder.build(ids));
			assertEquals(100, ids.size());
		});

		Set<Long> ids = new LongOpenHashSet();
		ids.addAll(LongList.of(4, 65, 84));
		IdBuilder<Long> builder = IdBuilder.defaultBuilder(long.class);
		for (int i = 0; i < 100; i++)
			ids.add(builder.build(ids));
		assertEquals(103, ids.size());
	}

	@Test
	public void defaultBuilderFloat() {
		foreachBoolConfig(boxedType -> {
			Set<Float> ids = new FloatOpenHashSet();
			IdBuilder<Float> builder = IdBuilder.defaultBuilder(boxedType ? Float.class : float.class);
			for (int i = 0; i < 100; i++)
				ids.add(builder.build(ids));
			assertEquals(100, ids.size());
		});
	}

	@Test
	public void defaultBuilderDouble() {
		foreachBoolConfig(boxedType -> {
			Set<Double> ids = new DoubleOpenHashSet();
			IdBuilder<Double> builder = IdBuilder.defaultBuilder(boxedType ? Double.class : double.class);
			for (int i = 0; i < 100; i++)
				ids.add(builder.build(ids));
			assertEquals(100, ids.size());
		});
	}

	@Test
	public void defaultBuilderString() {
		Set<String> ids = new ObjectOpenHashSet<>();
		IdBuilder<String> builder = IdBuilder.defaultBuilder(String.class);
		for (int i = 0; i < 100; i++)
			ids.add(builder.build(ids));
		assertEquals(100, ids.size());
	}

	@Test
	public void defaultBuilderObject() {
		Set<Object> ids = new ObjectOpenHashSet<>();
		IdBuilder<Object> builder = IdBuilder.defaultBuilder(Object.class);
		for (int i = 0; i < 100; i++)
			ids.add(builder.build(ids));
		assertEquals(100, ids.size());
	}

	@Test
	public void defaultBuilderCustomAbstract() {
		assertThrows(IllegalArgumentException.class, () -> IdBuilder.defaultBuilder(AbstractObject.class));
	}

	public abstract static class AbstractObject {
		abstract void someMethod();
	}

	@Test
	public void defaultBuilderCustomFailToCreate() {
		assertNotNull(new SometimesFailObject());
		SometimesFailObject.shouldFail = true;
		assertThrows(IllegalArgumentException.class, () -> IdBuilder.defaultBuilder(SometimesFailObject.class));

		SometimesFailObject.shouldFail = false;
		IdBuilder<SometimesFailObject> builder = IdBuilder.defaultBuilder(SometimesFailObject.class);
		SometimesFailObject.shouldFail = true;
		assertThrows(RuntimeException.class, () -> builder.build(new ObjectOpenHashSet<>()));
	}

	public static class SometimesFailObject {
		static boolean shouldFail;

		public SometimesFailObject() {
			if (shouldFail)
				throw new RuntimeException();
		}
	}

	@Test
	public void defaultBuilderCustomNonUnique() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IdBuilder<ArrayList<Integer>> builder = (IdBuilder) IdBuilder.defaultBuilder(ArrayList.class);
		Set<ArrayList<Integer>> ids = new ObjectOpenHashSet<>();
		ids.add(builder.build(ids));
		assertThrows(IllegalArgumentException.class, () -> builder.build(ids));
	}

	@Test
	public void defaultBuilderCustomNonPublicClass() {
		assertNotNull(new NonPublicClass());
		IdBuilder<NonPublicClass> builder = IdBuilder.defaultBuilder(NonPublicClass.class);
		Set<NonPublicClass> ids = new ObjectOpenHashSet<>();
		for (int i = 0; i < 100; i++)
			ids.add(builder.build(ids));
		assertEquals(100, ids.size());
	}

	private static class NonPublicClass {
		public NonPublicClass() {}
	}

	@Test
	public void defaultBuilderCustomNonPublicConstructor() {
		assertNotNull(new NonPublicConstructor());
		IdBuilder<NonPublicConstructor> builder = IdBuilder.defaultBuilder(NonPublicConstructor.class);
		Set<NonPublicConstructor> ids = new ObjectOpenHashSet<>();
		for (int i = 0; i < 100; i++)
			ids.add(builder.build(ids));
		assertEquals(100, ids.size());
	}

	public static class NonPublicConstructor {
		private NonPublicConstructor() {}
	}

}
