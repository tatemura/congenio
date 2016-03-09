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

import org.junit.Test;
import org.w3c.dom.Element;

import com.nec.congenio.impl.ExtendXml;
import com.nec.congenio.test.TestDataSet;
import com.nec.congenio.xml.Xml;

public class ExtendXmlTest {
    private TestDataSet set = new TestDataSet("extendxml");

    @Test
    public void testSimpleExtends() {
        successCases("extend");
    }

    @Test
    public void testNamedExtends() {
        successCases("extendname");
    }

    @Test
    public void testDocPathExtends() {
        successCases("docpath");
    }

    @Test
    public void testDeepExtends() {
        successCases("deep");
    }

    @Test
    public void testExtendsWithAttrs() {
        successCases("extend_attrs");
    }

    @Test
    public void testLibExtends() {
        successCases("lib");
    }

    @Test
    public void testExtendsFromDir() {
        successCases("extenddirs");
    }

    @Test
    public void testExtendsWithMixin() {
        successCases("extendmixin");
    }

    private void successCases(String name) {
        for (Element e : set.testSet(name)) {
            Element test = Xml.getSingleElement("test", e);
            ExtendXml.resolve(test, set.createResource(e));
            Element expected = Xml.getSingleElement("success", e);
            XmlValueUtil.assertEq(expected, test);
        }
    }
}
