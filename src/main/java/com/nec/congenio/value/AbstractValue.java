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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.json.JsonValue;

import org.w3c.dom.Element;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.xml.Xml;

public abstract class AbstractValue implements ConfigValue {

    @Override
    public abstract String getName();

    @Override
    public abstract ConfigValue findValue(String name);

    @Override
    public abstract boolean hasValue(String name);

    @Override
    public abstract Map<String, ConfigValue> toValueMap();

    @Override
    public abstract List<ConfigValue> toValueList();

    @Override
    public abstract String stringValue();

    @Override
    public abstract BigDecimal numberValue();

    @Override
    public abstract boolean booleanValue();

    @Override
    public Properties toProperties() {
        Properties prop = new Properties();
        for (Map.Entry<String, ConfigValue> e : toValueMap().entrySet()) {
            prop.setProperty(e.getKey(), e.getValue().stringValue());
        }
        return prop;
    }

    @Override
    public Element toXml(String name) {
        return toXml(Xml.createDocument(), name);
    }

    @Override
    public Element findXml(String name) {
        ConfigValue val = findValue(name);
        if (val != null) {
            return val.toXml(name);
        }
        return null;
    }

    @Override
    public abstract JsonValue toJson();

    @Override
    public int getInt(String name) {
        Integer val = findInt(name);
        if (val == null) {
            throw new ConfigException("value not found:" + name);
        }
        return val;
    }

    @Override
    public int getInt(String name, int defaultValue) {
        Integer val = findInt(name);
        if (val == null) {
            return defaultValue;
        }
        return val;
    }

    private BigDecimal findNumber(String name) {
        ConfigValue val = findValue(name);
        if (val != null) {
            return val.numberValue();
        }
        return null;

    }

    private BigDecimal getNumber(String name) {
        ConfigValue val = findValue(name);
        if (val != null) {
            BigDecimal num = val.numberValue();
            if (num != null) {
                return num;
            }
        }
        throw new ConfigException("value not found:" + name);
    }

    @Override
    public Integer findInt(String name) {
        BigDecimal num = findNumber(name);
        if (num != null) {
            return num.intValue();
        }
        return null;
    }

    @Override
    public long getLong(String name) {
        return getNumber(name).longValue();
    }

    @Override
    public long getLong(String name, long defaultValue) {
        BigDecimal num = findNumber(name);
        if (num != null) {
            return num.longValue();
        } else {
            return defaultValue;
        }
    }

    @Override
    public double getDouble(String name) {
        return getNumber(name).doubleValue();
    }

    @Override
    public double getDouble(String name, double defaultValue) {
        BigDecimal num = findNumber(name);
        if (num != null) {
            return num.doubleValue();
        } else {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        ConfigValue val = findValue(name);
        if (val == null) {
            return defaultValue;
        }
        return val.booleanValue();
    }

    @Override
    public String get(String name) {
        ConfigValue val = getValue(name);
        String value = val.stringValue();
        if (value == null) {
            return "";
        }
        return value;
    }

    @Override
    public String get(String name, String defaultValue) {
        ConfigValue val = findValue(name);
        if (val == null) {
            return defaultValue;
        }
        String value = val.stringValue();
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public String find(String name) {
        ConfigValue val = findValue(name);
        if (val != null) {
            return val.stringValue();
        }
        return null;
    }

    @Override
    public List<String> getList(String name) {
        List<String> list = new ArrayList<String>();
        for (ConfigValue v : getValueList(name)) {
            String str = v.stringValue();
            if (str != null) {
                list.add(str);
            }
        }
        return list;
    }

    @Override
    public ConfigValue getValue(String name) {
        ConfigValue val = findValue(name);
        if (val == null) {
            throw new ConfigException("value not found:" + name);
        }
        return val;
    }

    @Override
    public List<ConfigValue> getValueList(String name) {
        ConfigValue val = findValue(name);
        if (val != null) {
            return val.toValueList();
        } else {
            return Arrays.asList();
        }
    }

    @Override
    public Map<String, ConfigValue> getValueMap(String name) {
        ConfigValue val = findValue(name);
        if (val != null) {
            return val.toValueMap();
        }
        return new HashMap<String, ConfigValue>();
    }

    @Override
    public <T> T getObject(String name, Class<T> objectClass) {
        T obj = findObject(name, objectClass);
        if (obj == null) {
            throw new ConfigException("value not found: " + name);
        }
        return obj;
    }

    @Override
    public <T> T getObject(String name, Class<T> objectClass, T defaultObject) {
        T obj = findObject(name, objectClass);
        if (obj == null) {
            return defaultObject;
        }
        return obj;
    }

    @Override
    public <T> T findObject(String name, Class<T> objectClass) {
        ConfigValue value = findValue(name);
        if (value != null) {
            return ValueUtil.toObject(value, objectClass);
        }
        return null;
    }

    @Override
    public <T> T toObject(Class<T> objectClass) {
        return ValueUtil.toObject(this, objectClass);
    }

    @Override
    public <T> Map<String, T> toObjectMap(Class<T> valueClass) {
        return ValueUtil.toMap(this, valueClass);
    }

    @Override
    public int intValue(int defaultValue) {
        BigDecimal val = numberValue();
        if (val == null) {
            return defaultValue;
        }
        return val.intValue();
    }

    @Override
    public Properties getProperties(String name) {
        ConfigValue val = findValue(name);
        if (val == null) {
            return new Properties();
        }
        return val.toProperties();
    }

    @Override
    public JsonValue getJson(String name) {
        ConfigValue val = findValue(name);
        if (val == null) {
            return JsonValue.NULL;
        }
        return val.toJson();
    }

    @Override
    public JsonValue findJson(String name) {
        ConfigValue val = findValue(name);
        if (val == null) {
            return null;
        }
        return val.toJson();
    }

}
