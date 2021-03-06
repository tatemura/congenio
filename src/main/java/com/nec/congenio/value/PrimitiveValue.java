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

package com.nec.congenio.value;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.json.JsonValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.Type;
import com.nec.congenio.json.JsonValueUtil;
import com.nec.congenio.xml.Xml;

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

    /**
     * Creates a primitive value from given type and value.
     * @param type the type of the primitive value.
     * @param value the string representation of the value.
     * @return a created primitive value. A NULL primitive
     *         value when the given value is null.
     */
    public static PrimitiveValue valueOf(Type type, @Nullable  String value) {
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

    /**
     * Creates a number value from a string representation.
     * @param value a string representation of the value.
     * @return the created value.
     * @throws NumberFormatException if the string value is
     *         not a valid number.
     */
    public static NumberValue number(String value) {
        try {
            return new NumberValue(new BigDecimal(value));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("not a number: '" + value + "'");
        }
    }

    /**
     * Converts a given primitive value to a number value.
     * @param val the value to be converted
     * @return a converted number value. A NULL primitive
     *         value if the given value is either NULL or
     *         an empty string.
     * @throws NumberFormatException if the given primitive
     *         value does not represent a valid number.
     */
    public static PrimitiveValue number(PrimitiveValue val) {
        if (val instanceof NumberValue) {
            return val;
        } else if (val instanceof NullValue) {
            return val;
        } else {
            String str = val.stringValue();
            if (str == null || str.trim().isEmpty()) {
                return NULL;
            }
            return number(str);
        }
    }

    protected abstract Object rawValue();

    @Override
    public abstract String stringValue();

    @Override
    public abstract BigDecimal numberValue();

    @Override
    public abstract int intValue(int defaultValue);

    /**
     * converts the type of the value.
     * @param type the target type.
     * @return the value after type casting.
     */
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
    public <T> T toObject(Class<T> objectClass) {
        return ValueUtil.toObject(this, objectClass);
    }

    @Override
    public <T> Map<String, T> toObjectMap(Class<T> valueClass) {
        return new HashMap<String, T>();
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
    public Element toXml(Document doc, String name) {
        Element elem = doc.createElement(name);
        String content = stringValue();
        if (content != null) {
            elem.setTextContent(content);
        }
        return elem;
    }

    @Override
    public Element toXml(String name) {
        return toXml(Xml.createDocument(), name);
    }

    @Override
    public Element findXml(String name) {
        return null;
    }

    public static class StringValue extends PrimitiveValue {
        private final String value;

        public StringValue(String value) {
            this.value = value;
        }

        @Override
        protected Object rawValue() {
            return value;
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
        protected Object rawValue() {
            return value;
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
        protected Object rawValue() {
            return value;
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
        protected Object rawValue() {
            return null;
        }

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
