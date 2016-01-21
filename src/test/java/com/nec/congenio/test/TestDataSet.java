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
package com.nec.congenio.test;

import java.util.List;

import org.w3c.dom.Element;

import com.nec.congenio.impl.PathContext;
import com.nec.congenio.impl.path.SearchPath;
import com.nec.congenio.xml.XML;

public class TestDataSet {
	private String dirName;
	public TestDataSet(String dirName) {
		this.dirName = dirName;
	}
	public List<Element> testSet(String name) {
		return TestDataUtil.tests(dirName + "/" + name);
	}
	public PathContext createPathContext(Element e) {
		Element repo = XML.getSingleElement("repo", e, false);
		boolean repoFound = repo != null;
		MockPathContext path = (repoFound ? 
				MockPathContext.create(repo) : new MockPathContext());
		for (Element l : XML.getElements("lib", e)) {
			repoFound = true;
			MockPathContext lib = MockPathContext.create(l);
			path.setLib(l.getAttribute("name"), lib);
		}
		if (repoFound) {
			return path;
		}
		Element libs = XML.getSingleElement("libs", e, false);
		if (libs != null) {
			return SearchPath.create(TestDataUtil.getFile(dirName),
					libs.getTextContent().trim());
		}
		return SearchPath.create(TestDataUtil.getFile(dirName));
	}
}