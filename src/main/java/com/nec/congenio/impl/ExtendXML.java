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
import com.nec.congenio.value.xml.XMLValue;
import com.nec.congenio.xml.XML;

public class ExtendXML {

//    private final Map<String, Element> map =
//            new HashMap<String, Element>();
	/**
	 * Resolves inheritance (extension) of a given subtree
	 * of the document. Note that this is a destructive operation
	 * to the data: The given subtree is modified into
	 * the resolved subtree.
	 * @param e the root of the subtree.
	 * @param res resource.
	 */
  	public static void resolve(Element e, ConfigResource res) {
		new ExtendXML().resolveInheritance(e, EvalContext.create(res));
	}

 	public static void resolve(Element e, Element base, ConfigResource res) {
 		new ExtendXML().resolveMixin(e, base, EvalContext.create(res));
 	}

	public static void inherit(Element e, Element proto) {
		new ExtendXML().inherit(e, proto, new EvalContext());
	}

	/**
	 * Resolves inheritance (extension) of a given subtree
	 * of the document.
	 * @param e the root of the subtree.
	 * @param pc the path context to resolve the inheritance reference.
	 * @return true if this given subtree of the document
	 * contains deep extension (i.e., extends=".").
	 */
    private boolean resolveInheritance(Element e, EvalContext ctxt) {
    	ExtendPath p = ExtendPath.find(e);
        if (p == null) {
        	boolean deep = false;
            for (Element c : XML.getElements(e)) {
                deep |= resolveInheritance(c, ctxt);
            }
            return deep;
        } else if (p.isDeepExtendPoint()) {
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
        } 
    	Element base = getPrototype(ctxt.of(p.getPath()));
    	for (String m : p.getMixins()) {
            base = getMixin(base, ctxt.of(m));
    	}
    	ExtendPath.remove(e);
        inherit(e, base, ctxt);
        return false;
    }

    private void resolveMixin(Element e, Element base, EvalContext ctxt) {
    	ExtendPath p = ExtendPath.find(e);
        if (p == null) {
        	inherit(e, base, ctxt);
        } else {
        	Element proto = getMixin(base, ctxt.of(p.getPath()));
        	for (String m : p.getMixins()) {
        		proto = getMixin(proto, ctxt.of(m));
        	}
        	ExtendPath.remove(e);
        	inherit(e, proto, ctxt);
        }
    }

    private Element getMixin(Element base, EvalContext ctxt) {
    	ConfigResource resource = ctxt.getCurrentResource();
    	Element e = resource.createElement();
        e = resolveDocPath(e, ctxt);
    	resolveMixin(e, base, ctxt);
        return e;
    }

    private Element getPrototype(EvalContext ctxt) {
    	ConfigResource resource = ctxt.getCurrentResource();
    	Element e = resource.createElement();
    	resolveInheritance(e, ctxt);
    	return resolveDocPath(e, ctxt);
    }

    private Element resolveDocPath(Element e, EvalContext ctxt) {
 		if (ctxt.hasDocPath()) {
			Element sub = XML.getSingleElement(ctxt.getDocPath(), e, false);
			if (sub != null) {
				return sub;
			} else {
				ctxt.printResourceTrace();
				throw new ConfigException(
				"path (" + ctxt.getDocPath() + ") not found");
			}
		} else {
			return e;
		}
    }

    private void inherit(Element e, Element proto, EvalContext ctxt) {
        Document doc = e.getOwnerDocument();
        Map<String, Element> elemMap =
        		new HashMap<String, Element>();
        List<Element> elemList = new LinkedList<Element>();
        for (Element c : XML.getElements(e)) {
            elemMap.put(XMLConfigDescription.nameOf(c), c);
            elemList.add(c);
        }
        List<Element> sources = XML.getElements(proto);
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
        for (Element cSrc : sources) {
        	String name = XMLConfigDescription.nameOf(cSrc);
        	if (elemMap.containsKey(name)) {
        		Element cDst = elemMap.remove(name);
        		elemList.remove(cDst);
                boolean deep = resolveInheritance(cDst, ctxt);
                if (deep) {
                	ExtendPath.remove(cDst);
                	inherit(cDst, cSrc, ctxt);
                } else {
                	inheritAttrs(cDst, cSrc);
                }
        		e.appendChild(cDst);
        	} else {
                e.appendChild(doc.importNode(cSrc, true));
            }
        }
        for (Element cExt : elemList) {
            resolveInheritance(cExt, ctxt);
        	e.appendChild(cExt);
        }
        if (sources.isEmpty() && elemMap.isEmpty()) {
        	/**
        	 * leaf to leaf inheritance
        	 */
        	e.setTextContent(proto.getTextContent().trim());
        }
        inheritAttrs(e, proto);
    }
    private void inheritAttrs(Element e, Element proto) {
        for (Map.Entry<String, String> entry
                : XML.getAttributes(proto).entrySet()) {
        	String name = entry.getKey();
        	if (Attrs.VALUE.equals(name)) {
        		if (!XMLValue.hasValue(e)) {
        			e.setAttribute(name, entry.getValue());
        		}
        	} else if (XML.getAttribute(name, e, null) == null) {
                e.setAttribute(name, entry.getValue());
            }
        }
    }

}