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

package com.nec.congenio;

import java.io.Writer;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Properties;

import com.nec.congenio.value.PrimitiveValue;
import com.nec.congenio.value.xml.XmlValueBuilder;
import com.nec.congenio.value.xml.XmlValueFormat;

public final class Values {
    public static final ConfigValue NONE = PrimitiveValue.NULL;

    private static final ValueFormat FORMAT = new XmlValueFormat();

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
        return XmlValueBuilder.create(name);
    }

    public static ValueBuilder builder(String name, ConfigValue proto) {
        return XmlValueBuilder.create(name, proto);
    }

    public static boolean isNull(ConfigValue value) {
        return (value == NONE || value instanceof PrimitiveValue.NullValue);
    }

    public static ConfigValue valueOf(int value) {
        return PrimitiveValue.valueOf(value);
    }

    public static ConfigValue valueOf(long value) {
        return PrimitiveValue.valueOf(value);
    }

    public static ConfigValue valueOf(double value) {
        return PrimitiveValue.valueOf(value);
    }

    public static ConfigValue valueOf(String value) {
        return PrimitiveValue.valueOf(value);
    }

    public static ConfigValue valueOf(BigDecimal value) {
        return PrimitiveValue.valueOf(value);
    }

    public static ConfigValue valueOf(boolean value) {
        return PrimitiveValue.valueOf(value);
    }

    /**
     * Converts a property set (Properties) to a config
     * value.
     * @param value a property set to be converted.
     * @return a config value that represents the given
     *         property set.
     */
    public static ConfigValue valueOf(Properties value) {
        ValueBuilder builder = builder("properties");
        for (Object key : value.keySet()) {
            String name = key.toString();
            builder.add(name, value.getProperty(name));
        }
        return builder.build();
    }

    /**
     * Creates a ConfigValue from an object. The object must be either: (1)
     * primitive objects (Number, String, Boolean), (2) Properties, or (3) A
     * simple Java bean with getters (of type (1), (2), or (3))
     * 
     * @param value the object that is converted to a value.
     */
    public static ConfigValue create(Object value) {
        if (value == null) {
            return NONE;
        }
        /**
         * TODO refactor
         */
        return XmlValueBuilder.create("t")
                .add("t", value).build().getValue("t");
    }

}
