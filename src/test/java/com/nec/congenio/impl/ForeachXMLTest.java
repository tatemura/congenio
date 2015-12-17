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
