/*******************************************************************************
 * Copyright 2015 Junichi Tatemura
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.Type;
import com.nec.congenio.json.JsonXML;
import com.nec.congenio.value.PrimitiveValue;
import com.nec.congenio.value.xml.Attrs;
import com.nec.congenio.value.xml.XMLValue;
import com.nec.congenio.xml.XML;

/**
 * Value expression.
 *<ul>
 *<li> "concat('sep')"
 *<li> "find('pattern')"
 *<li> arithmetic: "*", "+"
 *<li> "jsondump"
 *</ul>
 *(TODO: switch-case, if-then-else?)
 *<p>
 *In the descendants of the value expression,
 *list value expressions can be used:
 *<ul>
 *<li> &lt;split (with)&gt;
 *<li> &lt;slice&gt;/(&lt;from&gt;,&lt;to&gt;,&lt;of&gt;)
 *<li> &lt;map (path)&gt;
 *<li> &lt;findAll (pattern,group)&gt;
 *</ul>
 *(TODO: filter, join?)
 */
public abstract class ExpXML {

	public static final String EXP = "exp";

	public static Element evaluate(Element e) {

		/**
		 * Handle value expression:
		 */
		Element evaled = ExpXML.eval(e);
		if (evaled != null) {
			return evaled;
		}

		/**
		 * shallow clone: copies element and its attributes only:
		 */
		Element result = (Element) e.cloneNode(false);
		List<Element> elements = XML.getElements(e);
		if (elements.isEmpty()) {
			String value = e.getTextContent();
			if (value != null) {
				result.setTextContent(value.trim());
			}
			return result;
		}
		for (Element c : elements) {
			result.appendChild(evaluate(c));
		}
		return result;
	}
	/**
	 * Finds a value expression associated with the
	 * given element.
	 * @param e
	 * @return null if no value expression is
	 * associated with the element.
	 */
	@Nullable
	public static ExpXML findExp(Element e) {
		String expression = XML.getAttribute(ExpXML.EXP, e, null);
		if (expression != null) {
			return create(expression.trim(), e);
		}
        return null;
	}
	protected static ExpXML create(String exp, Element e) {
		Matcher m = Concat.PATTERN.matcher(exp);
		if (m.matches()) {
			return new Concat(m, e);
		}
		Matcher m1 = Find.PATTERN.matcher(exp);
		if (m1.matches()) {
			return new Find(m1, e);
		}
		Matcher m2 = Arith.PATTERN.matcher(exp);
		if (m2.matches()) {
			return new Arith(m2, e);
		}
		Matcher m3 = JsonDump.PATTERN.matcher(exp);
		if (m3.matches()) {
			return new JsonDump(m3, e);
		}
		throw new ConfigException("invalid expression: " + exp);
	}

	/**
	 * Creates a new element that holds the result
	 * of expression evaluation (if it exists).
	 * @param e the element to be evaluated.
	 * @return null if no expression is associated
	 * with the given element.
	 */
	@Nullable
	public static Element eval(Element e) {
		ExpXML ex = findExp(e);
		if (ex != null) {
        	Element result = e.getOwnerDocument()
        			.createElement(e.getTagName());
        	result.setTextContent(ex.getValue());
        	/**
        	 * XML config-value attributes will remain
        	 * after expression evaluation.
        	 */
        	for (String attr : Attrs.VALUE_ATTRS) {
            	String value = XML.getAttribute(attr, e, null);
            	if (value != null) {
            		result.setAttribute(attr, value);
            	}
        	}
			return result;
		}
        return null;
	}

	/**
	 * Converts the element to a primitive value.
	 * If the element is associated with an expression,
	 * evaluate it. If it has a child element, converts
	 * the first child. If it has non empty text,
	 * create a primitive value of the (trimmed) text.
	 * Otherwise convert to PrimitiveValue.NULL.
	 * @param e
	 * @return the converted PrimitiveValue.
	 */
	public static PrimitiveValue getValue(Element e) {
		PrimitiveValue v = getRawValue(e);
		Type t = XMLValue.findType(e);
		if (t != null) {
			return v.cast(t);
		} else {
			return v;
		}
	}

	private static PrimitiveValue getRawValue(Element e) {
		ExpXML exp = findExp(e);
		if (exp != null) {
			return exp.value();
		}
    	Element c = XML.getFirstElementIfExists(e);
        if (c != null) {
        	return getRawValue(c);
        }
        return XMLValue.toPrimitive(e);
	}
	@Nullable
	public static String stringValue(Element e) {
		return getRawValue(e).stringValue();
	}

