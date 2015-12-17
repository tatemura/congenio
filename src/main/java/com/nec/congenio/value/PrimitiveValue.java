/*******************************************************************************
 *   Copyright 2015 Junichi Tatemura
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package com.nec.congenio.value;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.json.JsonValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.Type;
import com.nec.congenio.json.JsonValueUtil;
import com.nec.congenio.xml.XML;

public abstract class PrimitiveValue implements ConfigValue {

	public static final PrimitiveValue NULL = new NullValue();
	public static final PrimitiveValue TRUE = new BooleanValue(true);
	public static final PrimitiveValue FALSE = new BooleanValue(false);

	public static StringValue valueOf(String value) {
		return new StringValue(value);
	}
	public static PrimitiveValue valueOf(boolean value) {
		return (value ? TRUE : FALSE);
	}
	public static NumberValue valueOf(int value) {
		return new NumberValue(BigDecimal.valueOf(value));
	}
	public static NumberValue valueOf(long value) {
		return new NumberValue(BigDecimal.valueOf(value));
	}
	public static NumberValue valueOf(double value) {
		return new NumberValue(BigDecimal.valueOf(value));
	}
	public static NumberValue valueOf(BigDecimal value) {
		return new NumberValue(value);
	}
	public static NumberValue number(String value) {
		return new NumberValue(new BigDecimal(value));
	}
	public static PrimitiveValue number(PrimitiveValue v) {
		if (v instanceof NumberValue) {
			return v;
		} else if (v instanceof NullValue) {
			return v;
		} else {
			String str = v.stringValue();
			if (str == null || str.trim().isEmpty()) {
				return NULL;
			}
			return number(str);
		}
	}

	public static PrimitiveValue valueOf(Type type, String value) {
		if (value == null) {
			return NULL;
		}
		if (type == Type.NUMBER) {
			return number(value);
		} else if (type == Type.BOOL) {
			return Boolean.parseBoolean(value) ? TRUE : FALSE;
		} else {
			return valueOf(value);
		}
	}
	@Override
	public abstract String stringValue();

	@Override
	public abstract BigDecimal numberValue();

	@Override
	public abstract int intValue(int defaultValue);

	public PrimitiveValue cast(Type type) {
		if (type == getType()) {
			return this;
		} else {
			return valueOf(type, stringValue());
		}
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public int getInt(String name) {
		throw new ConfigException("value not found:" + name);
	}

	@Override
	public int getInt(String name, int defaultValue) {
		return defaultValue;
	}

	@Override
	public Integer findInt(String name) {
		return null;
	}

	@Override
	public long getLong(String name) {
		throw new ConfigException("value not found:" + name);
	}

	@Override
	public long getLong(String name, long defaultValue) {
		return defaultValue;
	}

	@Override
	public double getDouble(String name) {
		throw new ConfigException("value not found:" + name);
	}

	@Override
	public double getDouble(String name, double defaultValue) {
		return defaultValue;
	}

	@Override
	public boolean getBoolean(String name) {
		return false;
	}

	@Override
	public boolean getBoolean(String name, boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public String get(String name) {
		throw new ConfigException("value not found:" + name);
	}

	@Override
	public String get(String name, String defaultValue) {
		return defaultValue;
	}

	@Override
	public String find(String name) {
		return null;
	}

	@Override
	public List<String> getList(String name) {
		return Arrays.asList();
	}

	@Override
	public ConfigValue getValue(String name) {
		throw new ConfigException("value not found:" + name);
	}

	@Override
	public ConfigValue findValue(String name) {
		return null;
	}

	@Override
	public boolean hasValue(String name) {
		return false;
	}

	@Override
	public List<ConfigValue> getValueList(String name) {
		return Arrays.asList();
	}

	@Override
	public Map<String, ConfigValue> getValueMap(String name) {
		return new HashMap<String, ConfigValue>();
	}

	@Override
	public <T> T findObject(String name, Class<T> objectClass) {
		return null;
	}

	@Override
	public <T> T getObject(String name, Class<T> objectClass) {
		throw new ConfigException("value not found:" + name);
	}

	@Override
	public <T> T getObject(String name, Class<T> objectClass, T defaultObject) {
		return defaultObject;
	}

	@Override
	public Map<String, ConfigValue> toValueMap() {
		return new HashMap<String, ConfigValue>();
	}

	@Override
	public List<ConfigValue> toValueList() {
		return Arrays.asList();
	}

	@Override
	public JsonValue getJson(String name) {
		return JsonValue.NULL;
	}

	@Override
	public JsonValue findJson(String name) {
		return null;
	}

	@Override
	public Properties toProperties() {
		return new Properties();
	}

	@Override
	public Properties getProperties(String name) {
		return new Properties();
	}

	@Override
	public Element toXML(Document doc, String name) {
    	Element e = doc.createElement(name);
    	String content = stringValue();
    	if (content != null) {
    		e.setTextContent(content);
    	}
    	return e;
	}
	@Override
	public Element toXML(String name) {
		return toXML(XML.createDocument(), name);
	}

	@Override
	public Element findXML(String name) {
		return null;
	}

	public static class StringValue extends PrimitiveValue {
		private final String value;
		public StringValue(String value) {
			this.value = value;
		}
		@Override
		public String stringValue() {
			return value;
		}

		@Override
		public BigDecimal numberValue() {
			return new BigDecimal(value);
		}

		@Override
		public boolean booleanValue() {
			return Boolean.parseBoolean(value);
		}
		@Override
		public int intValue(int defaultValue) {
			return Integer.parseInt(value);
		}
		@Override
		public JsonValue toJson() {
			return JsonValueUtil.create(value);
		}
		@Override
		public Type getType() {
			return Type.STRING;
		}

	}
	public static class NumberValue extends PrimitiveValue {
		private final BigDecimal value;
		public NumberValue(BigDecimal value) {
			this.value = value;
		}
		@Override
		public String stringValue() {
			try {
				return Long.toString(value.longValueExact());
			} catch (ArithmeticException e1) {
				return value.toString();
			}
		}

		@Override
		public BigDecimal numberValue() {
			return value;
		}

		@Override
		public boolean booleanValue() {
			return false;
		}

		@Override
		public int intValue(int defaultValue) {
			return value.intValue();
		}
		@Override
		public JsonValue toJson() {
			return JsonValueUtil.create(value);
		}
		@Override
		public Type getType() {
			return Type.NUMBER;
		}
	}
	public static class BooleanValue extends PrimitiveValue {
		private final boolean value;
		public BooleanValue(boolean value) {
			this.value = value;
		}
		@Override
		public boolean booleanValue() {
			return value;
		}

		@Override
		public JsonValue toJson() {
			return (value ? JsonValue.TRUE : JsonValue.FALSE);
		}

		@Override
		public String stringValue() {
			return Boolean.toString(value);
		}

		@Override
		public BigDecimal numberValue() {
			return null;
		}

		@Override
		public int intValue(int defaultValue) {
			return defaultValue;
		}
		@Override
		public Type getType() {
			return Type.BOOL;
		}
	}
	public static class NullValue extends PrimitiveValue {

		@Override
		public String stringValue() {
			return null;
		}

		@Override
		public BigDecimal numberValue() {
			return null;
		}

		@Override
		public boolean booleanValue() {
			return false;
		}

		@Override
		public int intValue(int defaultValue) {
			return defaultValue;
		}
		@Override
		public JsonValue toJson() {
			return JsonValue.NULL;
		}
		@Override
		public Type getType() {
			return Type.NULL;
		}
		@Override
		public PrimitiveValue cast(Type type) {
			return this;
		}
	}
}
