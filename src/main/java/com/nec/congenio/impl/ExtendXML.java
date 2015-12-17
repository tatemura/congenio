/*******************************************************************************
 *   Copyright 2015 Junichi Tatemura
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package com.nec.congenio.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.xml.XML;

public class ExtendXML {
	public static final String EXTEND_HERE_REFERENCE = ".";

    private final Map<String, Element> map =
            new HashMap<String, Element>();
	/**
	 * Resolves inheritance (extension) of a given subtree
	 * of the document. Note that this is a destructive operation
	 * to the data: The given subtree is modified into
	 * the resolved subtree.
	 * @param e the root of the subtree.
	 * @param sp the search path to resolve the inheritance reference.
	 */
 	public static void resolve(Element e, SearchPath sp) {
		new ExtendXML().resolveInheritance(e, sp);
	}

	public static void inherit(Element e, Element proto) {
		new ExtendXML().inherit(e, proto, SearchPath.none());
	}

	/**
	 * Resolves inheritance (extension) of a given subtree
	 * of the document.
	 * @param e the root of the subtree.
	 * @param sp the search path to resolve the inheritance reference.
	 * @return true if this given subtree of the document
	 * contains deep extension (i.e., extends=".").
	 */
    private boolean resolveInheritance(Element e, SearchPath sp) {
        String protoPath = XML.getAttribute(ConfigDescription.ATTR_EXTENDS, e, null);
        if (protoPath == null) {
        	boolean deep = false;
            for (Element c : XML.getElements(e)) {
                deep |= resolveInheritance(c, sp);
            }
            return deep;
        } else if (isDeepExtendPoint(protoPath)) {
        	/**
        	 * NOTE this extends="." will remain in
        	 * the output.
        	 * (1) If this is called by inheritance,
        	 * it will be used and removed.
        	 * (2) Otherwise, it will kept in the final
        	 * output of inheritance resolution. Later,
        	 * reference resolution may use it.
        	 */
        	return true;
        } else {
            e.removeAttribute(ConfigDescription.ATTR_EXTENDS);
            ConfigPath path = sp.getPath(protoPath);
            inherit(e, getPrototype(path, sp), sp);
            return false;
        }
    }

    private Element getPrototype(ConfigPath path, SearchPath sp) {
		Element e = map.get(path.getResourceURI());
		if (e == null) {
			ConfigResource resource = path.getResource();
            e = resource.createElement();
        	resolveInheritance(e, resource.searchPath());
        	map.put(path.getResourceURI(), e);
		}
		if (path.hasDocPath()) {
			return XML.getSingleElement(path.getDocPath(), e);
		} else {
			return e;
		}
    }

    private boolean isDeepExtendPoint(String path) {
    	return EXTEND_HERE_REFERENCE.equals(path);
    }
    private void inherit(Element e, Element proto, SearchPath sp) {
        Document doc = e.getOwnerDocument();
        Map<String, Element> elemMap =
        		new HashMap<String, Element>();
        List<Element> elemList = new LinkedList<Element>();
        for (Element c : XML.getElements(e)) {
            elemMap.put(XMLConfigDescription.nameOf(c), c);
            elemList.add(c);
        }
        if (elemMap.isEmpty()) {
        	if (!e.getTextContent().trim().isEmpty()) {
            	/**
            	 * it has text content.
            	 * extension at the leaf.
            	 * Keep the content as is.
            	 */
            	return;
        	}
        }
        for (Node n : XML.getChildren(e)) {
        	e.removeChild(n);
        }
        for (Element cSrc : XML.getElements(proto)) {
        	String name = XMLConfigDescription.nameOf(cSrc);
        	if (elemMap.containsKey(name)) {
        		Element cDst = elemMap.remove(name);
        		elemList.remove(cDst);
                boolean deep = resolveInheritance(cDst, sp);
                if (deep) {
                    cDst.removeAttribute(ConfigDescription.ATTR_EXTENDS);
                	inherit(cDst, cSrc, sp);
                }
        		e.appendChild(cDst);
        	} else {
                e.appendChild(doc.importNode(cSrc, true));
            }
        }
        for (Element cExt : elemList) {
            resolveInheritance(cExt, sp);
        	e.appendChild(cExt);
        }
        for (Map.Entry<String, String> entry
                : XML.getAttributes(proto).entrySet()) {
            if (XML.getAttribute(entry.getKey(), e, null) == null) {
                e.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

}