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
import com.nec.congenio.json.JsonXML;
import com.nec.congenio.value.ValueUtil;
import com.nec.congenio.xml.XML;

public class XMLValueBuilder implements ValueBuilder {

	public static XMLValueBuilder create(String name) {
		Document doc = XML.createDocument();
		Element root = XMLValue.createElement(doc, name);
		doc.appendChild(root);
		return new XMLValueBuilder(root);
	}

	public static XMLValueBuilder create(String name, ConfigValue base) {
		Document doc = XML.createDocument();
		Element root = base.toXML(doc, name);
		doc.appendChild(root);
		return new XMLValueBuilder(root);
	}

	private final Document doc;
	private final Element root;

	public XMLValueBuilder(Element root) {
		this.doc = root.getOwnerDocument();
		this.root = root;
	}


	@Override
	public ValueBuilder add(String name, String value) {
    	Element e = XMLValue.createElement(doc, name, value);
		setElement(name, e);
		return this;
	}

	@Override
	public ValueBuilder add(String name, Number value) {
    	Element e = XMLValue.createElement(doc, name, Type.NUMBER, value);
		setElement(name, e);
		return this;
	}
	@Override
	public ValueBuilder add(String name, boolean value) {
    	Element e = XMLValue.createElement(doc, name, Type.BOOL, value);
		setElement(name, e);
		return this;
	}

	@Override
	public ValueBuilder add(String name, ValueBuilder value) {
		ConfigValue v = value.build();
		setElement(name, v.toXML(doc, name));
		return this;
	}

	@Override
	public ValueBuilder add(String name, ConfigValue value) {
		setElement(name, value.toXML(doc, name));
		return this;
	}
	@Override
	public ValueBuilder add(String name, ConfigValue[] value) {
		Element e = XMLValue.createElement(doc, name, Type.ARRAY);
		for (ConfigValue v : value) {
			e.appendChild(v.toXML(doc, "v"));
		}
		setElement(name, e);
		return this;
	}
	@Override
	public ValueBuilder add(String name, Properties props) {
		Element e = XMLValue.createElement(doc, name);
		for (Object k : props.keySet()) {
			Element p = XMLValue.createElement(doc,
					k.toString(), props.getProperty(k.toString()));
			e.appendChild(p);
		}
		setElement(name, e);
		return this;
	}
	@Override
	public ValueBuilder add(String name, Object value) {
		ValueUtil.setObject(this, name, value);
		return this;
	}
	@Override
	public String toXMLString() {
		return XML.toString(root);
	}

	@Override
	public String toJsonString() {
		return JsonValueUtil.toString(toJson());
	}

	public JsonValue toJson() {
		return JsonXML.toJson(root);
	}

	@Override
	public ConfigValue build() {
		return XMLValue.create(root);
	}

	private void setElement(String name, Element newElement) {
		if (root.hasAttribute(name)) {
			root.removeAttribute(name);
		}
		for (Element e : XML.getElements(name, root)) {
			root.removeChild(e);
		}
    	root.appendChild(newElement);
	}

}
