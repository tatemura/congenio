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

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.ConfigException;
import com.nec.congenio.xml.Xml;

/**
 * A class that resolves references ("ref").
 * @author tatemura
 *
 */
public final class RefXml {

    private RefXml() {
    }

    /**
     * Resolves references in the element.
     * @param elem an element to be resolved.
     * @return an element after resolution.
     */
    public static Element resolve(Element elem) {
        Element result = (Element) elem.cloneNode(false);
        List<Element> children = Xml.getElements(elem);
        if (children.isEmpty()) {
            Element ref = deref(elem);
            if (ref != null) {
                assignFromRef(result, ref);
                result.removeAttribute(ConfigDescription.ATTR_REF);
            } else {
                result.setTextContent(elem.getTextContent());
            }
        } else {
            for (Element c : children) {
                result.appendChild(resolve(c));
            }
            Element ref = deref(elem);
            if (ref != null) {
                ExtendXml.inherit(result, resolve(ref));
                result.removeAttribute(ConfigDescription.ATTR_REF);
            }
        }

        return result;
    }

    /**
     * Gets the element referred to by the given element.
     * 
     * @param elem the element that refers to another element.
     * @return null if e has no reference
     */
    protected static Element deref(Element elem) {
        RefPath path = refAt(elem);
        if (path == null) {
            return null;
        }
        Element current = elem;
        Node node;
        while ((node = current.getParentNode()) instanceof Element) {
            Element parent = (Element) node;
            if (followable(path, parent, current)) {
                Element ref = find(path, parent);
                if (ref != null) {
                    return ref;
                }
            }
            current = parent;
        }
        throw new ConfigException(
                "missing reference "
                 + elem.getTagName() + "@ref=\""
                 + path + "\" at " + ExpXml.pathOf(elem));
    }

    static Element find(RefPath path, Element elem) {
        Element e1 = Xml.getSingleElement(
                path.thisStep(), elem, false);
        if (e1 != null) {
            if (path.isSingle()) {
                return e1;
            } else {
                return find(path.nextPath(), e1);
            }
        }
        Element ref = deref(elem);
        if (ref != null) {
            return find(path, ref);
        }
        return null;
    }

    static boolean followable(RefPath path,
            Element elem, Element dontFollow) {
        Element e1 = Xml.getSingleElement(
                path.thisStep(), elem, false);
        if (e1 == null) {
            return hasRef(elem);
        }
        return e1 != dontFollow;
    }

    static boolean hasRef(Element elem) {
        return Xml.getAttribute(ConfigDescription.ATTR_REF,
                elem, null) != null;
    }

    static RefPath refAt(Element elem) {
        String ref = Xml.getAttribute(
                ConfigDescription.ATTR_REF, elem, null);
        if (ref != null) {
            return new RefPath(ref);
        } else {
            return null;
        }
    }

    protected static void assignFromRef(Element elem, Element ref) {
        /**
         * NOTE destructive copy from resolved to e does not affect the original
         * ref.
         */
        Element resolved = resolve(ref);
        for (Node n : Xml.getChildren(resolved)) {
            elem.appendChild(n);
        }
        for (Map.Entry<String, String> entry
                : Xml.getAttributes(resolved).entrySet()) {
            if (Xml.getAttribute(
                    entry.getKey(), elem, null) == null) {
                elem.setAttribute(
                        entry.getKey(), entry.getValue());
            }
        }
    }

    public static class RefPath {
        public static final String SEP = "/";
        private final String[] steps;

        public RefPath(String path) {
            steps = path.split(SEP);
        }

        public RefPath(String[] steps) {
            this.steps = steps;
        }

        public String[] steps() {
            return steps;
        }

        public String thisStep() {
            return steps[0];
        }

        /**
         * Gets the next step of the path.
         * @return the reference path
         *         after one step forwarded.
         */
        public RefPath nextPath() {
            String[] nexts = new String[steps.length - 1];
            for (int i = 0; i < nexts.length; i++) {
                nexts[i] = steps[i + 1];
            }
            return new RefPath(nexts);
        }

        public boolean isSingle() {
            return steps.length == 1;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < steps.length; i++) {
                if (i > 0) {
                    sb.append(SEP);
                }
                sb.append(steps[i]);
            }
            return sb.toString();
        }
    }
}
