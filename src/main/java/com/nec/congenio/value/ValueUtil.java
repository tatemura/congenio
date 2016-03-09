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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.json.JsonValue;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.ValueBuilder;
import com.nec.congenio.Values;
import com.nec.congenio.annotation.MapOf;

public final class ValueUtil {

    private ValueUtil() {
    }

    /**
     * Converts a config value to an object.
     * @param value the value to be converted.
     * @param cls the class of the object.
     * @return a converted object.
     * @throws ConfigException when an object instantiation fails.
     */
    public static <T> T toObject(ConfigValue value, Class<T> cls) {
        if (cls.isArray()) {
            return toObjArray(value, cls);
        }
        T obj = toPrimitive(value, cls);
        if (obj != null) {
            return obj;
        }
        obj = toMapObj(value, cls);
        if (obj != null) {
            return obj;
        }
        try {
            return cls.getConstructor(ConfigValue.class).newInstance(value);
        } catch (NoSuchMethodException ex) {
            return toObjBySetters(value, cls);
        } catch (SecurityException ex) {
            throw new ConfigException("failed to create instance", ex);
        } catch (InstantiationException ex) {
            throw new ConfigException("failed to create instance", ex);
        } catch (IllegalAccessException ex) {
            throw new ConfigException("failed to create instance", ex);
        } catch (IllegalArgumentException ex) {
            throw new ConfigException("failed to create instance", ex);
        } catch (InvocationTargetException ex) {
            throw new ConfigException("failed to create instance", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T toObjArray(ConfigValue value, Class<T> arryCls) {
        Class<?> componentType = arryCls.getComponentType();
        List<ConfigValue> values = value.toValueList();
        Object arry = Array.newInstance(componentType, values.size());
        for (int i = 0; i < values.size(); i++) {
            ConfigValue val = values.get(i);
            Array.set(arry, i, toObject(val, componentType));
        }
        return (T) arry;
    }

    private static <T> T toMapObj(ConfigValue value, Class<T> cls) {
        MapOf mo = cls.getAnnotation(MapOf.class);
        if (mo != null) {
            Map<String, ?> map = toMap(value, mo.value());
            try {
                return cls.getConstructor(Map.class).newInstance(map);
            } catch (InstantiationException ex) {
                throw new ConfigException("failed to create instance", ex);
            } catch (IllegalAccessException ex) {
                throw new ConfigException("failed to create instance", ex);
            } catch (IllegalArgumentException ex) {
                throw new ConfigException("failed to create instance", ex);
            } catch (InvocationTargetException ex) {
                throw new ConfigException("failed to create instance", ex);
            } catch (NoSuchMethodException ex) {
                throw new ConfigException("failed to create instance", ex);
            } catch (SecurityException ex) {
                throw new ConfigException("failed to create instance", ex);
            }
        }
        return null;
    }

    /**
     * Converts a value to an object map that maps strings
     * to objects of the given class.
     * @param value the value to be converted.
     * @param cls the class of the objects
     * @return an empty map if the value does not contain a name-value pair.
     */
    public static <T> Map<String, T> toMap(ConfigValue value, Class<T> cls) {
        Map<String, T> map = new HashMap<String, T>();
        for (Map.Entry<String, ConfigValue> en : value.toValueMap().entrySet()) {
            T val = toObject(en.getValue(), cls);
            map.put(en.getKey(), val);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static <T> T toPrimitive(ConfigValue value, Class<T> cls) {
        if (cls.equals(Properties.class)) {
            return cls.cast(value.toProperties());
        }
        String strVal = value.stringValue();
        if (strVal == null) {
            return null;
        }
        if (cls.equals(int.class) || cls.equals(Integer.class)) {
            return (T) Integer.valueOf(strVal);
        }
        if (cls.equals(long.class) || cls.equals(Long.class)) {
            return (T) Long.valueOf(strVal);
        }
        if (cls.equals(double.class) || cls.equals(Double.class)) {
            return (T) Double.valueOf(strVal);
        }
        if (cls.equals(boolean.class) || cls.equals(Boolean.class)) {
            return (T) Boolean.valueOf(value.booleanValue());
        }
        if (cls.equals(String.class)) {
            return cls.cast(strVal);
        }
        if (cls.equals(BigDecimal.class)) {
            return cls.cast(new BigDecimal(strVal));
        }
        // if the given class is a subclass of JsonValue
        if (JsonValue.class.isAssignableFrom(cls)) {
            return cls.cast(value.toJson());
        }
        return null;
    }

    static <T> T toObjBySetters(ConfigValue value, Class<T> cls) {
        try {
            T obj = cls.newInstance();
            for (Field f : cls.getDeclaredFields()) {
                Method setter = getSetter(cls, f.getName(), f.getType());
                if (setter != null) {
                    Object val = value.findObject(f.getName(), f.getType());
                    if (val != null) {
                        setter.invoke(obj, val);
                    }
                }
            }
            return obj;
        } catch (InstantiationException ex) {
            throw new ConfigException("failed to create instance", ex);
        } catch (IllegalAccessException ex) {
            throw new ConfigException("failed to create instance", ex);
        } catch (IllegalArgumentException ex) {
            throw new ConfigException("failed to create instance", ex);
        } catch (SecurityException ex) {
            throw new ConfigException("failed to create instance", ex);
        } catch (InvocationTargetException ex) {
            throw new ConfigException("failed to create instance", ex);
        }
    }

    static Method getSetter(Class<?> cls, String name, Class<?> type) {
        String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
        try {
            return cls.getMethod(methodName, type);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sets an object to the builder.
     * @param builder the value builder to be used.
     * @param name the name with which the object is set.
     * @param obj the object to be set as a value.
     */
    public static void setObject(ValueBuilder builder, String name, Object obj) {
        if (obj == null) {
            return;
        }
        if (obj instanceof Properties) {
            builder.add(name, (Properties) obj);
        } else if (obj instanceof String) {
            builder.add(name, (String) obj);
        } else if (obj instanceof Number) {
            builder.add(name, (Number) obj);
        } else if (obj instanceof Boolean) {
            builder.add(name, ((Boolean) obj).booleanValue());
        } else if (obj instanceof ConfigValue) {
            builder.add(name, (ConfigValue) obj);
        } else if (obj.getClass().isArray()) {
            /**
             * TODO implement array to value
             */
            throw new ConfigException("value from array not supported yet");
        } else {
            ValueBuilder valBuilder = Values.builder(name);
            buildWithGetters(valBuilder, obj);
            builder.add(name, valBuilder);
        }
    }

    protected static void buildWithGetters(ValueBuilder builder, Object obj) {
        Class<?> cls = obj.getClass();
        try {
            for (Field f : cls.getDeclaredFields()) {
                Method getter = getGetter(cls, f.getName(), f.getType());
                if (getter != null) {
                    setObject(builder, f.getName(), getter.invoke(obj));
                }
            }
        } catch (SecurityException ex) {
            throw new ConfigException("failed to create value from object", ex);
        } catch (IllegalAccessException ex) {
            throw new ConfigException("failed to create value from object", ex);
        } catch (IllegalArgumentException ex) {
            throw new ConfigException("failed to create value from object", ex);
        } catch (InvocationTargetException ex) {
            throw new ConfigException("failed to create value from object", ex);
        }
    }

    static Method getGetter(Class<?> cls, String name, Class<?> type) {
        String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
        try {
            Method getter = cls.getMethod(methodName);
            if (type.isAssignableFrom(getter.getReturnType())) {
                return getter;
            }
            return null;
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
}
