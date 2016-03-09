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
import com.nec.congenio.json.JsonXml;
import com.nec.congenio.value.PrimitiveValue;
import com.nec.congenio.value.xml.Attrs;
import com.nec.congenio.value.xml.XmlValue;
import com.nec.congenio.xml.Xml;

/**
 * Value expression.
 * <ul>
 * <li>"concat('sep')"
 * <li>"find('pattern')"
 * <li>arithmetic: "*", "+"
 * <li>"jsondump"
 * </ul>
 * (TODO: switch-case, if-then-else?)
 *
 * <p>In the descendants of the value expression, list value expressions can be
 * used:
 * <ul>
 * <li>&lt;split (with)&gt;
 * <li>&lt;slice&gt;/(&lt;from&gt;,&lt;to&gt;,&lt;of&gt;)
 * <li>&lt;map (path)&gt;
 * <li>&lt;findAll (pattern,group)&gt;
 * </ul>
 * (TODO: filter, join?)
 */
public abstract class ExpXml {

    public static final String EXP = "exp";

    /**
     * Evaluates expressions in the element.
     * @param elem the element to be evaluated.
     * @return a converted element.
     */
    public static Element evaluate(Element elem) {

        /**
         * Handle value expression.
         */
        Element evaled = ExpXml.eval(elem);
        if (evaled != null) {
            return evaled;
        }

        /**
         * shallow clone: copies element and its attributes only.
         */
        Element result = (Element) elem.cloneNode(false);
        List<Element> elements = Xml.getElements(elem);
        if (elements.isEmpty()) {
            String value = elem.getTextContent();
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
     * Finds a value expression associated with the given element.
     * 
     * @param elem the element from which an expression
     *        is found.
     * @return null if no value expression is associated with the element.
     */
    @Nullable
    public static ExpXml findExp(Element elem) {
        String expression = Xml.getAttribute(ExpXml.EXP, elem, null);
        if (expression != null) {
            return create(expression.trim(), elem);
        }
        return null;
    }

    protected static ExpXml create(String exp, Element elem) {
        Matcher match = Concat.PATTERN.matcher(exp);
        if (match.matches()) {
            return new Concat(match, elem);
        }
        Matcher m1 = Find.PATTERN.matcher(exp);
        if (m1.matches()) {
            return new Find(m1, elem);
        }
        Matcher m2 = Arith.PATTERN.matcher(exp);
        if (m2.matches()) {
            return new Arith(m2, elem);
        }
        Matcher m3 = JsonDump.PATTERN.matcher(exp);
        if (m3.matches()) {
            return new JsonDump(m3, elem);
        }
        throw new ConfigException("invalid expression: " + exp);
    }

    /**
     * Creates a new element that holds the result of expression evaluation (if
     * it exists).
     * 
     * @param elem
     *            the element to be evaluated.
     * @return null if no expression is associated with the given element.
     */
    @Nullable
    public static Element eval(Element elem) {
        ExpXml ex = findExp(elem);
        if (ex != null) {
            Element result = elem.getOwnerDocument()
                    .createElement(elem.getTagName());
            result.setTextContent(ex.stringValue());
            /**
             * XML config-value attributes will remain after expression
             * evaluation.
             */
            for (String attr : Attrs.VALUE_ATTRS) {
                String value =
                        Xml.getAttribute(attr, elem, null);
                if (value != null) {
                    if (Attrs.TYPE.equals(attr)) {
                        if (XmlValue.isPrimitiveType(value)) {
                            result.setAttribute(attr, value);
                        }
                    } else {
                        result.setAttribute(attr, value);
                    }
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Converts the element to a primitive value. If the element is associated
     * with an expression, evaluate it. If it has a child element, converts the
     * first child. If it has non empty text, create a primitive value of the
     * (trimmed) text. Otherwise convert to PrimitiveValue.NULL.
     * 
     * @param elem the element to be converted.
     * @return the converted PrimitiveValue.
     */
    public static PrimitiveValue getValue(Element elem) {
        PrimitiveValue val = getRawValue(elem);
        Type type = XmlValue.findType(elem);
        if (type != null) {
            return val.cast(type);
        } else {
            return val;
        }
    }

    private static PrimitiveValue getRawValue(Element elem) {
        ExpXml exp = findExp(elem);
        if (exp != null) {
            return exp.value();
        }
        Element child = Xml.getFirstElementIfExists(elem);
        if (child != null) {
            return getRawValue(child);
        }
        return XmlValue.primitiveValueOf(elem);
    }

    @Nullable
    public static String stringValue(Element elem) {
        return getRawValue(elem).stringValue();
    }

    /**
     * @return equivalent to value().stringValue().
     */
    @Nullable
    public abstract String stringValue();

    public abstract PrimitiveValue value();


    public static class Arith extends ExpXml {
        public static final Pattern PATTERN = Pattern.compile("^(\\+|\\*)$");
        private final String op;
        private final Element root;

        Arith(Matcher matched, Element elem) {
            this.root = elem;
            this.op = matched.group(1);
        }

        @Override
        public String stringValue() {
            return value().stringValue();
        }

        @Override
        public PrimitiveValue value() {
            if ("+".equals(op)) {
                return sum();
            } else if ("*".equals(op)) {
                return mul();
            } else {
                throw new ArithmeticException(
                        "unsupported operator: " + op + " at " + pathOf(root));
            }
        }

        PrimitiveValue sum() {
            BigDecimal sum = BigDecimal.ZERO;
            for (Element exp : Xml.getElements(root)) {
                BigDecimal val = toNumber(exp);
                sum = sum.add(val);
            }
            return PrimitiveValue.valueOf(sum);
        }

        PrimitiveValue mul() {
            BigDecimal mul = BigDecimal.ONE;
            for (Element exp : Xml.getElements(root)) {
                BigDecimal val = toNumber(exp);
                mul = mul.multiply(val);
            }
            return PrimitiveValue.valueOf(mul);
        }

        BigDecimal toNumber(Element exp) {
            return getValue(exp).numberValue();
        }
    }

    public static class Concat extends ExpXml {
        private static final Pattern PATTERN =
                Pattern.compile("^concat\\('(.*)'\\)$");
        private final Element root;
        private final String with;

        Concat(Matcher matched, Element elem) {
            this.root = elem;
            this.with = matched.group(1);
        }

        @Override
        public String stringValue() {
            StringBuilder sb = new StringBuilder();
            boolean contd = false;
            for (Element c : Xml.getElements(root)) {
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
            return PrimitiveValue.valueOf(stringValue());
        }

        private void append(Element elem, String with, StringBuilder sb) {
            ListExpXml lxml = ListExpXml.find(elem);
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
                String val = stringValue(elem);
                if (val != null) {
                    sb.append(val);
                }
            }
        }
    }

    public static class Find extends ExpXml {
        public static final String TAG_NAME = "find";
        public static final Pattern PATTERN =
                Pattern.compile("^find\\('(.*)'\\)$");
        public static final String ATTR_PATTERN = "pattern";
        public static final String ATTR_GROUP = "group";
        public static final String ATTR_DEFAULT = "default";
        private final Element root;
        private final Pattern pattern;
        private final int group;
        private final String defaultValue;

        Find(Element elem) {
            this.root = elem;
            this.pattern = Pattern.compile(
                    Xml.getAttribute(ATTR_PATTERN, elem));
            this.group = Xml.getAttributeInt(
                    ATTR_GROUP, elem, 0);
            this.defaultValue = Xml.getAttribute(
                    ATTR_DEFAULT, elem, "");
        }

        Find(Matcher match, Element elem) {
            this.root = elem;
            this.pattern = Pattern.compile(match.group(1));
            this.group = 0;
            this.defaultValue = "";
        }

        @Override
        public String stringValue() {
            Matcher match =
                    pattern.matcher(getContentValue());
            if (match.find()) {
                return match.group(group);
            } else {
                return defaultValue;
            }
        }

        @Override
        public PrimitiveValue value() {
            return PrimitiveValue.valueOf(stringValue());
        }

        private String getContentValue() {
            Element child =
                    Xml.getFirstElementIfExists(root);
            if (child != null) {
                String content = stringValue(child);
                return content != null ? content : "";
            } else {
                return root.getTextContent().trim();
            }
        }
    }

    public static class JsonDump extends ExpXml {
        public static final Pattern PATTERN = Pattern.compile("^jsondump$");
        private final Element root;

        JsonDump(Matcher match, Element elem) {
            this.root = elem;
        }

        @Override
        public PrimitiveValue value() {
            return PrimitiveValue.valueOf(stringValue());
        }

        @Override
        public String stringValue() {
            return getJsonValue().toString();
        }

        JsonValue getJsonValue() {
            /**
             * NOTE the top level element must not be processed by JsonXML
             * directly: this element has exp="jsondump" attribute. If it is
             * given to JsonXML it calls JsonDump, resulting in an infinite
             * loop.
             */
            Type type = XmlValue.findType(root);
            if (Type.ARRAY.equals(type)) {
                return toJsonArray(root);
            } else {
                return toJsonObject(root);
            }
        }

        JsonValue toJsonArray(Element elem) {
            JsonArrayBuilder builder =
                    Json.createArrayBuilder();
            for (Element c : Xml.getElements(elem)) {
                builder.add(toJson(c));
            }
            return builder.build();
        }

        JsonValue toJsonObject(Element elem) {
            JsonObjectBuilder builder =
                    Json.createObjectBuilder();
            for (Element c : Xml.getElements(elem)) {
                String key = XmlValue.nameOf(c);
                builder.add(key, toJson(c));
            }
            return builder.build();
        }

        JsonValue toJson(Element elem) {
            /**
             * NOTE now JsonXML.toJson() handles exp (evaluation of expressions
             * in descendants). TODO separate evaluation out of JsonXML.
             */
            return JsonXml.toJson(elem);
        }

    }

    public abstract static class ListExpXml {

        /**
         * Finds list expression at the element
         * @param elem the element where an expression
         *        is found.
         * @return null if the given element does
         *         not represent a list expression.
         */
        @Nullable
        public static ListExpXml find(Element elem) {
            String name = XmlValue.nameOf(elem);
            if (Split.TAG_NAME.equals(name)) {
                return new Split(elem);
            } else if (FindAll.TAG_NAME.equals(name)) {
                return new FindAll(elem);
            } else if (Slice.TAG_NAME.equals(name)) {
                return new Slice(elem);
            } else if (MapPath.TAG_NAME.equals(name)) {
                return new MapPath(elem);
            }
            return null;
        }

        public abstract List<PrimitiveValue> getList();

        protected List<PrimitiveValue> contentValues(Element elem) {
            List<PrimitiveValue> result =
                    new ArrayList<PrimitiveValue>();
            List<Element> elems = Xml.getElements(elem);
            if (elems.isEmpty()) {
                result.add(PrimitiveValue.valueOf(
                        elem.getTextContent().trim()));
            } else {
                for (Element c : elems) {
                    ListExpXml lxml = ListExpXml.find(c);
                    if (lxml != null) {
                        result.addAll(lxml.getList());
                    } else {
                        result.add(getValue(c));
                    }
                }
            }
            return result;
        }

        /**
         * Evaluate descendants (ignore the exp associated with this element
         * itself).
         */
        protected ConfigValue eval(Element elem) {
            Element res = (Element) elem.cloneNode(false);
            for (Element c : Xml.getElements(elem)) {
                res.appendChild(ExpXml.evaluate(c));
            }
            return XmlValue.create(res);
        }
    }

    public static class FindAll extends ListExpXml {
        public static final String TAG_NAME = "findAll";
        public static final String ATTR_PATTERN = "pattern";
        public static final String ATTR_GROUP = "group";
        public static final String ATTR_DEFAULT = "default";
        private final Element elem;
        private final Pattern pattern;
        private final int group;

        FindAll(Element elem) {
            this.elem = elem;
            this.pattern = Pattern.compile(
                    Xml.getAttribute(ATTR_PATTERN, elem));
            this.group = Xml.getAttributeInt(
                    ATTR_GROUP, elem, 0);
        }

        @Override
        public List<PrimitiveValue> getList() {
            List<PrimitiveValue> result =
                    new ArrayList<PrimitiveValue>();
            for (PrimitiveValue content : contentValues(elem)) {
                String str = content.stringValue();
                Matcher match = pattern.matcher(str);
                while (match.find()) {
                    String value = match.group(group);
                    if (value != null) {
                        result.add(PrimitiveValue.valueOf(value));
                    }
                }
            }
            return result;
        }
    }

    /**
     * A list function that generates a sublist of an input list It has two
     * optional parameters: from (the start index, inclusive) and to (the end
     * index, exclusive). If from is omitted, the result starts at the beginning
     * of the input list. If to is omitted, the result ends at the end of the
     * input list. The input list can be explicitly specified by "of":
     * 
     * <pre>
     * &lt;slice&gt;
     *   &lt;to ref="..."&gt;
     *   &lt;of&gt;
     *      ....
     *   &lt;/of&gt;
     * &lt;/slice&gt;
     * </pre>
     *
     * <p>Or directly written if it is a list function (e.g. map, findAll).
     * 
     * <pre>
     * &lt;slice&gt;
     *   &lt;from ref="..."&gt;
     *   &lt;to ref="..."&gt;
     *   &lt;map&gt;
     *      ....
     *   &lt;/map&gt;
     * &lt;/slice&gt;
     * </pre>
     * 
     * @author tatemura
     *
     */
    public static class Slice extends ListExpXml {
        public static final String TAG_NAME = "slice";
        public static final String FROM = "from";
        public static final String TO = "to";
        public static final String OF = "of";
        private final Element elem;

        Slice(Element elem) {
            this.elem = elem;
        }

        @Override
        public List<PrimitiveValue> getList() {
            List<PrimitiveValue> input = inputList();
            ConfigValue vs = eval(elem);
            int fromIndex = vs.getInt(FROM, 0);
            if (fromIndex > input.size()) {
                throw new IndexOutOfBoundsException(
                        "sclice.from[" + fromIndex
                                + "] > input size["
                                + input.size()
                                + "]: " + pathOf(elem));
            } else if (fromIndex < 0) {
                throw new IndexOutOfBoundsException(
                        "sclice.from[" + fromIndex
                        + "] < 0: " + pathOf(elem));
            }
            int toIndex = vs.getInt(TO, -1);
            if (toIndex >= 0) {
                if (toIndex > input.size()) {
                    throw new IndexOutOfBoundsException(
                            "sclice.to[" + toIndex
                            + "] > input size["
                            + input.size() + "]: " + pathOf(elem));
                } else if (fromIndex > toIndex) {
                    throw new IndexOutOfBoundsException(
                            "sclice.from[" + fromIndex
                            + "] > slice.to[" + toIndex
                            + "]: " + pathOf(elem));
                }
                return input.subList(fromIndex, toIndex);
            } else {
                return input.subList(fromIndex, input.size());
            }
        }

        List<PrimitiveValue> inputList() {
            Element in = Xml.getSingleElement(OF, elem, false);
            if (in != null) {
                return contentValues(in);
            }
            /**
             * Otherwise input is implicit.
             */
            List<PrimitiveValue> result = new ArrayList<PrimitiveValue>();
            for (Element c : Xml.getElements(elem)) {
                ListExpXml lxml = ListExpXml.find(c);
                if (lxml != null) {
                    result.addAll(lxml.getList());
                }
            }
            return result;
        }
    }

    public static class MapPath extends ListExpXml {
        public static final String TAG_NAME = "map";
        public static final String ATTR_PATH = "path";
        private final Element elem;
        private final String[] path;

        MapPath(Element elem) {
            this.elem = elem;
            String attrPath =
                    Xml.getAttribute(ATTR_PATH, elem);
            this.path = attrPath.split("/");
        }

        @Override
        public List<PrimitiveValue> getList() {
            List<PrimitiveValue> result = new ArrayList<PrimitiveValue>();
            for (Element c : Xml.getElements(elem)) {
                PrimitiveValue val = extract(c, 0);
                if (val != null) {
                    result.add(val);
                }
            }
            return result;
        }

        PrimitiveValue extract(Element elem, int idx) {
            String name = path[idx];
            if (idx + 1 == path.length) {
                Attr attr = elem.getAttributeNode(name);
                if (attr != null) {
                    return PrimitiveValue.valueOf(attr.getValue());
                }
                Element child = Xml.getSingleElement(
                        name, elem, false);
                if (child != null) {
                    return getValue(child);
                }
            } else {
                Element child = Xml.getSingleElement(
                        name, elem, false);
                if (child != null) {
                    return extract(child, idx + 1);
                }
            }
            return null;
        }
    }

    public static class Split extends ListExpXml {
        public static final String TAG_NAME = "split";
        public static final String ATTR_WITH = "with";
        private final Element elem;

        Split(Element elem) {
            this.elem = elem;
        }

        @Override
        public List<PrimitiveValue> getList() {
            String sep =
                    Xml.getAttribute(ATTR_WITH, elem);
            List<PrimitiveValue> result =
                    new ArrayList<PrimitiveValue>();
            for (PrimitiveValue v : contentValues(elem)) {
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

    /**
     * Gets a path expression that refers to the
     * given element.
     * @param elem the element for which a path
     *        is generated.
     * @return a path from the root to the element.
     */
    public static String pathOf(Element elem) {
        List<String> path = new ArrayList<String>();
        path.add(XmlConfigDescription.nameOf(elem));
        Element current = elem;
        Node node;
        while ((node = current.getParentNode()) instanceof Element) {
            current = (Element) node;
            path.add("/");
            path.add(XmlConfigDescription.nameOf(current));
        }
        Collections.reverse(path);
        StringBuilder sb = new StringBuilder();
        for (String p : path) {
            sb.append(p);
        }
        return sb.toString();
    }
}