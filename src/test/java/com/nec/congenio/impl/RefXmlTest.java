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

import com.nec.congenio.impl.RefXml;
import com.nec.congenio.test.TestDataUtil;
import com.nec.congenio.xml.Xml;

public class RefXmlTest {

    @Test
    public void testSimpleRef() {
        for (Element e : TestDataUtil.tests("refxml/reference")) {
            Element test = Xml.getSingleElement("test", e);
            Element expected = Xml.getSingleElement("success", e);
            XmlValueUtil.assertEq(expected, RefXml.resolve(test));
        }
    }

    @Test
    public void testCascadeRef() {
        for (Element e : TestDataUtil.tests("refxml/refcascade")) {
            Element test = Xml.getSingleElement("test", e);
            Element expected = Xml.getSingleElement("success", e);
            XmlValueUtil.assertEq(expected, RefXml.resolve(test));
        }
    }

    @Test
    public void testRefExtend() {
        for (Element e : TestDataUtil.tests("refxml/refextend")) {
            Element test = Xml.getSingleElement("test", e);
            Element expected = Xml.getSingleElement("success", e);
            XmlValueUtil.assertEq(expected, RefXml.resolve(test));
        }
    }

    @Test
    public void testRefAndExp() {
        for (Element e : TestDataUtil.tests("refxml/refexp")) {
            Element test = Xml.getSingleElement("test", e);
            Element expected = Xml.getSingleElement("success", e);
            XmlValueUtil.assertEq(expected,
                    ExpXml.evaluate(RefXml.resolve(test)));
        }
    }

}
