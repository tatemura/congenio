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
package com.nec.congenio.test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.nec.congenio.xml.XML;


public final class TestDataUtil {

	private TestDataUtil() {
	}

	public static Document getXMLResource(String name) {
		return XML.parse(getFile(name));
	}
	public static List<Element> tests(String name) {
		return XML.getElements("Test",
				TestDataUtil.getXMLResource(name).getDocumentElement());
	}

    public static File getFile(String name) {
        URL url = getTestResource(name);
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public static URL getTestResource(String name) {
        URL url = TestDataUtil.class.getResource("/" + name);
        if (url == null) {
            url = TestDataUtil.class.getResource("/" + name + ".xml");
        }
        return url;
    }
}
