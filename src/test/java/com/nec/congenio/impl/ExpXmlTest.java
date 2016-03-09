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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.w3c.dom.Element;

import com.nec.congenio.impl.ExpXml;
import com.nec.congenio.test.TestDataUtil;
import com.nec.congenio.xml.Xml;

public class ExpXmlTest {

    @Test
    public void testConcat() {
        doCaseSet("expxml/concat");
    }

    @Test
    public void testSlice() {
        doCaseSet("expxml/slice");
    }

    @Test
    public void testJsonDump() {
        doCaseSet("expxml/jsondump");
    }

    private void doCaseSet(String name) {
        for (Element e : tests(name)) {
            TestCase test = new TestCase(e);
            doCase(test);
        }
    }

    private void doCase(TestCase test) {
        if (test.isSuccessCase()) {
            ExpXml exp = test.exp();
            assertEquals(test.expectedString(), exp.stringValue());
        } else {
            boolean failed = false;
            try {
                ExpXml exp = test.exp();
                assertNotNull(exp);
                exp.stringValue();
            } catch (Exception ex) {
                failed = true;
                String exname = test.exception();
                if (exname != null) {
                    assertEquals(exname, ex.getClass().getName());
                }
            }
            assertTrue(failed);
        }
    }

    protected List<Element> tests(String name) {
        return Xml.getElements("Test", TestDataUtil.getXmlResource(name).getDocumentElement());
    }

    static class TestCase {
        private final Element root;

        public TestCase(Element root) {
            this.root = root;
        }

        public ExpXml exp() {
            return ExpXml.findExp(Xml.getSingleElement("exp", root));
        }

        public boolean isSuccessCase() {
            return success() != null;
        }

        private Element success() {
            return Xml.getSingleElement("success", root, false);
        }

        public String exception() {
            Element elem = Xml.getSingleElement("exception", root, false);
            if (elem != null) {
                return elem.getTextContent().trim();
            } else {
                return null;
            }
        }

        public String expectedString() {
            Element elem = success();
            if (elem != null) {
                return elem.getTextContent().trim();
            }
            return null;
        }
    }
}
