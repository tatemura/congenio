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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nec.congenio.xml.Xml;

public final class XmlValueUtil {

    private XmlValueUtil() {
    }

    /**
     * Asserts that given elements are equivalent as
     * a config value.
     * @param exp the expected element.
     * @param act the acutal element.
     */
    public static void assertEq(Element exp, Element act) {
        String path = path(exp);
        List<Element> expElems = Xml.getElements(exp);
        List<Element> actElems = Xml.getElements(act);
        for (int i = 0; i < expElems.size(); i++) {
            Element exp1 = expElems.get(i);
            Element act1 = actElems.get(i);
            assertEquals(path,
                    exp1.getTagName(),
                    act1.getTagName());
            assertEq(exp1, act1);
        }
        if (expElems.isEmpty()) {
            assertEquals(path,
                    exp.getTextContent().trim(),
                    act.getTextContent().trim());
        }
        assertEquals(path,
                Xml.getAttributes(exp),
                Xml.getAttributes(act));
    }

    /**
     * Generates a path expression that refers to
     * the given element.
     * @param elem the element for which a path is
     *        generated.
     * @return a path from the root to the given element.
     */
    public static String path(Element elem) {
        String path = "/" + name(elem);
        Node currentNode = elem;
        while ((currentNode = currentNode.getParentNode()) != null) {
            if (currentNode instanceof Element) {
                path = "/" + name((Element) currentNode) + path;
            } else {
                break;
            }
        }
        return path;
    }

    private static String name(Element elem) {
        String name = elem.getAttribute("name");
        if (!name.isEmpty()) {
            return elem.getTagName() + "[" + name + "]";
        } else {
            return elem.getTagName();
        }
    }
}
