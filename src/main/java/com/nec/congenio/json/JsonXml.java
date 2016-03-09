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
import com.nec.congenio.impl.ExpXml;
import com.nec.congenio.value.PrimitiveValue;
import com.nec.congenio.value.xml.Attrs;
import com.nec.congenio.value.xml.XmlValue;
import com.nec.congenio.xml.Xml;

public final class JsonXml {

    public static final String TAG_ARRAY_ELEMENT = "e";
    public static final String TAG_OBJECT_NAME = "param";

    private JsonXml() {
    }

    public static Element toXml(JsonObject value) {
        return toXml(TAG_OBJECT_NAME, value);
    }

    public static Element toXml(String name, JsonObject value) {
        return toXml(Xml.createDocument(), name, value);
    }

    /**
     * Converts a JSON object to an XML element
     * @param doc the document of the generated element.
     * @param name the tag name of the generated element.
     * @param value the JSON object to be converted.
     * @return the generated element.
     */
    public static Element toXml(Document doc, String name, JsonObject value) {
        Element elem = XmlValue.createElement(doc, name, Type.OBJECT);
        for (Map.Entry<String, JsonValue> entry : value.entrySet()) {
            elem.appendChild(createElement(doc, entry.getKey(), entry.getValue()));
        }
        return elem;
    }

    private static Element toXml(Document doc, String name, JsonString value) {
        return XmlValue.createElement(doc, name, value.getString());
    }

    private static Element toXml(Document doc, String name, JsonArray value) {
        Element elem = XmlValue.createElement(doc, name, Type.ARRAY);
        for (JsonValue v : value) {
            elem.appendChild(createElement(doc, TAG_ARRAY_ELEMENT, v));
        }
        return elem;
    }

    private static Element createElement(Document doc, String name, JsonValue value) {
        ValueType type = value.getValueType();
        switch (type) {
        case OBJECT:
            return toXml(doc, name, (JsonObject) value);
        case ARRAY:
            return toXml(doc, name, (JsonArray) value);
        case NUMBER:
            return XmlValue.createElement(doc, name, Type.NUMBER, value);
        case STRING:
            return toXml(doc, name, (JsonString) value);
        case NULL:
            return XmlValue.createElement(doc, name);
        case TRUE:
            return XmlValue.createElement(doc, name, "true");
        case FALSE:
            return XmlValue.createElement(doc, name, "false");
        default:
            throw new ConfigException("unsupported Json type:" + type);
        }

    }

    /**
     * Converts an XML element to JSON value.
     * @param elem the element to be converted.
     * @return the converted JSON value.
     */
    public static JsonValue toJson(Element elem) {
        Type type = XmlValue.findType(elem);
        /**
         * exp must be processed since this is also used in exp="jsondump".
         */
        ExpXml exp = ExpXml.findExp(elem);
        if (exp != null) {
            PrimitiveValue val = exp.value();
            if (type != null && XmlValue.isPrimitiveType(type)) {
                return val.cast(type).toJson();
            } else {
                return val.toJson();
            }
        }
        List<Element> elements = Xml.getElements(elem);
        Map<String, String> attrs = Attrs.userAttrs(elem);
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
        return toJsonValue(type, XmlValue.primitiveValueOf(elem));
    }

    private static JsonValue toJsonObject(
            Map<String, String> attrs, List<Element> elements) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (Map.Entry<String, String> en : attrs.entrySet()) {
            String attr = en.getKey();
            builder.add(attr, JsonValueUtil.create(en.getValue()));
        }
        for (Element c : elements) {
            String key = XmlValue.nameOf(c);
            builder.add(key, toJson(c));
        }
        return builder.build();
    }

    private static JsonValue toJsonArray(Map<String, String> attrs, List<Element> elements) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (String value : attrs.values()) {
            builder.add(JsonValueUtil.create(value));
        }
        for (Element c : elements) {
            builder.add(toJson(c));
        }
        return builder.build();
    }

    private static Type guessType(List<Element> elements) {
        Set<String> names = new HashSet<String>();
        for (Element e : elements) {
            names.add(XmlValue.nameOf(e));
        }
        if (names.size() == 1 && elements.size() > 1) {
            return Type.ARRAY;
        } else {
            return Type.OBJECT;
        }
    }

    private static JsonValue toJsonValue(@Nullable Type type, PrimitiveValue value) {
        if (type == null) {
            return value.toJson();
        }
        String strVal = value.stringValue();
        if (strVal == null || strVal.isEmpty()) {
            if (Type.OBJECT.equals(type)) {
                return Json.createObjectBuilder().build();
            } else if (Type.ARRAY.equals(type)) {
                return Json.createArrayBuilder().build();
            } else {
                return JsonValue.NULL;
            }
        }
        if (Type.OBJECT.equals(type)) {
            return JsonValueUtil.parse(strVal);
        } else if (Type.ARRAY.equals(type)) {
            return JsonValueUtil.parse(strVal);
        } else {
            return value.cast(type).toJson();
        }
    }

}
