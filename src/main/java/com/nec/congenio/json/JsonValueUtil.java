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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;

import com.nec.congenio.ConfigException;

/**
 * Utility to create a JsonValue
 * @author tatemura
 *
 */
public final class JsonValueUtil {

	private JsonValueUtil() {
	}

	public static JsonNumber create(double v) {
		return new JsonNumberImpl(BigDecimal.valueOf(v));
	}

	public static JsonNumber create(long v) {
		return new JsonNumberImpl(BigDecimal.valueOf(v));
	}

	public static JsonNumber create(Number v) {
		if (v instanceof Long || v instanceof Integer) {
			return create(v.longValue());
		} else if (v instanceof BigDecimal) {
			return new JsonNumberImpl((BigDecimal) v);
		} else if (v instanceof BigInteger) {
			return create(((BigInteger) v).longValue());
		} else {
			return create(v.doubleValue());
		}
	}
	public static JsonObject parseObject(File file) {
		try {
			JsonParser p = Json.createParser(new FileInputStream(file));
			return (JsonObject) new JsonValueParser(p).parse();
		} catch (FileNotFoundException e) {
			throw new ConfigException("json file not found", e);
		}
	}
	public static JsonString create(String v) {
		return new JsonStringImpl(v);
	}
	public static JsonNumber number(String v) {
		return create(new BigDecimal(v));
	}
	public static JsonObject object(String v) {
		return (JsonObject) parse(v);
	}
	public static JsonValue parse(String v) {
		return new JsonValueParser(v).parse();
	}
	public static JsonObject emptyObject() {
		return Json.createObjectBuilder().build();
	}
	static class JsonValueParser {
		private final JsonParser p;
		JsonValueParser(JsonParser p) {
			this.p = p;
		}
		JsonValueParser(String text) {
			this.p = Json.createParser(
					new StringReader(text));
		}
		public JsonValue parse() {
			JsonValue v = parseValue(assertEvent());
			assertEnd();
			p.close();
			return v;
		}
		private JsonObject parseObject() {
			JsonObjectBuilder b = Json.createObjectBuilder();
			String key;
			while ((key = assertKeyOrEndObject()) != null) {
				JsonValue value = parseValue();
				b.add(key, value);
			}
			return b.build();
		}
		private JsonValue parseValue() {
			return parseValue(assertEvent());
		}
		private JsonArray parseArray() {
			JsonArrayBuilder b = Json.createArrayBuilder();
			JsonValue v;
			while ((v = assertValueOrEndArray()) != null) {
				b.add(v);
			}
			return b.build();
		}
		private JsonValue parseValue(Event event) {
			switch (event) {
			case START_OBJECT:
				return parseObject();
			case END_OBJECT:
				throw error("unexpected end of object");
			case START_ARRAY:
				return parseArray();
			case END_ARRAY:
				throw error("unexpected end of array");
			case VALUE_NUMBER:
				return JsonValueUtil.create(p.getBigDecimal());
			case VALUE_STRING:
				return JsonValueUtil.create(p.getString());
			case VALUE_TRUE:
				return JsonValue.TRUE;
			case VALUE_FALSE:
				return JsonValue.FALSE;
			case KEY_NAME:
				throw error("unexpected key name");
			default:
				throw error("unknown event");
			}
		}
		private String assertKeyOrEndObject() {
			Event e = assertEvent();
			if (e == Event.KEY_NAME) {
				return p.getString();
			} else if (e == Event.END_OBJECT) {
				return null;
			} else {
				throw error("key expected");
			}
		}
		private JsonValue assertValueOrEndArray() {
			Event e = assertEvent();
			if (e == Event.END_ARRAY) {
				return null;
			} else {
				return parseValue(e);
			}
		}
		private void assertEnd() {
			if (p.hasNext()) {
				throw error("unexpected text after expression");
			}
		}
		private Event assertEvent() {
			if (p.hasNext()) {
				return p.next();
			} else {
				throw error("unexpected end of text");
			}
		}
		private JsonParsingException error(String msg) {
			return new JsonParsingException(msg,
					p.getLocation());
		}
	}
	static class JsonNumberImpl implements JsonNumber {
		private final BigDecimal value;
		public JsonNumberImpl(BigDecimal value) {
			this.value = value;
		}
		@Override
		public ValueType getValueType() {
			return ValueType.NUMBER;
		}

		@Override
		public boolean isIntegral() {
			return value.scale() == 0;
		}

