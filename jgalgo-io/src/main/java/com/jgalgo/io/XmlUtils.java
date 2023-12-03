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
package com.jgalgo.io;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.jgalgo.internal.util.Assertions;

class XmlUtils {

	private XmlUtils() {}

	static Iterable<Element> children(Node parent, String tag) {
		return () -> StreamSupport.stream(children(parent).spliterator(), false).filter(e -> e.getTagName().equals(tag))
				.iterator();
	}

	static Iterable<Element> children(Node parent) {
		return () -> new Iterator<>() {

			final NodeList childNodes = parent.getChildNodes();
			final int length = childNodes.getLength();
			int idx;
			{
				advance();
			}

			private void advance() {
				for (; idx < length; idx++)
					if (childNodes.item(idx) instanceof Element)
						return;
			}

			@Override
			public boolean hasNext() {
				return idx < length;
			}

			@Override
			public Element next() {
				Assertions.Iters.hasNext(this);
				Element element = (Element) childNodes.item(idx++);
				advance();
				return element;
			}
		};
	}

	static Element requiredChild(Element parent, String tag) {
		Iterator<Element> children = children(parent, tag).iterator();
		if (!children.hasNext())
			throw new IllegalArgumentException("no " + tag + " element");
		Element child = children.next();
		if (children.hasNext())
			throw new IllegalArgumentException("multiple " + tag + " elements");
		return child;
	}

	static Optional<Element> optionalChild(Element parent, String tag) {
		Iterator<Element> children = children(parent, tag).iterator();
		if (!children.hasNext())
			return Optional.empty();
		Element child = children.next();
		if (children.hasNext())
			throw new IllegalArgumentException("multiple " + tag + " elements");
		return Optional.of(child);
	}

	static String requiredAttribute(Element element, String att) {
		return optionalAttribute(element, att)
				.orElseThrow(() -> new IllegalArgumentException("no " + att + " attribute"));
	}

	static Optional<String> optionalAttribute(Element element, String att) {
		String value = element.getAttribute(att);
		return value.isEmpty() && !element.hasAttribute(att) ? Optional.empty() : Optional.of(value);
	}

}
