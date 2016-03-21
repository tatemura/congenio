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

import java.io.File;
import java.io.Writer;
import java.util.Map;

import javax.annotation.Nullable;
import com.nec.congenio.impl.ConfigFactory;

public abstract class ConfigDescription {
    public static final String[] SUFFIXES = { "xml", "json", "properties" };
    public static final String ATTR_EXTENDS = "extends";
    public static final String ATTR_REF = "ref";

    public static ConfigValue resolve(File file) {
        return create(file).resolve();
    }

    /**
     * Evaluates the document without foreach unfolding, resulting in a single
     * value.
     * 
     * @return The result value.
     */
    public abstract ConfigValue resolve();

    public static Iterable<ConfigValue> evaluate(File file) {
        return create(file).evaluate();
    }

    /**
     * Fully evaluates the document: (1) foreach unfolding, (2) reference
     * resolution, and (3) value expression evaluation.
     * 
     * @return a sequence of the result values.
     */
    public abstract Iterable<ConfigValue> evaluate();

    public static ConfigDescription create(File file) {
        return create(file,
                ConfigProperties.getLibDefs(file.getParentFile()));
    }

    public static ConfigDescription create(File file, Map<String, String> libDefs) {
        return new ConfigFactory(libDefs).create(file);
    }

    public static ConfigDescription create(Class<?> cls, String name) {
        return create(cls, name,
                ConfigProperties.getLibDefs());
    }

    public static ConfigDescription create(Class<?> cls,
            String name, Map<String, String> libDefs) {
        return new ConfigFactory(libDefs).create(cls, name);
    }

    public static ConfigDescription create(File file, ConfigDescription base) {
        return create(file, base,
                ConfigProperties.getLibDefs(file.getParentFile()));
    }

    public static ConfigDescription create(File file, ConfigDescription base,
            Map<String, String> libDefs) {
        return new ConfigFactory(libDefs).create(file, base);
    }

    public abstract String getName();

    /**
     * Writes the extended document without indentation.
     * 
     * @param writer writer to use write this description.
     */
    public void write(Writer writer) {
        write(writer, false);
    }

    /**
     * Writes the extended document.
     * 
     * @param writer
     *            writer to use write this description.
     * @param indent
     *            true to use indentation.
     */
    public abstract void write(Writer writer, boolean indent);

    /**
     * Evaluates a part of the document specified with the name, without foreach
     * unfolding.
     *
     * @param name the name of the config value element.
     * @return equivalent to resolve().find(name).
     */
    @Nullable
    public abstract String get(String name);

//<<<<<<< Updated upstream
//=======
//
//    private static Properties congenProperties() {
//        return ConfigProperties.getProperties();
//    }
//
//    private static Properties congenProperties(File file) {
//        return ConfigProperties.getProperties(file.getParentFile());
//    }
//>>>>>>> Stashed changes
}
