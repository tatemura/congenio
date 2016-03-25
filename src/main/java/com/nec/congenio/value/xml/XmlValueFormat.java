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

package com.nec.congenio.value.xml;

import java.io.File;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nec.congenio.ConfigValue;
import com.nec.congenio.ValueFormat;
import com.nec.congenio.xml.Xml;

public class XmlValueFormat implements ValueFormat {

    @Override
    public ConfigValue parse(String data) {
        return create(Xml.parse(data));
    }

    @Override
    public ConfigValue parse(URL url) {
        return create(Xml.parse(url));
    }

    @Override
    public ConfigValue parse(File file) {
        return create(Xml.parse(file));
    }

    @Override
    public ConfigValue parse(InputStream instr) {
        return create(Xml.parse(instr));
    }

    private ConfigValue create(Document doc) {
        return XmlValue.create(doc.getDocumentElement());
    }

    @Override
    public void write(ConfigValue conf, Writer writer, boolean indent) {
        if (conf instanceof XmlValue) {
            Element elem = ((XmlValue) conf).toXml();
            Xml.write(elem, writer, false, indent);
        } else {
            Element elem = conf.toXml(conf.getName());
            Xml.write(elem, writer, false, indent);
        }
    }

    /**
     * Generates a path expression that refers to
     * the given element.
     * @param elem the element for which a path is
     *        generated.
     * @return a path from the root to the given element.
     */
    public static String path(Element elem) {
        String path = "/" + name(elem);
        Node currentNode = elem;
        while ((currentNode = currentNode.getParentNode()) != null) {
            if (currentNode instanceof Element) {
                path = "/" + name((Element) currentNode) + path;
            } else {
                break;
            }
        }
        return path;
    }

    private static String name(Element elem) {
        String name = elem.getAttribute("name");
        if (!name.isEmpty()) {
            return elem.getTagName() + "[" + name + "]";
        } else {
            return elem.getTagName();
        }
    }
}
