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
	private static <T> T toMapObj(ConfigValue value, Class<T> cls) {
		MapOf mo = cls.getAnnotation(MapOf.class);
		if (mo != null) {
			Map<String, ?> map = toMap(value, mo.value());
			try {
				return cls.getConstructor(Map.class).newInstance(map);
			} catch (InstantiationException e) {
				throw new ConfigException("failed to create instance", e);
			} catch (IllegalAccessException e) {
				throw new ConfigException("failed to create instance", e);
			} catch (IllegalArgumentException e) {
				throw new ConfigException("failed to create instance", e);
			} catch (InvocationTargetException e) {
				throw new ConfigException("failed to create instance", e);
			} catch (NoSuchMethodException e) {
				throw new ConfigException("failed to create instance", e);
			} catch (SecurityException e) {
				throw new ConfigException("failed to create instance", e);
			}
		}
		return null;
	}

	public static <T> Map<String, T> toMap(ConfigValue value, Class<T> cls) {
		Map<String, T> map = new HashMap<String, T>();
		for (Map.Entry<String, ConfigValue> en : value.toValueMap().entrySet()) {
			T v = toObject(en.getValue(), cls);
			map.put(en.getKey(), v);
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
				Method s = getSetter(cls, f.getName(), f.getType());
				if (s != null) {
					Object v = value.findObject(f.getName(), f.getType());
					if (v != null) {
						s.invoke(obj, v);
					}
				}
			}
			return obj;
		} catch (InstantiationException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (IllegalAccessException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (IllegalArgumentException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (SecurityException e) {
			throw new ConfigException("failed to create instance", e);
		} catch (InvocationTargetException e) {
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
			ValueBuilder b = Values.builder(name);
			buildWithGetters(b, obj);
			builder.add(name, b);
		}
	}
	public static void buildWithGetters(ValueBuilder builder, Object obj) {
		Class<?> cls = obj.getClass();
		try {
			for (Field  f : cls.getDeclaredFields()) {
				Method g = getGetter(cls, f.getName(), f.getType());
				if (g != null) {
					setObject(builder, f.getName(), g.invoke(obj));
				}
			}
		} catch (SecurityException e) {
			throw new ConfigException("failed to create value from object", e);
		} catch (IllegalAccessException e) {
			throw new ConfigException("failed to create value from object", e);
		} catch (IllegalArgumentException e) {
			throw new ConfigException("failed to create value from object", e);
		} catch (InvocationTargetException e) {
			throw new ConfigException("failed to create value from object", e);
		}
	}
	static Method getGetter(Class<?> cls, String name, Class<?> type) {
		String methodName = "get" + name.substring(0, 1).toUpperCase()
				+ name.substring(1);
		try {
			Method m = cls.getMethod(methodName);
			if (type.isAssignableFrom(m.getReturnType())) {
				return m;
			}
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
