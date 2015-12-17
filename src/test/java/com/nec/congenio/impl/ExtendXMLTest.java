package com.nec.congenio.impl;

import org.junit.Test;
import org.w3c.dom.Element;

import com.nec.congenio.impl.ExtendXML;
import com.nec.congenio.test.MockSearchPath;
import com.nec.congenio.test.TestDataUtil;
import com.nec.congenio.xml.XML;

public class ExtendXMLTest {

	@Test
	public void testSimpleExtends() {
		successCases("extendxml/extend");
	}

	@Test
	public void testNamedExtends() {
		successCases("extendxml/extendname");
	}
	@Test
	public void testDocPathExtends() {
		successCases("extendxml/docpath");
	}
	@Test
	public void testDeepExtends() {
		successCases("extendxml/deep");
	}

	private void successCases(String name) {
		for (Element e : TestDataUtil.tests(name)) {
			Element t = XML.getSingleElement("test", e);
			MockSearchPath path = MockSearchPath.create(XML.getSingleElement("repo", e));
			ExtendXML.resolve(t, path);
			Element r = XML.getSingleElement("success", e);
			XMLValueUtil.assertEq(r, t);
		}
	}
}
