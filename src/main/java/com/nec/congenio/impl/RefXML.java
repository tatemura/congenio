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

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.ConfigException;
import com.nec.congenio.xml.XML;

public final class RefXML {

	private RefXML() {
	}

	public static Element resolve(Element e) {
		Element result = (Element) e.cloneNode(false);
		List<Element> children = XML.getElements(e);
		if (children.isEmpty()) {
	    	Element ref = deref(e);
	        if (ref != null) {
	        	assignFromRef(result, ref);
	            result.removeAttribute(ConfigDescription.ATTR_REF);
	        } else {
	        	result.setTextContent(e.getTextContent());
	        }
		} else {
	        for (Element c : children) {
	            result.appendChild(resolve(c));
	        }
	    	Element ref = deref(e);
	        if (ref != null) {
	        	ExtendXML.inherit(result, resolve(ref));
	            result.removeAttribute(ConfigDescription.ATTR_REF);
	        }
		}

        return result;
    }

	/**
	 * Gets the element referred to by the
	 * given element.
	 * @param e
	 * @return null if e has no reference
	 */
	protected static Element deref(Element e) {
		RefPath path = refAt(e);
		if (path == null) {
			return null;
		}
		Element current = e;
		Node n;
		while ((n = current.getParentNode()) instanceof Element) {
			Element parent = (Element) n;
			if (followable(path, parent, current)) {
                Element ref = find(path, parent);
                if (ref != null) {
                	return ref;
                }
			}
			current = parent;
		}
        throw new ConfigException("missing reference "
                + e.getTagName() + "@ref=\"" + path
                +"\" at " + ExpXML.pathOf(e));
	}

	static Element find(RefPath path, Element e) {
		Element e1 = XML.getSingleElement(path.thisStep(), e, false);
		if (e1 != null) {
			if (path.isSingle()) {
				return e1;
			} else {
				return find(path.nextPath(), e1);
			}
		}
		Element ref = deref(e);
		if (ref != null) {
			return find(path, ref);
		}
		return null;
	}
	static boolean followable(RefPath path, Element e, Element dontFollow) {
		Element e1 = XML.getSingleElement(path.thisStep(), e, false);
		if (e1 == null) {
			return hasRef(e);
		}
		return e1 != dontFollow;
	}

	static boolean hasRef(Element e) {
		return XML.getAttribute(ConfigDescription.ATTR_REF, e, null) != null;
	}
	static RefPath refAt(Element e) {
		String ref = XML.getAttribute(ConfigDescription.ATTR_REF, e, null);
		if (ref != null) {
			return new RefPath(ref);
		} else {
			return null;
		}
	}

    protected static void assignFromRef(Element e, Element ref) {
    	/**
    	 * NOTE destructive copy from resolved to e
    	 * does not affect the original ref.
    	 */
    	Element resolved = resolve(ref);
        for (Node n : XML.getChildren(resolved)) {
            e.appendChild(n);
        }
        for (Map.Entry<String, String> entry
                : XML.getAttributes(resolved).entrySet()) {
            if (XML.getAttribute(entry.getKey(), e, null) == null) {
                e.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    public static class RefPath {
    	String[] steps;
    	public RefPath(String path) {
    		steps = path.split("/");
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
    	public String toString() {
    		StringBuilder sb = new StringBuilder();
    		for (int i = 0; i < steps.length; i++) {
    			if (i > 0) {
    				sb.append('/');
    			}
    			sb.append(steps[i]);
    		}
    		return sb.toString();
    	}
    }
}
