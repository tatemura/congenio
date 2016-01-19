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
package com.nec.congenio.value.xml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.json.JsonValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nec.congenio.ConfigValue;
import com.nec.congenio.Type;
import com.nec.congenio.json.JsonXML;
import com.nec.congenio.value.AbstractValue;
import com.nec.congenio.value.PrimitiveValue;
import com.nec.congenio.xml.XML;


public class XMLValue extends AbstractValue implements ConfigValue {
	private final Element root;
	protected XMLValue(Element e) {
		this.root = e;
	}
	public Element toXML() {
		return root;
	}

	@Override
	public String getName() {
		return getName(root);
	}

	@Override
	public ConfigValue findValue(String name) {
		String value = XML.getAttribute(name, root, null);
		if (value != null) {
			return PrimitiveValue.valueOf(value);
		}
		Element e = XML.getSingleElement(name, root, false);
		if (e != null) {
			return new XMLValue(e);
		} else {
			return null;
		}
	}
	@Override
	public boolean hasValue(String name) {
		Element e = XML.getSingleElement(name, root, false);
		String a = XML.getAttribute(name, root, null);
		return e != null || a != null;
	}


	@Override
	public JsonValue toJson() {
		return JsonXML.toJson(root);
	}
	@Override
	public Element toXML(Document doc, String name) {
		if (root.getTagName().equals(name)) {
    		return (Element) doc.importNode(root, true);
		}
		Element e1 = doc.createElement(name);
		NodeList nlist = root.getChildNodes();
		for (int i = 0; i < nlist.getLength(); i++) {
			Node n = nlist.item(i);
			e1.appendChild(doc.importNode(n, true));
		}
		return e1;
	}

	@Override
	public Map<String, ConfigValue> toValueMap() {
		Map<String, ConfigValue> map = new HashMap<String, ConfigValue>();
		for (Map.Entry<String, String> e : Attrs.userAttrs(root).entrySet()) {
			map.put(e.getKey(), PrimitiveValue.valueOf(e.getValue()));
		}
		for (Element e : XML.getElements(root)) {
			map.put(XMLValue.getName(e),
					new XMLValue(e));
		}
		return map;
	}

	@Override
	public List<ConfigValue> toValueList() {
		List<ConfigValue> list = new ArrayList<ConfigValue>();
		for (String value
				: Attrs.userAttrs(root).values()) {
			list.add(PrimitiveValue.valueOf(value));
		}
		for (Element e : XML.getElements(root)) {
			list.add(new XMLValue(e));
		}
		return list;
	}

	@Override
	public String stringValue() {
		return toPrimitive().stringValue();
	}
	@Override
	public BigDecimal numberValue() {
		return toPrimitive().numberValue();
	}
	@Override
	public boolean booleanValue() {
		return toPrimitive().booleanValue();
	}

	PrimitiveValue toPrimitive() {
		return toPrimitive(root);
	}

	public static boolean hasValue(Element e) {
	    String avalue = XML.getAttribute(Attrs.VALUE, e, null);
	    if (avalue != null) {
	    	return true;
	    } else {
	    	String value = e.getTextContent().trim();
	    	return !value.isEmpty();
	    }
	}
	public static PrimitiveValue toPrimitive(Element e) {
		PrimitiveValue v;
	    String avalue = XML.getAttribute(Attrs.VALUE, e, null);
	    if (avalue != null) {
	    	v = PrimitiveValue.valueOf(avalue);
	    } else {
	    	String value = e.getTextContent().trim();
	    	if (value.isEmpty()) {
	    		return PrimitiveValue.NULL;
	    	}
        	v = PrimitiveValue.valueOf(value);
	    }
	    Type type = findType(e);
	    if (type != null) {
	    	return v.cast(type);
	    } else {
		    return v;
	    }
	}

    public String toString() {
    	return XML.toString(root);
    }
    @Override
    public Type getType() {
    	Type type = findType(root);
    	if (type != null) {
    		return type;
    	}
    	return guessType(root);
    }

	@Nullable
	public static Type findType(Element e) {
		String value = XML.getAttribute(Attrs.TYPE, e, null);
		if (value != null) {
			try {
				return Type.valueOf(value.trim().toUpperCase());
			} catch (IllegalArgumentException ex) {
				/**
				 * Note: type does not match with the Type.
				 * ("type" may be used by other purpose)
				 * TODO use namespace to distinguish "type" attribute?
				 *
				 */
				return null;
			}
		}
		return null;
	}

	public static Type guessType(Element e) {
		List<Element> elements = XML.getElements(e);
		Set<String> names = new HashSet<String>();
		for (Element c : elements) {
			names.add(XMLValue.getName(c));
		}
		if (names.size() == 1 && elements.size() > 1) {
			return Type.ARRAY;
		} else if (elements.size() > 0){
			return Type.OBJECT;
		}
		Map<String, String> attrs = Attrs.userAttrs(e);
		if (!attrs.isEmpty()) {
			return Type.OBJECT;
		}
		return Type.STRING;
	}

	public static final String TAG_PROPERTY = "property";

	private static final Pattern TAG_PATTERN = Pattern.compile("^\\w+$");

	public static XMLValue create(Element e) {
		return new XMLValue(e);
	}
	public static String getName(Element e) {
		return XML.getAttribute(Attrs.NAME, e, e.getTagName());
	}
	/**
	 * NOTE we use two ways to encode a name-value pair in XML:
	 * When the name consists only of word characters, we use a tag
	 * name for the name. Otherwise, use a name attribute (like a
	 * property element).
	 */
	public static Element createElement(Document doc, String name) {
		if (TAG_PATTERN.matcher(name).matches()) {
			return doc.createElement(name);
		}
		Element e = doc.createElement(TAG_PROPERTY);
		e.setAttribute(Attrs.NAME, name);
		return e;
	}
	public static Element createElement(Document doc, String name, Type type) {
		Element e = createElement(doc, name);
		e.setAttribute(Attrs.TYPE, type.attr());
		return e;
	}
	public static Element createElement(Document doc, String name, Type type, Object value) {
		Element e = createElement(doc, name, type);
		if (value != null) {
			e.setTextContent(value.toString());
		}
		return e;
	}
	public static Element createElement(Document doc, String name, String value) {
		Element e = createElement(doc, name);
		if (value != null) {
			e.setTextContent(value);
		}
		return e;
	}
}
