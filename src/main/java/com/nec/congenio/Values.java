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
package com.nec.congenio;

import java.io.Writer;
import java.net.URL;

import com.nec.congenio.value.PrimitiveValue;
import com.nec.congenio.value.xml.XMLValueBuilder;
import com.nec.congenio.value.xml.XMLValueFormat;

public final class Values {
	public static final ConfigValue NONE =
			PrimitiveValue.NULL;

	private static final ValueFormat FORMAT = new XMLValueFormat();
	private Values() {
		// not instantiated
	}

	public static ConfigValue parseValue(URL source) {
		return FORMAT.parse(source);
	}

	public static ConfigValue parseValue(String text) {
		return FORMAT.parse(text);
	}
	public static void write(ConfigValue conf, Writer writer, boolean indent) {
		FORMAT.write(conf, writer, indent);
	}

	public static ValueBuilder builder(String name) {
		return XMLValueBuilder.create(name);
	}

	public static ValueBuilder builder(String name, ConfigValue proto) {
		return XMLValueBuilder.create(name, proto);
	}

	public static boolean isNull(ConfigValue value) {
		return (value == NONE || value instanceof PrimitiveValue.NullValue);
	}
}
