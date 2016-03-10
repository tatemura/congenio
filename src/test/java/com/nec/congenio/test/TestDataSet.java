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
import java.util.Properties;

import org.w3c.dom.Element;

import com.nec.congenio.ConfigProperties;
import com.nec.congenio.impl.ConfigResource;
import com.nec.congenio.impl.path.ResourceFinder;
import com.nec.congenio.impl.path.SearchPath;
import com.nec.congenio.xml.Xml;

public class TestDataSet {
    private String dirName;

    public TestDataSet(String dirName) {
        this.dirName = dirName;
    }

    public List<Element> testSet(String name) {
        return TestDataUtil.tests(dirName + "/" + name);
    }

    /**
     * Creates a mock resource finder from the given test data.
     * @param elem the element that contains a test data.
     * @return a resource finder used in a test.
     */
    public ResourceFinder createResourceFinder(Element elem) {
        Element repo = Xml.getSingleElement("repo", elem, false);
        boolean repoFound = repo != null;
        MockResourceFinder path =
                (repoFound
                        ? MockResourceFinder.create(repo)
                                : new MockResourceFinder());
        for (Element l : Xml.getElements("lib", elem)) {
            repoFound = true;
            MockResourceFinder lib = MockResourceFinder.create(l);
            path.setLib(l.getAttribute("name"), lib);
        }
        if (repoFound) {
            return path;
        }
        Properties props = new Properties();
        Element libs = Xml.getSingleElement("libs", elem, false);
        if (libs != null) {
            String libDef = libs.getTextContent().trim();
            props.setProperty(ConfigProperties.PROP_LIBS, libDef);
        }
        return SearchPath.create(TestDataUtil.getFile(dirName),
                props);
    }

    public MockResource createResource(Element elem) {
        return new MockResource(elem, createResourceFinder(elem));
    }

    static class MockResource extends ConfigResource {
        private final Element elem;
        private final ResourceFinder pc;

        public MockResource(Element elem, ResourceFinder pc) {
            this.elem = elem;
            this.pc = pc;
        }

        @Override
        public Element createElement() {
            return (Element) elem.cloneNode(true);
        }

        @Override
        public ResourceFinder getFinder() {
            return pc;
        }

        @Override
        public String getUri() {
            return "test";
        }

    }
}