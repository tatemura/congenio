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
import com.nec.congenio.json.JsonXml;
import com.nec.congenio.value.AbstractValue;
import com.nec.congenio.value.PrimitiveValue;
import com.nec.congenio.xml.Xml;

public class XmlValue extends AbstractValue implements ConfigValue {
    private final Element root;

    protected XmlValue(Element elem) {
        this.root = elem;
    }

    @Override
    public String getName() {
        return nameOf(root);
    }

    @Override
    public ConfigValue findValue(String name) {
        String value = Xml.getAttribute(name, root, null);
        if (value != null) {
            return PrimitiveValue.valueOf(value);
        }
        Element elem = Xml.getSingleElement(name, root, false);
        if (elem != null) {
            return new XmlValue(elem);
        } else {
            return null;
        }
    }

    @Override
    public boolean hasValue(String name) {
        Element elem = Xml.getSingleElement(name, root, false);
        String attr = Xml.getAttribute(name, root, null);
        return elem != null || attr != null;
    }

    @Override
    public JsonValue toJson() {
        return JsonXml.toJson(root);
    }

    @Override
    public Element toXml(Document doc, String name) {
        if (root.getTagName().equals(name)) {
            return (Element) doc.importNode(root, true);
        }
        Element e1 = doc.createElement(name);
        NodeList nlist = root.getChildNodes();
        for (int i = 0; i < nlist.getLength(); i++) {
            Node node = nlist.item(i);
            e1.appendChild(doc.importNode(node, true));
        }
        return e1;
    }

    public Element toXml() {
        return root;
    }

    @Override
    public Map<String, ConfigValue> toValueMap() {
        Map<String, ConfigValue> map = new HashMap<String, ConfigValue>();
        for (Map.Entry<String, String> e : Attrs.userAttrs(root).entrySet()) {
            map.put(e.getKey(), PrimitiveValue.valueOf(e.getValue()));
        }
        for (Element e : Xml.getElements(root)) {
            map.put(XmlValue.nameOf(e), new XmlValue(e));
        }
        return map;
    }

    @Override
    public List<ConfigValue> toValueList() {
        List<ConfigValue> list = new ArrayList<ConfigValue>();
        for (String value : Attrs.userAttrs(root).values()) {
            list.add(PrimitiveValue.valueOf(value));
        }
        for (Element e : Xml.getElements(root)) {
            list.add(new XmlValue(e));
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
        return primitiveValueOf(root);
    }

    /**
     * Checks if the given element has a value.
     * @param elem the element to be checked.
     * @return true if it has a value.
     */
    public static boolean valueExists(Element elem) {
        String avalue = Xml.getAttribute(Attrs.VALUE, elem, null);
        if (avalue != null) {
            return true;
        } else {
            String value = elem.getTextContent().trim();
            return !value.isEmpty();
        }
    }

    /**
     * Converts an element to a primitive value.
     * @param elem an element that holds a primitive value.
     * @return a converted primitive value
     */
    public static PrimitiveValue primitiveValueOf(Element elem) {
        PrimitiveValue val;
        String avalue = Xml.getAttribute(Attrs.VALUE, elem, null);
        if (avalue != null) {
            val = PrimitiveValue.valueOf(avalue);
        } else {
            String value = elem.getTextContent().trim();
            if (value.isEmpty()) {
                return PrimitiveValue.NULL;
            }
            val = PrimitiveValue.valueOf(value);
        }
        Type type = findType(elem);
        if (type != null) {
            return val.cast(type);
        } else {
            return val;
        }
    }

    public String toString() {
        return Xml.toString(root);
    }

    @Override
    public Type getType() {
        Type type = findType(root);
        if (type != null) {
            return type;
        }
        return guessType(root);
    }

    public static boolean isPrimitiveType(String typeName) {
        Type type = typeOf(typeName);
        return (type != Type.ARRAY && type != Type.OBJECT);
    }

    public static boolean isPrimitiveType(Type type) {
        return (type != Type.ARRAY && type != Type.OBJECT);
    }

    /**
     * Gets a type by its name.
     * @param typeName the name of the type
     * @return null if there is no such type.
     */
    @Nullable
    public static Type typeOf(String typeName) {
        try {
            return Type.valueOf(typeName.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            /**
             * Note: type does not match with the Type. ("type" may be used by
             * other purpose) TODO use namespace to distinguish "type"
             * attribute?
             *
             */
            return null;
        }
    }

    /**
     * Gets a type that is associated with the element.
     * @param elem the element from which a type is found.
     * @return null if the element is not associated
     *         with a type.
     */
    @Nullable
    public static Type findType(Element elem) {
        String value = Xml.getAttribute(Attrs.TYPE, elem, null);
        if (value != null) {
            return typeOf(value);
        }
        return null;
    }

    /**
     * Guesses a type of the element from its content.
     * @param elem the element for which a type is guessed.
     * @return a guessed type.
     */
    public static Type guessType(Element elem) {
        List<Element> elements = Xml.getElements(elem);
        Set<String> names = new HashSet<String>();
        for (Element c : elements) {
            names.add(XmlValue.nameOf(c));
        }
        if (names.size() == 1 && elements.size() > 1) {
            return Type.ARRAY;
        } else if (elements.size() > 0) {
            return Type.OBJECT;
        }
        Map<String, String> attrs = Attrs.userAttrs(elem);
        if (!attrs.isEmpty()) {
            return Type.OBJECT;
        }
        return Type.STRING;
    }

    public static final String TAG_PROPERTY = "property";

    private static final Pattern TAG_PATTERN = Pattern.compile("^\\w+$");

    public static XmlValue create(Element elem) {
        return new XmlValue(elem);
    }

    public static String nameOf(Element elem) {
        return Xml.getAttribute(Attrs.NAME, elem, elem.getTagName());
    }

    /**
     * NOTE we use two ways to encode a name-value pair in XML: When the name
     * consists only of word characters, we use a tag name for the name.
     * Otherwise, use a name attribute (like a property element).
     */
    public static Element createElement(Document doc, String name) {
        if (TAG_PATTERN.matcher(name).matches()) {
            return doc.createElement(name);
        }
        Element elem = doc.createElement(TAG_PROPERTY);
        elem.setAttribute(Attrs.NAME, name);
        return elem;
    }

    /**
     * Creates an element of a specific type.
     * @param doc the document to which the element belongs.
     * @param name the name of the element
     * @param type the type of the element
     * @return an empty element associated with the given type.
     */
    public static Element createElement(Document doc, String name, Type type) {
        Element elem = createElement(doc, name);
        elem.setAttribute(Attrs.TYPE, type.attr());
        return elem;
    }

    /**
     * Creates an element of a specific type.
     * @param doc the document to which the element belongs.
     * @param name the name of the element.
     * @param type the type of the element.
     * @param value the value of the element.
     * @return a created element.
     */
    public static Element createElement(Document doc, String name, Type type, Object value) {
        Element elem = createElement(doc, name, type);
        if (value != null) {
            elem.setTextContent(value.toString());
        }
        return elem;
    }

    /**
     * Creates an element from a name-value pair.
     * @param doc the document to which the element belongs.
     * @param name the name of the element.
     * @param value the value of the element.
     * @return a created element.
     */
    public static Element createElement(Document doc, String name, String value) {
        Element elem = createElement(doc, name);
        if (value != null) {
            elem.setTextContent(value);
        }
        return elem;
    }
}