	/**
	 * @return equivalent to value().stringValue().
	 */
	@Nullable
	public abstract String getValue();

	public abstract PrimitiveValue value();

	public static class Arith extends ExpXML {
		public static final Pattern PATTERN = Pattern.compile(
				"^(\\+|\\*)$");
		private final String op;
		private final Element e;
		Arith(Matcher m, Element e) {
			this.e = e;
			this.op = m.group(1);
		}
		@Override
		public String getValue() {
			return value().stringValue();
		}
		public PrimitiveValue value() {
			if ("+".equals(op)) {
				return sum();
			} else if ("*".equals(op)) {
				return mul();
			} else {
				throw new ArithmeticException(
					"unsupported operator: " + op
					+ " at " + pathOf(e));
			}
		}
		
		PrimitiveValue sum() {
			BigDecimal sum = BigDecimal.ZERO;
			for (Element exp : XML.getElements(e)) {
				BigDecimal v = toNumber(exp);
				sum = sum.add(v);
			}
			return PrimitiveValue.valueOf(sum);
		}
		PrimitiveValue mul() {
			BigDecimal mul = BigDecimal.ONE;
			for (Element exp : XML.getElements(e)) {
				BigDecimal v = toNumber(exp);
				mul = mul.multiply(v);
			}
			return PrimitiveValue.valueOf(mul);
		}
		BigDecimal toNumber(Element exp) {
			return getValue(exp).numberValue();
		}
	}
	public static class Concat extends ExpXML {
		private static final Pattern PATTERN = Pattern.compile(
				"^concat\\('(.*)'\\)$");
		private final Element e;
		private final String with;
		Concat(Matcher matched, Element e) {
			this.e = e;
			this.with = matched.group(1);
		}

		@Override
		public String getValue() {
			StringBuilder sb = new StringBuilder();
			boolean contd = false;
			for (Element c : XML.getElements(e)) {
				if (contd && with != null) {
					sb.append(with);
				}
				append(c, with, sb);
				contd = true;
			}
			return sb.toString();
		}
		@Override
		public PrimitiveValue value() {
			return PrimitiveValue.valueOf(getValue());
		}
		private void append(Element c, String with, StringBuilder sb) {
			ListExpXML lxml = ListExpXML.find(c);
			if (lxml != null) {
				boolean contd = false;
				for (PrimitiveValue s : lxml.getList()) {
					if (contd && with != null) {
						sb.append(with);
					}
					sb.append(s.stringValue());
					contd = true;
				}
			} else {
				String v = stringValue(c);
				if (v != null) {
					sb.append(v);
				}
			}
		}
	}
	public static class Find extends ExpXML {
		public static final String TAG_NAME = "find";
		public static final Pattern PATTERN =
				Pattern.compile("^find\\('(.*)'\\)$");
		public static final String ATTR_PATTERN = "pattern";
		public static final String ATTR_GROUP = "group";
		public static final String ATTR_DEFAULT = "default";
		private final Element e;
		private final Pattern pattern;
		private final int group;
		private final String defaultValue;
		Find(Element e) {
			this.e = e;
			this.pattern = Pattern.compile(
					XML.getAttribute(ATTR_PATTERN, e));
			this.group = XML.getAttributeInt(ATTR_GROUP, e, 0);
			this.defaultValue =
					XML.getAttribute(ATTR_DEFAULT, e, "");
		}
		Find(Matcher m, Element e) {
			this.e = e;
			this.pattern = Pattern.compile(m.group(1));
			this.group = 0;
			this.defaultValue = "";
		}
		@Override
		public String getValue() {
			Matcher m = pattern.matcher(
					getContentValue());
			if (m.find()) {
				return m.group(group);
			} else {
				return defaultValue;
			}
		}
		@Override
		public PrimitiveValue value() {
			return PrimitiveValue.valueOf(getValue());
		}
		private String getContentValue() {
			Element c = XML.getFirstElementIfExists(e);
			if (c != null) {
				String content = stringValue(c);
				return content != null ? content : "";
			} else {
				return e.getTextContent().trim();
			}
		}
	}
	public static class JsonDump extends ExpXML {
		public static final Pattern PATTERN =
				Pattern.compile("^jsondump$");
		private final Element e;
		JsonDump(Matcher m, Element e) {
			this.e = e;
		}
		@Override
		public PrimitiveValue value() {
			return PrimitiveValue.valueOf(getValue());
		}
		@Override
		public String getValue() {
			return getJsonValue().toString();
		}

