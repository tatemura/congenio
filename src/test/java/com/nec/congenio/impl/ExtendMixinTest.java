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

import com.nec.congenio.test.TestDataSet;
import com.nec.congenio.xml.Xml;

public class ExtendMixinTest {
    private TestDataSet set = new TestDataSet("extendxml");

    @Test
    public void testMixin() {
        successCases("mixin");
    }

    private void successCases(String name) {
        for (Element e : set.testSet(name)) {
            Element test = Xml.getSingleElement("test", e);
            Element base = Xml.getSingleElement("base", e);
            ConfigResource res = set.createResource(e);
            ExtendXml.resolve(base, res);
            ExtendXml.resolve(test, base, res);
            Element expectedResult =
                    Xml.getSingleElement("success", e);
            XmlValueUtil.assertEq(expectedResult, test);
        }
    }
}
