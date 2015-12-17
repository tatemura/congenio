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

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

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
		T obj = toPrimitive(value, cls);
		if (obj != null) {
			return obj;
		}
		try {
			return cls.getConstructor(ConfigValue.class).newInstance(value);
		} catch (NoSuchMethodException e) {
			/**
			 * TODO try a different way to instantiate: e.g.
			 * create as a bean.
			 */
			throw new ConfigException("no constructor found", e);
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
	private static <T> T toPrimitive(ConfigValue value, Class<T> cls) {
		String strVal = value.stringValue();
		if (strVal == null) {
			return null;
		}
		if (cls.equals(Integer.class)) {
			return cls.cast(Integer.parseInt(strVal));
		}
		if (cls.equals(Long.class)) {
			return cls.cast(Long.parseLong(strVal));
		}
		if (cls.equals(Double.class)) {
			return cls.cast(Double.parseDouble(strVal));
		}
		if (cls.equals(String.class)) {
			return cls.cast(strVal);
		}
		if (cls.equals(BigDecimal.class)) {
			return cls.cast(new BigDecimal(strVal));
		}
		return null;
	}
}