		JsonValue getJsonValue() {
			/**
			 * NOTE the top level element
			 * must not be processed by JsonXML
			 * directly: this element has
			 * exp="jsondump" attribute. If it
			 * is given to JsonXML it calls JsonDump,
			 * resulting in an infinite loop.
			 */
			Type type = XMLValue.findType(e);
			if (Type.ARRAY.equals(type)) {
				return toJsonArray(e);
			} else {
				return toJsonObject(e);
			}
		}
		JsonValue toJsonArray(Element e) {
			JsonArrayBuilder b = Json.createArrayBuilder();
			for (Element c : XML.getElements(e)) {
				b.add(toJson(c));
			}
			return b.build();
		}
		JsonValue toJsonObject(Element e) {
			JsonObjectBuilder b = Json.createObjectBuilder();
			for (Element c : XML.getElements(e)) {
				String key = XMLValue.getName(c);
				b.add(key, toJson(c));
			}
			return b.build();
		}
		JsonValue toJson(Element c) {
			/**
			 * NOTE now JsonXML.toJson() handles
			 * exp (evaluation of expressions in descendants).
			 * TODO separate evaluation out of JsonXML.
			 */
			return JsonXML.toJson(c);
		}

	}
	public abstract static class ListExpXML {
		//@Nullable
		public static ListExpXML find(Element e) {
			String name = XMLValue.getName(e);
			if (Split.TAG_NAME.equals(name)) {
				return new Split(e);
			} else if (FindAll.TAG_NAME.equals(name)) {
				return new FindAll(e);
			} else if (Slice.TAG_NAME.equals(name)) {
				return new Slice(e);
			} else if (MapPath.TAG_NAME.equals(name)) {
				return new MapPath(e);
			}
			return null;
		}
		public abstract List<PrimitiveValue> getList();
		protected List<PrimitiveValue> contentValues(Element e) {
			List<PrimitiveValue> result = new ArrayList<PrimitiveValue>();
			List<Element> elems = XML.getElements(e);
			if (elems.isEmpty()) {
				result.add(PrimitiveValue.valueOf(
						e.getTextContent().trim()));
			} else {
				for (Element c : elems) {
					ListExpXML lxml = ListExpXML.find(c);
					if (lxml != null) {
						result.addAll(lxml.getList());
					} else {
						PrimitiveValue v =
								getValue(c);
						result.add(v);
					}
				}
			}
			return result;
		}
		/**
		 * Evaluate descendants (ignore the exp
		 * associated with this element itself).
		 */
		protected ConfigValue eval(Element e) {
			Element res = (Element) e.cloneNode(false);
			for (Element c : XML.getElements(e)) {
				res.appendChild(ExpXML.evaluate(c));
			}
			return XMLValue.create(res);
		}
	}

	public static class FindAll extends ListExpXML {
		public static final String TAG_NAME = "findAll";
		public static final String ATTR_PATTERN = "pattern";
		public static final String ATTR_GROUP = "group";
		public static final String ATTR_DEFAULT = "default";
		private final Element e;
		private final Pattern pattern;
		private final int group;
		FindAll(Element e) {
			this.e = e;
			this.pattern = Pattern.compile(
					XML.getAttribute(ATTR_PATTERN, e));
			this.group = XML.getAttributeInt(ATTR_GROUP, e, 0);
		}
		@Override
		public List<PrimitiveValue> getList() {
			List<PrimitiveValue> result = new ArrayList<PrimitiveValue>();
			for (PrimitiveValue content : contentValues(e)) {
				String str = content.stringValue();
				Matcher m = pattern.matcher(str);
				while (m.find()) {
					String s = m.group(group);
					if (s != null) {
						result.add(PrimitiveValue.valueOf(s));
					}
				}
			}
			return result;
		}
	}

	/**
	 * A list function that generates
	 * a sublist of an input list
	 * It has two optional parameters: from (the start
	 * index, inclusive) and to (the end index,
	 * exclusive). If from is omitted, the result
	 * starts at the beginning of the input list.
	 * If to is omitted, the result ends at the
	 * end of the input list.
	 * The input list can be explicitly specified
	 * by "of":
	 * <pre>
	 * &lt;slice&gt;
	 *   &lt;to ref="..."&gt;
	 *   &lt;of&gt;
	 *      ....
	 *   &lt;/of&gt;
	 * &lt;/slice&gt;
	 * </pre>
	 * Or directly written if it is
	 * a list function (e.g. map, findAll).
	 * <pre>
	 * &lt;slice&gt;
	 *   &lt;from ref="..."&gt;
	 *   &lt;to ref="..."&gt;
	 *   &lt;map&gt;
	 *      ....
	 *   &lt;/map&gt;
	 * &lt;/slice&gt;
	 * </pre>
	 * @author tatemura
	 *
	 */
	public static class Slice extends ListExpXML {
		public static final String TAG_NAME = "slice";
		public static final String FROM = "from";
		public static final String TO = "to";
		public static final String OF = "of";
		private final Element e;
		Slice(Element e) {
			this.e = e;
		}

