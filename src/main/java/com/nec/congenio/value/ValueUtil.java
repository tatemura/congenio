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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nullable;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;

public final class ValueUtil {

	private ValueUtil() {
	}

	@Nullable
	public static <T> T findObject(ConfigValue conf, String name, Class<T> cls) {
		ConfigValue value = conf.findValue(name);
		if (value != null) {
			return toObject(value, cls);
		}
		return null;
	}
	public static <T> T getObject(ConfigValue conf, String name, Class<T> cls) {
		return toObject(conf.getValue(name), cls);
	}
	public static <T> T toObject(ConfigValue value, Class<T> cls) {
		if (cls.isArray()) {
			return toObjArray(value, cls);
		}
		T obj = toPrimitive(value, cls);
		if (obj != null) {
			return obj;
		}
		try {
			return cls.getConstructor(ConfigValue.class).newInstance(value);
		} catch (NoSuchMethodException e) {
			return toObjBySetters(value, cls);
		} catch (SecurityException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (InstantiationException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (IllegalAccessException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (IllegalArgumentException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (InvocationTargetException e) {
			throw new ConfigException("failed to create instance", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T toObjArray(ConfigValue value, Class<T> arryCls) {
		Class<?> componentType = arryCls.getComponentType();
		List<ConfigValue> values = value.toValueList();
		Object arry = Array.newInstance(componentType, values.size());
		for (int i = 0; i < values.size(); i++) {
			ConfigValue v = values.get(i);
			Array.set(arry, i, toObject(v, componentType));
		}
		return (T) arry;
	}
	@SuppressWarnings("unchecked")
	private static <T> T toPrimitive(ConfigValue value, Class<T> cls) {
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
		if (cls.equals(Properties.class)) {
			return cls.cast(value.toProperties());
		}
		return null;
	}
	static <T> void setup(T obj, ConfigValue value, Class<T> cls) {
		try {
			for (Field f : cls.getDeclaredFields()) {
				Method s = getSetter(cls, f.getName(), f.getType());
				if (s != null) {
					Object v = findObject(value, f.getName(), f.getType());
					if (v != null) {
						s.invoke(obj, v);
					}
				}
			}
		} catch (SecurityException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (IllegalAccessException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (IllegalArgumentException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (InvocationTargetException e) {
			throw new ConfigException("failed to create instance", e);
		}
	}

	static <T> T toObjBySetters(ConfigValue value, Class<T> cls) {
		try {
			T obj = cls.newInstance();
			setup(obj, value, cls);
			return obj;
		} catch (InstantiationException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (IllegalAccessException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (IllegalArgumentException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (SecurityException e) {
			throw new ConfigException("failed to create instance", e);
		}
	}

	static Method getSetter(Class<?> cls, String name, Class<?> type) {
		String methodName = "set" + name.substring(0, 1).toUpperCase()
				+ name.substring(1);
		try {
			return cls.getMethod(methodName, type);
		} catch (NoSuchMethodException e) {
			return null;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
