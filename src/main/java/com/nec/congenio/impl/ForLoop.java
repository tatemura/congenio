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
import com.nec.congenio.xml.Xml;

/**
 * <pre>
 * &lt;a&gt;
 *   &lt;foreach name="b" sep=" "&gt;1 2 3&lt;/foreach&gt;
 *   ...
 * &lt;/a&gt;
 * </pre>
 * 
 * <p>the foreach expression is expanded to a sequence of three XML documents:
 * 
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
 * 
 * <p>When the document contains multiple foreach expression, Cartesian product of
 * them are created.
 * 
 * <pre>
 * &lt;a&gt;
 *   &lt;foreach name="b"&gt;...&lt;/foreach&gt;
 *   &lt;foreach name="c"&gt;...&lt;/foreach&gt;
 *   ...
 * &lt;/a&gt;
 * </pre>
 * 
 * <p>It generates a sequence of XML documents in the following form:
 * 
 * <pre>
 * &lt;a&gt;
 *   &lt;b&gt;...&lt;/b&gt;
 *   &lt;c&gt;...&lt;/c&gt;
 *   ...
 * &lt;/a>
 * </pre>
 * 
 * @author tatemura
 *
 */
public class ForLoop implements Iterable<Element> {
    public static final String ELEMENT_FOREACH = "foreach";
    private final Element orig;
    private final List<ForLoop.Each> eachs =
            new ArrayList<ForLoop.Each>();

    /**
     * Unfolds the loop and generates a sequence of elements
     * from the source element.
     * @param elem the source element that is unfolded.
     * @return an iterable that represents a sequence
     *         of elements.
     */
    public static Iterable<Element> unfold(Element elem) {
        if (isForLoop(elem)) {
            return new ForLoop(elem);
        } else {
            return Arrays.asList(elem);
        }
    }

    public static boolean isForLoop(Element elem) {
        return !Xml.getElements(ELEMENT_FOREACH, elem).isEmpty();
    }

    protected static boolean isForeach(Element elem) {
        return elem.getTagName().equals(ELEMENT_FOREACH);
    }

    /**
     * Creates a for-loop interpreter from the element.
     * @param elem the element that contains for loops.
     */
    public ForLoop(Element elem) {
        this.orig = elem;
        for (Element c : Xml.getElements(elem)) {
            if (isForeach(c)) {
                eachs.add(Each.create(c));
            }
        }
    }

    /**
     * Creates a list of for-loop bindings, each of
     * which corresponds to variable bindings for
     * an output element
     * @return a list of variable bindings.
     */
    public List<ForLoop.Binding> bindings() {
        List<Binding> bins = new ArrayList<Binding>();
        enumBindings(0, new ArrayList<Element>(), bins);
        return bins;
    }

    protected void enumBindings(int idx,
            List<Element> eachList, List<Binding> res) {
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
        return new ForLoopIterator(orig, bindings().iterator());
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
            Element result =
                    (Element) orig.cloneNode(false);
            int idx = 0;
            for (Element c : Xml.getElements(orig)) {
                if (isForeach(c)) {
                    result.appendChild(
                            bin.get(idx).cloneNode(true));
                    idx++;
                } else {
                    result.appendChild(
                            c.cloneNode(true));
                }
            }
            return result;
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
        private final Element root;
        private final String name;

        protected Each(Element elem) {
            this.root = elem;
            this.name = Xml.getAttribute("name", elem);
        }

        public static Each create(Element elem) {
            String sep = Xml.getAttribute("sep", elem, null);
            if (sep != null) {
                return new EachTextSplit(sep, elem);
            }
            String range = Xml.getAttribute("range", elem, null);
            if (range != null) {
                return new EachIntRange(range, elem);
            }
            return new EachElement(elem);
        }

        public static List<Element> elements(Element elem) {
            return create(elem).elements();
        }

        public abstract List<Element> elements();

        protected Element newElement() {
            return root.getOwnerDocument().createElement(name);
        }

    }

    static class EachIntRange extends Each {
        private static final Pattern PATTERN =
                Pattern.compile("^(\\d+)\\.\\.(\\d+)$");
        private int from;
        private int to;

        public EachIntRange(String range, Element elem) {
            super(elem);
            Matcher match = PATTERN.matcher(range);
            if (match.matches()) {
                from = Integer.parseInt(match.group(1));
                to = Integer.parseInt(match.group(2));
                if (from > to) {
                    throw new ConfigException("invalid range=" + range);
                }
            } else {
                throw new ConfigException("invalid range=" + range);
            }
        }

        @Override
        public List<Element> elements() {
            List<Element> result = new ArrayList<Element>();
            for (int i = from; i <= to; i++) {
                Element each = newElement();
                each.setTextContent(Integer.toString(i));
                result.add(each);
            }
            return result;
        }

    }

    static class EachTextSplit extends Each {
        private static final String ATTR_VALUE = "value";
        private final String sep;
        private final Element root;

        public EachTextSplit(String sep, Element elem) {
            super(elem);
            this.sep = sep;
            this.root = elem;
        }

        private String textValue() {
            String value = Xml.getAttribute(
                    ATTR_VALUE, root, null);
            if (value != null) {
                return value;
            }
            return root.getTextContent().trim();
        }

        @Override
        public List<Element> elements() {
            List<Element> result =
                    new ArrayList<Element>();
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
        private Element root;

        EachElement(Element elem) {
            super(elem);
            this.root = elem;
        }

        @Override
        public List<Element> elements() {
            List<Element> result = new ArrayList<Element>();
            for (Element c : Xml.getElements(root)) {
                Element each = newElement();
                NodeList nlist = c.getChildNodes();
                for (int i = 0; i < nlist.getLength(); i++) {
                    Node node = nlist.item(i);
                    each.appendChild(node.cloneNode(true));
                }
                result.add(each);
            }
            return result;
        }

    }
}