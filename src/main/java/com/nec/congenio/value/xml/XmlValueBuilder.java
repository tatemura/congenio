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

import java.util.Properties;

import javax.json.JsonValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.nec.congenio.ConfigValue;
import com.nec.congenio.Type;
import com.nec.congenio.ValueBuilder;
import com.nec.congenio.json.JsonValueUtil;
import com.nec.congenio.json.JsonXml;
import com.nec.congenio.value.ValueUtil;
import com.nec.congenio.xml.Xml;

public class XmlValueBuilder implements ValueBuilder {

    /**
     * Creates a builder that generates a config value using
     * XML as internal data.
     * @param name the name of the root XML element.
     * @return a builder.
     */
    public static XmlValueBuilder create(String name) {
        Document doc = Xml.createDocument();
        Element root = XmlValue.createElement(doc, name);
        doc.appendChild(root);
        return new XmlValueBuilder(root);
    }

    /**
     * Creates a builder that generates a config value using
     * XML as internal data and using a given config value as
     * a base.
     * @param name the name of the root XML element.
     * @param base the base value.
     * @return a builder.
     */
    public static XmlValueBuilder create(String name, ConfigValue base) {
        Document doc = Xml.createDocument();
        Element root = base.toXml(doc, name);
        doc.appendChild(root);
        return new XmlValueBuilder(root);
    }

    private final Document doc;
    private final Element root;

    public XmlValueBuilder(Element root) {
        this.doc = root.getOwnerDocument();
        this.root = root;
    }

    @Override
    public ValueBuilder add(String name, String value) {
        setElement(name, XmlValue.createElement(doc, name, value));
        return this;
    }

    @Override
    public ValueBuilder add(String name, Number value) {
        setElement(name, XmlValue.createElement(
                doc, name, Type.NUMBER, value));
        return this;
    }

    @Override
    public ValueBuilder add(String name, boolean value) {
        setElement(name, XmlValue.createElement(
                doc, name, Type.BOOL, value));
        return this;
    }

    @Override
    public ValueBuilder add(String name, ValueBuilder value) {
        ConfigValue val = value.build();
        setElement(name, val.toXml(doc, name));
        return this;
    }

    @Override
    public ValueBuilder add(String name, ConfigValue value) {
        setElement(name, value.toXml(doc, name));
        return this;
    }

    @Override
    public ValueBuilder add(String name, ConfigValue[] value) {
        Element elem = XmlValue.createElement(doc, name, Type.ARRAY);
        for (ConfigValue v : value) {
            elem.appendChild(v.toXml(doc, "v"));
        }
        setElement(name, elem);
        return this;
    }

    @Override
    public ValueBuilder add(String name, Properties props) {
        Element elem = XmlValue.createElement(doc, name);
        for (Object k : props.keySet()) {
            Element prop = XmlValue.createElement(
                    doc, k.toString(),
                    props.getProperty(k.toString()));
            elem.appendChild(prop);
        }
        setElement(name, elem);
        return this;
    }

    @Override
    public ValueBuilder add(String name, Object value) {
        ValueUtil.setObject(this, name, value);
        return this;
    }

    @Override
    public String toXmlString() {
        return Xml.toString(root);
    }

    @Override
    public String toJsonString() {
        return JsonValueUtil.toString(toJson());
    }

    public JsonValue toJson() {
        return JsonXml.toJson(root);
    }

    @Override
    public XmlValue build() {
        return XmlValue.create(root);
    }

    private void setElement(String name, Element newElement) {
        if (root.hasAttribute(name)) {
            root.removeAttribute(name);
        }
        for (Element e : Xml.getElements(name, root)) {
            root.removeChild(e);
        }
        root.appendChild(newElement);
    }

}
