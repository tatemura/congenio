package com.nec.congenio.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.w3c.dom.Element;

import com.nec.congenio.xml.XML;

public final class XMLValueUtil {

	private  XMLValueUtil() {
	}

	public static void assertEq(Element exp, Element act) {
		List<Element> expElems = XML.getElements(exp);
		List<Element> actElems = XML.getElements(act);
		for (int i = 0; i < expElems.size(); i++) {
			Element exp1 = expElems.get(i);
			Element act1 = actElems.get(i);
			assertEquals(XML.toString(exp1), XML.toString(act1));
		}
		assertEquals(XML.getAttributes(exp), XML.getAttributes(act));
	}
}
