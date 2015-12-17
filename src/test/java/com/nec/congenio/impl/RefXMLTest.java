package com.nec.congenio.impl;

import org.junit.Test;
import org.w3c.dom.Element;

import com.nec.congenio.impl.RefXML;
import com.nec.congenio.test.TestDataUtil;
import com.nec.congenio.xml.XML;

public class RefXMLTest {

	@Test
	public void testSimpleRef() {
		for (Element e : TestDataUtil.tests("refxml/reference")) {
			Element t = XML.getSingleElement("test", e);
			Element r = XML.getSingleElement("success", e);
			XMLValueUtil.assertEq(r, RefXML.resolve(t));
		}
	}
	@Test
	public void testCascadeRef() {
		for (Element e : TestDataUtil.tests("refxml/refcascade")) {
			Element t = XML.getSingleElement("test", e);
			Element r = XML.getSingleElement("success", e);
			XMLValueUtil.assertEq(r, RefXML.resolve(t));
		}
	}
	@Test
	public void testRefExtend() {
		for (Element e : TestDataUtil.tests("refxml/refextend")) {
			Element t = XML.getSingleElement("test", e);
			Element r = XML.getSingleElement("success", e);
			XMLValueUtil.assertEq(r, RefXML.resolve(t));
		}
	}


}
