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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nec.congenio.ConfigException;
import com.nec.congenio.value.xml.Attrs;
import com.nec.congenio.value.xml.XmlValue;
import com.nec.congenio.xml.Xml;

public class ExtendXml {

    /**
     * Resolves inheritance (extension) of a given subtree of the document. Note
     * that this is a destructive operation to the data: The given subtree is
     * modified into the resolved subtree.
     * 
     * @param elem
     *            the root of the subtree.
     * @param res
     *            resource.
     */
    public static void resolve(Element elem, ConfigResource res) {
        new ExtendXml().resolveInheritance(elem,
                EvalContext.create(res));
    }

    /**
     * Resolves inheritance (extension) of a given subtree of the document
     * with a base document.
     * @param elem the root of the subtree (which will be updated)
     * @param base the root of the base
     * @param res resource.
     */
    public static void resolve(Element elem, Element base, ConfigResource res) {
        new ExtendXml().resolveMixin(elem, base,
                EvalContext.create(res));
    }

    public static void inherit(Element elem, Element proto) {
        new ExtendXml().inheritElement(elem, proto,
                new EvalContext());
    }

    /**
     * Resolves inheritance (extension) of a given subtree of the document.
     * 
     * @param elem
     *            the root of the subtree.
     * @param pc
     *            the path context to resolve the inheritance reference.
     * @return true if this given subtree of the document contains deep
     *         extension (i.e., extends=".").
     */
    private boolean resolveInheritance(Element elem, EvalContext ctxt) {
        ExtendPath extPath = ExtendPath.find(elem);
        if (extPath == null) {
            boolean deep = false;
            for (Element c : Xml.getElements(elem)) {
                deep |= resolveInheritance(c, ctxt);
            }
            return deep;
        } else if (extPath.isDeepExtendPoint()) {
            /**
             * NOTE this extends="." will remain in the output. (1) If this is
             * called by inheritance, it will be used and removed. (2)
             * Otherwise, it will be kept in the final output of inheritance
             * resolution. Later, reference resolution may use it.
             */
            return true;
        }
        Element base = getPrototype(ctxt.of(extPath.getPath(), elem));
        for (String m : extPath.getMixins()) {
            base = getMixin(base, ctxt.of(m, elem));
        }
        ExtendPath.remove(elem);
        inheritElement(elem, base, ctxt);
        return false;
    }

    private void resolveMixin(Element elem, Element base, EvalContext ctxt) {
        ExtendPath extPath = ExtendPath.find(elem);
        if (extPath == null) {
            inheritElement(elem, base, ctxt);
        } else {
            Element proto = getMixin(base,
                    ctxt.of(extPath.getPath(), elem));
            for (String m : extPath.getMixins()) {
                proto = getMixin(proto, ctxt.of(m, elem));
            }
            ExtendPath.remove(elem);
            inheritElement(elem, proto, ctxt);
        }
    }

    private Element getMixin(Element base, EvalContext ctxt) {
        ConfigResource resource = ctxt.getCurrentResource();
        Element elem = resource.createElement();
        elem = resolveDocPath(elem, ctxt);
        resolveMixin(elem, base, ctxt);
        return elem;
    }

    private Element getPrototype(EvalContext ctxt) {
        ConfigResource resource = ctxt.getCurrentResource();
        Element elem = resource.createElement();
        resolveInheritance(elem, ctxt);
        return resolveDocPath(elem, ctxt);
    }

    private Element resolveDocPath(Element elem, EvalContext ctxt) {
        if (ctxt.hasDocPath()) {
            Element sub = Xml.getSingleElement(
                    ctxt.getDocPath(), elem, false);
            if (sub != null) {
                return sub;
            } else {
                ctxt.printResourceTrace();
                throw new ConfigException("path ("
                + ctxt.getDocPath() + ") not found");
            }
        } else {
            return elem;
        }
    }

    private void inheritElement(Element elem,
            Element proto, EvalContext ctxt) {
        Document doc = elem.getOwnerDocument();
        Map<String, Element> elemMap = new HashMap<String, Element>();
        List<Element> elemList = new LinkedList<Element>();
        for (Element c : Xml.getElements(elem)) {
            elemMap.put(XmlConfigDescription.nameOf(c), c);
            elemList.add(c);
        }
        List<Element> sources = Xml.getElements(proto);
        if (elemMap.isEmpty()) {
            if (!elem.getTextContent().trim().isEmpty()) {
                /**
                 * it has text content. extension at the leaf. Keep the content
                 * as is.
                 */
                return;
            }
        }
        for (Node n : Xml.getChildren(elem)) {
            elem.removeChild(n);
        }
        for (Element src : sources) {
            String name = XmlConfigDescription.nameOf(src);
            if (elemMap.containsKey(name)) {
                Element dst = elemMap.remove(name);
                elemList.remove(dst);
                boolean deep = resolveInheritance(dst, ctxt);
                if (deep) {
                    ExtendPath.remove(dst);
                    inheritElement(dst, src, ctxt);
                } else {
                    inheritAttrs(dst, src);
                }
                elem.appendChild(dst);
            } else {
                elem.appendChild(doc.importNode(src, true));
            }
        }
        for (Element ext : elemList) {
            resolveInheritance(ext, ctxt);
            elem.appendChild(ext);
        }
        if (sources.isEmpty() && elemMap.isEmpty()) {
            /**
             * leaf to leaf inheritance
             */
            elem.setTextContent(proto.getTextContent().trim());
        }
        inheritAttrs(elem, proto);
    }

    private void inheritAttrs(Element elem, Element proto) {
        for (Map.Entry<String, String> entry
                : Xml.getAttributes(proto).entrySet()) {
            String name = entry.getKey();
            if (Attrs.VALUE.equals(name)) {
                if (!XmlValue.valueExists(elem)) {
                    elem.setAttribute(name, entry.getValue());
                }
            } else if (Xml.getAttribute(name, elem, null) == null) {
                elem.setAttribute(name, entry.getValue());
            }
        }
    }

}