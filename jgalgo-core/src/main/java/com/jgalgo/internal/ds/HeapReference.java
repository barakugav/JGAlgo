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

/**
 * A reference to an element in a {@link HeapReferenceable}.
 * <p>
 * When a new element is inserted to a referenceable heap using {@link HeapReferenceable#insert(Object, Object)} a
 * reference to the element is returned. The reference object will be valid as long as element is still in the heap. The
 * reference may be used to access the heap element's key and value directly using the {@link #key()} and
 * {@link #value()}, or for various operations such as {@link HeapReferenceable#decreaseKey(HeapReference, Object)} or
 * {@link HeapReferenceable#remove(HeapReference)} which could be implemented efficiently given the reference, as the
 * implementation doesn't need to search for the element in the heap.
 *
 * @param  <K> the key type
 * @param  <V> the value type
 * @see        HeapReferenceable
 * @author     Barak Ugav
 */
public interface HeapReference<K, V> {

	/**
	 * Get the key of this element.
	 * <p>
	 * There is no {@code setKey} method, but the key of an element can be changed (decrease only) by
	 * {@link HeapReferenceable#decreaseKey}.
	 *
	 * @return the element's key
	 */
	K key();

	/**
	 * Get the value of this element.
	 *
	 * @return the element's key
	 */
	V value();

	/**
	 * Set the value of this element.
	 *
	 * @param val new value for this element
	 */
	void setValue(V val);
}
