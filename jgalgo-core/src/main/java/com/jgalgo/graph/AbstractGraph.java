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

/**
 * Abstract implementation of {@link Graph}.
 *
 * <p>
 * This class provides default implementations for {@link #equals(Object)}, {@link #hashCode()} and {@link #toString()}.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public abstract class AbstractGraph<V, E> implements Graph<V, E> {

	@Override
	public boolean equals(Object other) {
		return other instanceof Graph && Graphs.isEquals(this, (Graph<?, ?>) other);
	}

	@Override
	public int hashCode() {
		return Graphs.hashCode(this);
	}

	@Override
	public String toString() {
		return Graphs.toString(this);
	}

}
