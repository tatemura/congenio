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

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.nec.congenio.impl.ForLoop;
import com.nec.congenio.test.TestDataUtil;
import com.nec.congenio.xml.XML;

public class ForeachXMLTest {

	@Test
	public void testForEach() {
		successCases("forxml/foreach");
	}
	@Test
	public void testMultiForEach() {
		successCases("forxml/multi");
	}
	
	void successCases(String name) {
		for (Element e : TestDataUtil.tests(name)) {
			Document doc = e.getOwnerDocument();
			Element t = XML.getSingleElement("test", e);
			Element res = doc.createElement("success");
			for (Element x : ForLoop.unfold(t)) {
				res.appendChild(x);
			}
			Element r = XML.getSingleElement("success", e);
			XMLValueUtil.assertEq(r, res);
		}
	}

}
