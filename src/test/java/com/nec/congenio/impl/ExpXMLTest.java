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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.w3c.dom.Element;

import com.nec.congenio.impl.ExpXML;
import com.nec.congenio.test.TestDataUtil;
import com.nec.congenio.xml.XML;

public class ExpXMLTest {


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
			ExpXML exp = test.exp();
			assertEquals(test.expectedString(), exp.getValue());
		} else {
			boolean failed = false;
			try {
				ExpXML exp = test.exp();
				assertNotNull(exp);
				exp.getValue();
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
		return XML.getElements("Test",
				TestDataUtil.getXMLResource(name).getDocumentElement());
	}
	static class TestCase {
		private final Element root;
		public TestCase(Element root)  {
			this.root = root;
		}
		public ExpXML exp() {
			return ExpXML.findExp(XML.getSingleElement("exp", root));
		}
		public boolean isSuccessCase() {
			return success() != null;
		}
		private Element success() {
			return XML.getSingleElement("success", root, false);
		}
		public String exception() {
			Element e = XML.getSingleElement("exception", root, false);
			if (e != null) {
				return e.getTextContent().trim();
			} else {
				return null;
			}
		}
		public String expectedString() {
			Element e = success();
			if (e != null) {
				return e.getTextContent().trim();
			}
			return null;
		}
	}
}
