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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nec.congenio.xml.XML;

public final class XMLValueUtil {

	private  XMLValueUtil() {
	}

	public static void assertEq(Element exp, Element act) {
		String path = path(exp);
		List<Element> expElems = XML.getElements(exp);
		List<Element> actElems = XML.getElements(act);
		for (int i = 0; i < expElems.size(); i++) {
			Element exp1 = expElems.get(i);
			Element act1 = actElems.get(i);
			assertEquals(path, exp1.getTagName(), act1.getTagName());
			assertEq(exp1, act1);
		}
		if (expElems.isEmpty()) {
			assertEquals(path,
					exp.getTextContent().trim(), act.getTextContent().trim());
		}
		assertEquals(path, XML.getAttributes(exp), XML.getAttributes(act));
	}
	public static String path(Element e) {
		String path = "/" + name(e);
		Node p = e;
		while ((p = p.getParentNode()) != null) {
			if (p instanceof Element) {
				path = "/" + name((Element) p) + path;
			} else {
				break;
			}
		}
		return path;
	}
	private static String name(Element e) {
		String name = e.getAttribute("name");
		if (!name.isEmpty()) {
			return e.getTagName() + "[" + name + "]";
		} else {
			return e.getTagName();
		}
	}
}
