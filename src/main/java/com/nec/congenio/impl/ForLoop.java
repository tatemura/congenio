/*******************************************************************************
 * Copyright 2015, 2016 Junichi Tatemura
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nec.congenio.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nec.congenio.ConfigException;
import com.nec.congenio.xml.XML;


/**
 * <pre>
 * &lt;a&gt;
 *   &lt;foreach name="b" sep=" "&gt;1 2 3&lt;/foreach&gt;
 *   ...
 * &lt;/a&gt;
 * </pre>
 * the foreach expression is expanded to a sequence of three XML documents:
 * <pre>
 * &lt;a&gt;
 *   &lt;b&gt;1&lt;/b&gt;
 *   ...
 * &lt;/a&gt;
 * &lt;a&gt;
 *   &lt;b&gt;2&lt;/b&gt;
 *   ...
 * &lt;/a&gt;
 * &lt;a&gt;
 *   &lt;b&gt;3&lt;/b&gt;
 *   ...
 * &lt;/a&gt;
 * </pre>
 * When the document contains multiple foreach expression,
 * Cartesian product of them are created.
 * <pre>
 * &lt;a&gt;
 *   &lt;foreach name="b"&gt;...&lt;/foreach&gt;
 *   &lt;foreach name="c"&gt;...&lt;/foreach&gt;
 *   ...
 * &lt;/a&gt;
 * </pre>
 * It generates a sequence of XML documents
 * in the following form:
 * <pre>
 * &lt;a&gt;
 *   &lt;b&gt;...&lt;/b&gt;
 *   &lt;c&gt;...&lt;/c&gt;
 *   ...
 * &lt;/a>
 * </pre>
 * @author tatemura
 *
 */
public class ForLoop implements Iterable<Element> {
	public static final String ELEMENT_FOREACH = "foreach";
	private final Element orig;
	private final List<ForLoop.Each> eachs = new ArrayList<ForLoop.Each>();

	public static Iterable<Element> unfold(Element e) {
		if (isForLoop(e)) {
			return new ForLoop(e);
		} else {
			return Arrays.asList(e);
		}
	}
	public static boolean isForLoop(Element e) {
		return !XML.getElements(ELEMENT_FOREACH, e).isEmpty();
	}
	protected static boolean isForeach(Element e) {
		return e.getTagName().equals(ELEMENT_FOREACH);
	}

	public ForLoop(Element e) {
		this.orig = e;
		for (Element e1 : XML.getElements(e)) {
			if (isForeach(e1)) {
				eachs.add(Each.create(e1));
			}
		}
	}

	public List<ForLoop.Binding> bindings() {
		List<Binding> bins = new ArrayList<Binding>();
		enumBindings(0, new ArrayList<Element>(), bins);
		return bins;
	}

	protected void enumBindings(int idx,
			List<Element> eachList,
			List<Binding> res) {
		if (idx < eachs.size()) {
			for (Element e : eachs.get(idx).elements()) {
				eachList.add(e);
				enumBindings(idx + 1, eachList, res);
				eachList.remove(eachList.size() - 1);
			}
		} else {
			res.add(new Binding(eachList));
		}
	}
	@Override
	public Iterator<Element> iterator() {
		return new ForLoopIterator(orig,
				bindings().iterator());
	}
	static class ForLoopIterator implements Iterator<Element> {
		private final Iterator<Binding> bins;
		private final Element orig;
		ForLoopIterator(Element orig, Iterator<Binding> bins) {
			this.orig = orig;
			this.bins = bins;
		}
		@Override
		public boolean hasNext() {
			return bins.hasNext();
		}

		@Override
		public Element next() {
			Binding bin = bins.next();
			Element s =
					(Element) orig.cloneNode(false);
			int idx = 0;
			for (Element c : XML.getElements(orig)) {
				if (isForeach(c)) {
					s.appendChild(bin.get(idx)
							.cloneNode(true));
					idx++;
				} else {
					s.appendChild(c.cloneNode(true));
				}
			}
			return s;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	static class Binding {
		private Element[] values;
		Binding(List<Element> list) {
			values = list.toArray(new Element[list.size()]);
		}
		public Element get(int idx) {
			return values[idx];
		}
		public int size() {
			return values.length;
		}
	}
	abstract static class Each {
		private final Element e;
		private final String name;
		protected Each(Element e) {
			this.e = e;
			this.name = XML.getAttribute("name", e);
		}
		public static Each create(Element e) {
			String sep = XML.getAttribute("sep", e, null);
			if (sep != null) {
				return new EachTextSplit(sep, e);
			}
			String range = XML.getAttribute("range", e, null);
			if (range != null) {
				return new EachIntRange(range, e);
			}
			return new EachElement(e);
		}
		public static List<Element> elements(Element e) {
			return create(e).elements();
		}
		protected Element newElement() {
			return e.getOwnerDocument()
					.createElement(name);
		}
		public abstract List<Element> elements();
	}

	static class EachIntRange extends Each {
		private static final Pattern PATTERN =
				Pattern.compile("^(\\d+)\\.\\.(\\d+)$");
		private int from;
		private int to;
		public EachIntRange(String range, Element e) {
			super(e);
			Matcher m = PATTERN.matcher(range);
			if (m.matches()) {
				from = Integer.parseInt(m.group(1));
				to = Integer.parseInt(m.group(2));
				if (from > to) {
					throw new ConfigException(
						"invalid range=" + range);
				}
			} else {
				throw new ConfigException(
						"invalid range=" + range);
			}
		}
		@Override
		public List<Element> elements() {
			List<Element> result = new ArrayList<Element>();
			for (int i = from; i <= to; i++) {
				Element each = newElement();
				each.setTextContent(
						Integer.toString(i));
				result.add(each);
			}
			return result;
		}

	}
	static class EachTextSplit extends Each {
		private final String sep;
		private final Element e;
		public EachTextSplit(String sep, Element e) {
			super(e);
			this.sep = sep;
			this.e = e;
		}
		private String textValue() {
			String value = XML.getAttribute("value", e, null);
			if (value != null) {
				return value;
			}
			return e.getTextContent()
					.trim();
		}
		@Override
		public List<Element> elements() {
			List<Element> result = new ArrayList<Element>();
			String[] vals = textValue().split(sep);
			for (String v : vals) {
				Element each = newElement();
				each.setTextContent(v);
				result.add(each);
			}
			return result;
		}
	}
	static class EachElement extends Each {
		private Element e;
		EachElement(Element e) {
			super(e);
			this.e = e;
		}
		@Override
		public List<Element> elements() {
			List<Element> result = new ArrayList<Element>();
			for (Element c : XML.getElements(e)) {
				Element each = newElement();
				NodeList nlist = c.getChildNodes();
				for (int i = 0; i < nlist.getLength(); i++) {
					Node n = nlist.item(i);
					each.appendChild(n.cloneNode(true));
				}
				result.add(each);
			}
			return result;
		}

	}
}