		@Override
		public List<PrimitiveValue> getList() {
			List<PrimitiveValue> input = inputList();
			ConfigValue vs = eval(e);
			int fromIndex = vs.getInt(FROM, 0);
			if (fromIndex > input.size()) {
				throw new IndexOutOfBoundsException(
						"sclice.from["
						+ fromIndex + "] > input size["
						+ input.size() + "]: "
						+ pathOf(e));
			} else if (fromIndex < 0) {
				throw new IndexOutOfBoundsException(
						"sclice.from["
						+ fromIndex + "] < 0: "
						+ pathOf(e));
			}
			int toIndex = vs.getInt(TO, -1);
			if (toIndex >= 0) {
				if (toIndex > input.size()) {
					throw new IndexOutOfBoundsException(
						"sclice.to["
						+ toIndex + "] > input size["
						+ input.size() + "]: "
							+ pathOf(e));
				} else if (fromIndex > toIndex) {
					throw new IndexOutOfBoundsException(
							"sclice.from["
							+ fromIndex + "] > slice.to["
							+ toIndex + "]: "
					+ pathOf(e));
				}
				return input.subList(fromIndex, toIndex);
			} else {
				return input.subList(fromIndex, input.size());
			}
		}
		List<PrimitiveValue> inputList() {
			Element in = XML.getSingleElement(OF, e, false);
			if (in != null) {
				return contentValues(in);
			}
			/**
			 * Otherwise input is implicit
			 */
			List<PrimitiveValue> result = new ArrayList<PrimitiveValue>();
			for (Element c : XML.getElements(e)) {
				ListExpXML lxml = ListExpXML.find(c);
				if (lxml != null) {
					result.addAll(lxml.getList());
				}
			}
			return result;
		}
	}
	public static class MapPath extends ListExpXML {
		public static final String TAG_NAME = "map";
		public static final String ATTR_PATH = "path";
		private final Element e;
		private final String[] path;
		MapPath(Element e) {
			this.e = e;
			String p = XML.getAttribute(ATTR_PATH, e);
			this.path = p.split("/");
		}
		@Override
		public List<PrimitiveValue> getList() {
			List<PrimitiveValue> result = new ArrayList<PrimitiveValue>();
			for (Element c : XML.getElements(e)) {
				PrimitiveValue v = extract(c, 0);
				if (v != null) {
					result.add(v);
				}
			}
			return result;
		}
		PrimitiveValue extract(Element e, int idx) {
			String name = path[idx];
			if (idx + 1 == path.length) {
				Attr attr = e.getAttributeNode(name);
				if (attr != null) {
					return PrimitiveValue.valueOf(attr.getValue());
				}
				Element c = XML.getSingleElement(name, e, false);
				if (c != null) {
					return getValue(c);
				}
			} else {
				Element c = XML.getSingleElement(name, e, false);
				if (c != null) {
					return extract(c, idx + 1);
				}
			}
			return null;
		}
	}
	public static class Split extends ListExpXML {
		public static final String TAG_NAME = "split";
		public static final String ATTR_WITH = "with";
		private final Element e;
		Split(Element e) {
			this.e = e;
		}
		@Override
		public List<PrimitiveValue> getList() {
			String sep = XML.getAttribute(ATTR_WITH, e);
			List<PrimitiveValue> result = new ArrayList<PrimitiveValue>();
			for (PrimitiveValue v : contentValues(e)) {
				String str = v.stringValue();
				if (str != null) {
					for (String s : str.split(sep)) {
						result.add(PrimitiveValue.valueOf(s));
					}
				}
			}
			return result;
		}
	}
	public static String pathOf(Element e) {
		List<String> path = new ArrayList<String>();
		path.add(XMLConfigDescription.nameOf(e));
		Element current = e;
		Node n;
		while ((n = current.getParentNode()) instanceof Element) {
			current = (Element) n;
			path.add("/");
			path.add(XMLConfigDescription.nameOf(current));
		}
		Collections.reverse(path);
		StringBuilder sb = new StringBuilder();
		for (String p : path) {
			sb.append(p);
		}
		return sb.toString();
	}
}