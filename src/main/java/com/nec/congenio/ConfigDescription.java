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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.json.JsonValue;

import com.nec.congenio.impl.ConfigFactory;
import com.nec.congenio.json.JsonValueUtil;

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
        return create(file, congenProperties(file));
    }

    public static ConfigDescription create(File file, Properties props) {
        return new ConfigFactory(props).create(file);
    }

    public static ConfigDescription create(Class<?> cls, String name) {
        return create(cls, name, congenProperties());
    }

    public static ConfigDescription create(Class<?> cls, String name, Properties props) {
        return new ConfigFactory(props).create(cls, name);
    }

    public static ConfigDescription create(File file, ConfigDescription base) {
        return create(file, base, congenProperties(file));
    }

    public static ConfigDescription create(File file, ConfigDescription base, Properties props) {
        return new ConfigFactory(props).create(file, base);
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

    public static final String EXTEND_ONLY = "extend-only";
    public static final String PROP_MODE = "cdl.mode";
    public static final String PROP_OUT = "cdl.output";
    public static final String PROP_IDX = "cdl.doc.idx";
    public static final String PROP_PATH = "cdl.doc.path";
    public static final String PROP_BASE = "cdl.doc.base";

    /**
     * Converts a config file.
     * @param args the filename of the config file.
     * @throws Exception when conversion is failed.
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("arg: filename");
            return;
        }
        String base = System.getProperty(PROP_BASE);
        File file = new File(args[0]);
        ConfigDescription cdl;
        if (base != null) {
            cdl = ConfigDescription.create(file, ConfigDescription.create(new File(base)));
        } else {
            cdl = ConfigDescription.create(file);
        }

        if (EXTEND_ONLY.equals(System.getProperty(PROP_MODE))) {
            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            cdl.write(writer, true);
        } else if ("json".equals(System.getProperty(PROP_OUT))) {
            Filter filter = new Filter(System.getProperty(PROP_IDX));
            Projection proj = new Projection(System.getProperty(PROP_PATH));
            int idx = 0;
            for (ConfigValue conf : cdl.evaluate()) {
                if (filter.output(idx)) {
                    JsonValue value = proj.project(conf).toJson();
                    System.out.println(JsonValueUtil.toString(value));
                }
                idx++;
                if (idx > filter.maxIndex()) {
                    break;
                }
            }
        } else {
            Filter filter = new Filter(System.getProperty(PROP_IDX));
            Projection proj = new Projection(System.getProperty(PROP_PATH));
            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            int idx = 0;
            for (ConfigValue conf : cdl.evaluate()) {
                if (filter.output(idx)) {
                    writeSeparator(idx, writer);
                    Values.write(proj.project(conf), writer, true);
                }
                idx++;
                if (idx > filter.maxIndex()) {
                    break;
                }
            }
        }
    }

    static Properties congenProperties() {
        Properties sys = System.getProperties();
        Properties prop = ConfigProperties.getProperties();
        return ConfigProperties.merge(prop, sys);
    }

    static Properties congenProperties(File file) {
        Properties sys = System.getProperties();
        Properties prop = ConfigProperties.getProperties(file.getParentFile());
        return ConfigProperties.merge(prop, sys);
    }

    static void writeSeparator(int idx, Writer writer) throws IOException {
        if (idx > 0) {
            writer.write("<!-- " + idx + " -->\n");
        }
    }

    static class Projection {
        String[] path;

        Projection(String pattern) {
            if (pattern == null) {
                path = new String[0];
            } else {
                // path = pattern.split("\\.");
                path = pattern.split("/");
            }
        }

        public ConfigValue project(ConfigValue val) {
            ConfigValue res = val;
            for (String p : path) {
                res = res.getValue(p);
            }
            return res;
        }
    }

    static class Filter {
        int idx;

        Filter(String pattern) {
            if (pattern != null) {
                idx = Integer.parseInt(pattern);
            } else {
                idx = -1;
            }
        }

        public boolean output(int idx) {
            return this.idx < 0 || this.idx == idx;
        }

        public int maxIndex() {
            if (idx < 0) {
                return Integer.MAX_VALUE;
            } else {
                return idx;
            }
        }
    }
}
