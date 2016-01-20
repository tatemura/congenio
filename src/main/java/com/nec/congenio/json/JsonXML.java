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
package com.nec.congenio.json;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.nec.congenio.ConfigException;
import com.nec.congenio.Type;
import com.nec.congenio.impl.ExpXML;
import com.nec.congenio.value.PrimitiveValue;
import com.nec.congenio.value.xml.Attrs;
import com.nec.congenio.value.xml.XMLValue;
import com.nec.congenio.xml.XML;

public final class JsonXML {

	public static final String TAG_ARRAY_ELEMENT = "e";
	public static final String TAG_OBJECT_NAME = "param";

	private JsonXML() {
	}

	public static Element toXML(JsonObject value) {
		return toXML(TAG_OBJECT_NAME, value);
	}
	public static Element toXML(String name, JsonObject value) {
		return toXML(XML.createDocument(), name, value);
	}
	public static Element toXML(Document doc, String name, JsonObject value) {
		Element e = XMLValue.createElement(doc, name, Type.OBJECT);
		for (Map.Entry<String, JsonValue> entry : value.entrySet()) {
			String key = entry.getKey();
			JsonValue v = entry.getValue();
			e.appendChild(createElement(doc, key, v));
		}
		return e;
	}
	private static Element toXML(Document doc, String name, JsonString value) {
		return XMLValue.createElement(doc, name, value.getString());
	}
	private static Element toXML(Document doc, String name, JsonArray value) {
		Element e = XMLValue.createElement(doc, name, Type.ARRAY);
		for (JsonValue v : value) {
			e.appendChild(createElement(doc, TAG_ARRAY_ELEMENT, v));
		}
		return e;
	}
	private static Element createElement(Document doc, String name, JsonValue v) {
		ValueType type = v.getValueType();
		switch (type) {
		case OBJECT:
			return toXML(doc, name, (JsonObject) v);
		case ARRAY:
			return toXML(doc, name, (JsonArray) v);
		case NUMBER:
			return XMLValue.createElement(doc, name, Type.NUMBER, v);
		case STRING:
			return toXML(doc, name, (JsonString) v);
		case NULL:
			return XMLValue.createElement(doc, name);
		case TRUE:
			return XMLValue.createElement(doc, name, "true");
		case FALSE:
			return XMLValue.createElement(doc, name, "false");
		default:
			throw new ConfigException("unsupported Json type:" + type);
		}

	}


	public static JsonValue toJson(Element e) {
		Type type = XMLValue.findType(e);
		/**
		 * exp must be processed since this is
		 * also used in exp="jsondump".
		 */
		ExpXML exp = ExpXML.findExp(e);
		if (exp != null) {
			return toJsonValue(type, exp.value());
		}
		List<Element> elements = XML.getElements(e);
		Map<String, String> attrs = Attrs.userAttrs(e);
		if (!elements.isEmpty() || !attrs.isEmpty()) {
			if (type == null) {
				type = guessType(elements);
			}
			if (Type.ARRAY.equals(type)) {
				return toJsonArray(attrs, elements);
			} else {
				return toJsonObject(attrs, elements);
			}
		}
		return toJsonValue(type, XMLValue.toPrimitive(e));
	}

	private static JsonValue toJsonObject(Map<String, String> attrs, List<Element> elements) {
		JsonObjectBuilder b = Json.createObjectBuilder();
		for (Map.Entry<String, String> en : attrs.entrySet()) {
			String attr = en.getKey();
			b.add(attr, JsonValueUtil.create(en.getValue()));
		}
		for (Element c : elements) {
			String key = XMLValue.getName(c);
			b.add(key, toJson(c));
		}
		return b.build();
	}
	private static JsonValue toJsonArray(Map<String, String> attrs, List<Element> elements) {
		JsonArrayBuilder b = Json.createArrayBuilder();
		for (String value : attrs.values()) {
			b.add(JsonValueUtil.create(value));
		}
		for (Element c : elements) {
			b.add(toJson(c));
		}
		return b.build();
	}
	private static Type guessType(List<Element> elements) {
		Set<String> names = new HashSet<String>();
		for (Element e : elements) {
			names.add(XMLValue.getName(e));
		}
		if (names.size() == 1 && elements.size() > 1) {
			return Type.ARRAY;
		} else {
			return Type.OBJECT;
		}
	}
	private static JsonValue toJsonValue(@Nullable Type type, PrimitiveValue v) {
		if (type == null) {
			return v.toJson();
		}
		String value = v.stringValue();
		if (value == null || value.isEmpty()) {
			if (Type.OBJECT.equals(type)) {
				return Json.createObjectBuilder().build();
			} else if (Type.ARRAY.equals(type)) {
				return Json.createArrayBuilder().build();
			} else {
				return JsonValue.NULL;
			}
		}
		if (Type.OBJECT.equals(type)) {
			return JsonValueUtil.parse(value);
		} else if (Type.ARRAY.equals(type)) {
			return JsonValueUtil.parse(value);
		} else {
			return v.cast(type).toJson();
		}
	}


}
