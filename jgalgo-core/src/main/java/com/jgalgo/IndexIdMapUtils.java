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

import java.util.Objects;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

class IndexIdMapUtils {

	static class IteratorFromIndexIterator implements IntIterator {

		private final IntIterator it;
		private final IndexIdMap map;

		IteratorFromIndexIterator(IntIterator it, IndexIdMap map) {
			this.it = Objects.requireNonNull(it);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			return map.indexToId(it.nextInt());
		}

		@Override
		public void remove() {
			it.remove();
		}

	}

	static class IndexIteratorFromIterator implements IntIterator {

		private final IntIterator it;
		private final IndexIdMap map;

		IndexIteratorFromIterator(IntIterator it, IndexIdMap map) {
			this.it = Objects.requireNonNull(it);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			return map.idToIndex(it.nextInt());
		}

		@Override
		public void remove() {
			it.remove();
		}

	}

	static class CollectionFromIndexCollection extends AbstractIntCollection {

		private final IntCollection c;
		private final IndexIdMap map;

		CollectionFromIndexCollection(IntCollection c, IndexIdMap map) {
			this.c = Objects.requireNonNull(c);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public int size() {
			return c.size();
		}

		@Override
		public boolean isEmpty() {
			return c.isEmpty();
		}

		@Override
		public void clear() {
			c.clear();
		}

		@Override
		public IntIterator iterator() {
			return new IteratorFromIndexIterator(c.iterator(), map);
		}

		@Override
		public boolean contains(int key) {
			return c.contains(map.idToIndex(key));
		}

		@Override
		public boolean rem(int key) {
			return c.rem(map.idToIndex(key));
		}
	}

	static class IndexCollectionFromCollection extends AbstractIntCollection {

		private final IntCollection c;
		private final IndexIdMap map;

		IndexCollectionFromCollection(IntCollection c, IndexIdMap map) {
			this.c = Objects.requireNonNull(c);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public int size() {
			return c.size();
		}

		@Override
		public boolean isEmpty() {
			return c.isEmpty();
		}

		@Override
		public void clear() {
			c.clear();
		}

		@Override
		public IntIterator iterator() {
			return new IndexIteratorFromIterator(c.iterator(), map);
		}

		@Override
		public boolean contains(int key) {
			return c.contains(map.indexToId(key));
		}

		@Override
		public boolean rem(int key) {
			return c.rem(map.indexToId(key));
		}
	}

	static class ListFromIndexList extends AbstractIntList {

		private final IntList list;
		private final IndexIdMap map;

		ListFromIndexList(IntList list, IndexIdMap map) {
			this.list = Objects.requireNonNull(list);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public boolean isEmpty() {
			return list.isEmpty();
		}

		@Override
		public void clear() {
			list.clear();;
		}

		@Override
		public boolean contains(int key) {
			return list.contains(map.idToIndex(key));
		}

		@Override
		public boolean rem(int key) {
			return list.rem(map.idToIndex(key));
		}

		@Override
		public int getInt(int index) {
			return map.indexToId(list.getInt(index));
		}

		@Override
		public int indexOf(int k) {
			return list.indexOf(map.idToIndex(k));
		}

		@Override
		public int lastIndexOf(int k) {
			return list.lastIndexOf(map.idToIndex(k));
		}

		@Override
		public int removeInt(int index) {
			return map.indexToId(list.removeInt(index));
		}

	}

}