		@Override
		public int intValue() {
			return value.intValue();
		}

		@Override
		public int intValueExact() {
			return value.intValueExact();
		}

		@Override
		public long longValue() {
			return value.longValue();
		}

		@Override
		public long longValueExact() {
			return value.longValueExact();
		}

		@Override
		public BigInteger bigIntegerValue() {
			return value.toBigInteger();
		}

		@Override
		public BigInteger bigIntegerValueExact() {
			return value.toBigIntegerExact();
		}

		@Override
		public double doubleValue() {
			return value.doubleValue();
		}

		@Override
		public BigDecimal bigDecimalValue() {
			return value;
		}
		@Override
		public String toString() {
			return value.toString();
		}
		@Override
		public int hashCode() {
			return value.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof JsonNumber) {
				JsonNumber v = (JsonNumber) obj;
				return value.equals(v.bigDecimalValue());
			}
			return false;
		}
	}
	static class JsonStringImpl implements JsonString {
		private final String value;
		JsonStringImpl(String value) {
			this.value = value;
		}
		@Override
		public ValueType getValueType() {
			return ValueType.STRING;
		}

		@Override
		public String getString() {
			return value;
		}

		@Override
		public CharSequence getChars() {
			return value;
		}
		@Override
		public int hashCode() {
			return value.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof JsonString) {
				JsonString s = (JsonString) obj;
				return value.equals(s.getString());
			}
			return false;
		}
		@Override
		public String toString() {
			if (value == null) {
				return "\"\"";
			}
			StringBuilder sb = new StringBuilder();
			sb.append('"');
			for (int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				switch (c)  {
				case '\\':
				case '"':
					sb.append('\\').append(c);
					break;
				case '\t':
					sb.append('\\').append('t');
					break;
				case '\n':
					sb.append('\\').append('n');
					break;
				case '\r':
					sb.append('\\').append('r');
					break;
				case '\b':
					sb.append('\\').append('b');
					break;
				case '\f':
					sb.append('\\').append('f');
					break;
				default:
					if (c < ' ') {
						String hexStr = "000" + Integer.toHexString(c);
						sb.append("\\u").append(hexStr.substring(
								hexStr.length() - UNICODE_HEX_LEN));
					} else {
						sb.append(c);
					}
				}
			}
			sb.append('"');
			return sb.toString();
		}
		private static final int UNICODE_HEX_LEN = 4;
	}
	public static JsonObject union(JsonObject... values) {
		JsonObjectBuilder b = Json.createObjectBuilder();
		for (JsonObject v : values) {
			for (Map.Entry<String, JsonValue> e : v.entrySet()) {
				b.add(e.getKey(), e.getValue());
			}
		}
		return b.build();
	}

	/**
	 * Converts a JSON value to an indented
	 * text representation.
	 * @param json
	 * @return an indented text.
	 */
	public static String toString(JsonValue json) {
		StringBuilder sb = new StringBuilder();
		toString(json, sb, 0);
		return sb.toString();
	}
	private static void toString(JsonObject json, StringBuilder sb, int indent) {
		sb.append("{");
		boolean contd = false;
		for (Map.Entry<String, JsonValue> e : json.entrySet()) {
			if (contd) {
				sb.append(",\n");
			} else {
				contd = true;
				sb.append("\n");
			}
			indent(indent + 1, sb);
			sb.append("\"").append(e.getKey()).append("\": ");
			toString(e.getValue(), sb, indent + 1);
		}
		if (contd) {
			sb.append("\n");
			indent(indent, sb);
		}
		sb.append("}");
	}
	private static void toString(JsonArray json, StringBuilder sb, int indent) {
		sb.append("[");
		boolean contd = false;
		for (JsonValue v : json) {
			if (contd) {
				sb.append(",\n");
			} else {
				contd = true;
				sb.append("\n");
			}
			indent(indent + 1, sb);
			toString(v, sb, indent + 1);
		}
		sb.append("]");
	}
	private static void toString(JsonValue json, StringBuilder sb, int indent) {
		if (json.getValueType() == ValueType.OBJECT) {
			toString((JsonObject) json, sb, indent);
		} else if (json.getValueType() == ValueType.ARRAY) {
			toString((JsonArray) json, sb, indent);
		} else {
			sb.append(json.toString());
		}
	}
	private static void indent(int indent, StringBuilder sb) {
		for (int i = 0; i < indent; i++) {
			sb.append(INDENT);
		}
	}
	private static final String INDENT = "  ";
}
